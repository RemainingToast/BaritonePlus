package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.container.SmeltInBlastFurnaceTask;
import baritone.plus.main.tasks.container.SmeltInFurnaceTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.SmeltTarget;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class CollectIronIngotTask extends ResourceTask {

    private final int _count;

    public CollectIronIngotTask(int count) {
        super(Items.IRON_INGOT, count);
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
        if (mod.getModSettings().shouldUseBlastFurnace()) {
            if (mod.getItemStorage().hasItem(Items.BLAST_FURNACE) ||
                    mod.getBlockTracker().anyFound(Blocks.BLAST_FURNACE) ||
                    mod.getEntityTracker().itemDropped(Items.BLAST_FURNACE)) {
                return new SmeltInBlastFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, _count), new ItemTarget(Items.RAW_IRON, _count)));
            }
            if (_count < 5) {
                return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, _count), new ItemTarget(Items.RAW_IRON, _count)));
            }
            mod.getBehaviour().addProtectedItems(Items.COBBLESTONE, Items.STONE, Items.SMOOTH_STONE);
            Optional<BlockPos> furnacePos = mod.getBlockTracker().getNearestTracking(Blocks.FURNACE);
            furnacePos.ifPresent(blockPos -> mod.getBehaviour().avoidBlockBreaking(blockPos));
            if (mod.getItemStorage().getItemCount(Items.IRON_INGOT) >= 5) {
                return TaskCatalogue.getItemTask(Items.BLAST_FURNACE, 1);
            }
            return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, 5), new ItemTarget(Items.RAW_IRON, 5)));
        }
        return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.IRON_INGOT, _count), new ItemTarget(Items.RAW_IRON, _count)));
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectIronIngotTask && ((CollectIronIngotTask) other)._count == _count;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " iron.";
    }
}
