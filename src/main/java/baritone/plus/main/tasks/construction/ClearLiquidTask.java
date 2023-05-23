package baritone.plus.main.tasks.construction;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.InteractWithBlockTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

/**
 * Removes a liquid source block at a position.
 */
public class ClearLiquidTask extends Task {

    private final BlockPos _liquidPos;

    public ClearLiquidTask(BlockPos liquidPos) {
        this._liquidPos = liquidPos;
    }

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (mod.getItemStorage().hasItem(Items.BUCKET)) {
            mod.getBehaviour().setRayTracingFluidHandling(RaycastContext.FluidHandling.SOURCE_ONLY);
            return new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), _liquidPos, false);
        }

        return new PlaceStructureBlockTask(_liquidPos);
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        if (mod.getChunkTracker().isChunkLoaded(_liquidPos)) {
            return mod.getWorld().getBlockState(_liquidPos).getFluidState().isEmpty();
        }
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ClearLiquidTask task) {
            return task._liquidPos.equals(_liquidPos);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Clear liquid at " + _liquidPos;
    }
}
