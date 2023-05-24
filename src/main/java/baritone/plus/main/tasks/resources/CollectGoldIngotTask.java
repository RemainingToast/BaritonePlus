package baritone.plus.main.tasks.resources;

import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.*;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.container.CraftInTableTask;
import baritone.plus.main.tasks.container.SmeltInBlastFurnaceTask;
import baritone.plus.main.tasks.container.SmeltInFurnaceTask;
import baritone.plus.main.tasks.movement.DefaultGoToDimensionTask;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class CollectGoldIngotTask extends ResourceTask {

    private final int _count;

    public CollectGoldIngotTask(int count) {
        super(Items.GOLD_INGOT, count);
        _count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {
        mod.getBehaviour().push();
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {
        if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD) {
            if (mod.getModSettings().shouldUseBlastFurnace()) {
                if (mod.getItemStorage().hasItem(Items.BLAST_FURNACE) ||
                        mod.getBlockTracker().anyFound(Blocks.BLAST_FURNACE) ||
                        mod.getEntityTracker().itemDropped(Items.BLAST_FURNACE)) {
                    return new SmeltInBlastFurnaceTask(new SmeltTarget(new ItemTarget(Items.GOLD_INGOT, _count), new ItemTarget(Items.RAW_GOLD, _count)));
                }
                if (_count < 5) {
                    return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.GOLD_INGOT, _count), new ItemTarget(Items.RAW_GOLD, _count)));
                }
                mod.getBehaviour().addProtectedItems(Items.COBBLESTONE, Items.STONE, Items.SMOOTH_STONE);
                Optional<BlockPos> furnacePos = mod.getBlockTracker().getNearestTracking(Blocks.FURNACE);
                furnacePos.ifPresent(blockPos -> mod.getBehaviour().avoidBlockBreaking(blockPos));
                if (mod.getItemStorage().getItemCount(Items.IRON_INGOT) >= 5) {
                    return TaskCatalogue.getItemTask(Items.BLAST_FURNACE, 1);
                }
                return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, 5), new ItemTarget(Items.RAW_IRON, 5)));
            }
            return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.GOLD_INGOT, _count), new ItemTarget(Items.RAW_GOLD, _count)));
        } else if (WorldHelper.getCurrentDimension() == Dimension.NETHER) {
            // If we have enough nuggets, craft them.
            int nuggs = mod.getItemStorage().getItemCount(Items.GOLD_NUGGET);
            int nuggs_needed = _count * 9 - mod.getItemStorage().getItemCount(Items.GOLD_INGOT) * 9;
            if (nuggs >= nuggs_needed) {
                ItemTarget n = new ItemTarget(Items.GOLD_NUGGET);
                CraftingRecipe recipe = CraftingRecipe.newShapedRecipe("gold_ingot", new ItemTarget[]{
                        n, n, n, n, n, n, n, n, n
                }, 1);
                return new CraftInTableTask(new RecipeTarget(Items.GOLD_INGOT, _count, recipe));
            }
            // Mine nuggets
            return new MineAndCollectTask(new ItemTarget(Items.GOLD_NUGGET, _count * 9), new Block[]{Blocks.NETHER_GOLD_ORE}, MiningRequirement.WOOD);
        } else {
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectGoldIngotTask && ((CollectGoldIngotTask) other)._count == _count;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " gold.";
    }
}
