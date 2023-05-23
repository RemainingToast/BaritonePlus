package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.CraftInInventoryTask;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.RecipeTarget;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public class CollectNetherBricksTask extends ResourceTask {

    private final int _count;

    public CollectNetherBricksTask(int count) {
        super(Items.NETHER_BRICKS, count);
        _count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {
        mod.getBlockTracker().trackBlock(Blocks.NETHER_BRICKS);
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {

        /*
         * If we find nether bricks, mine them.
         *
         * Otherwise craft them from the "nether_brick" item.
         */

        if (mod.getBlockTracker().anyFound(Blocks.NETHER_BRICKS)) {
            return new MineAndCollectTask(Items.NETHER_BRICKS, _count, new Block[]{Blocks.NETHER_BRICKS}, MiningRequirement.WOOD);
        }

        ItemTarget b = new ItemTarget(Items.NETHER_BRICK, 1);
        return new CraftInInventoryTask(new RecipeTarget(Items.NETHER_BRICK, _count, CraftingRecipe.newShapedRecipe("nether_brick", new ItemTarget[]{b, b, b, b}, 1)));
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.NETHER_BRICKS);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectNetherBricksTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " nether bricks.";
    }
}
