package baritone.plus.main.tasks.movement;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.plus.api.tasks.ITaskRequiresGrounded;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class GetToSurfaceTask extends CustomBaritoneGoalTask implements ITaskRequiresGrounded {

    private BlockPos _position;
    private final boolean _preferStairs;

    public GetToSurfaceTask() {
        this(false);
    }

    public GetToSurfaceTask(boolean preferStairs) {
        _preferStairs = preferStairs;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            Debug.logMessage("Can not 'get to surface' in %s", WorldHelper.getCurrentDimension());
            stop(mod);
            return null;
        }

        // Get the surface position for the player's current X, Z coordinate
        var _blockPos = mod.getPlayer().getBlockPos().add(10, 0, 10);
        var _yLevel = mod.getWorld().getTopY(Heightmap.Type.WORLD_SURFACE,
                _blockPos.getX(),
                _blockPos.getZ()
        );

        _position = new BlockPos(_blockPos.getX(), _yLevel, _blockPos.getZ());

        return super.onTick(mod);
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        super.onStart(mod);
        if (_preferStairs) {
            mod.getBehaviour().push();
            mod.getBehaviour().setPreferredStairs(true);
        }
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        super.onStop(mod, interruptTask);
        if (_preferStairs) {
            mod.getBehaviour().pop();
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToSurfaceTask task) {
            return task._position.equals(_position) && task._preferStairs == _preferStairs;
        }
        return false;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return super.isFinished(mod);
    }

    @Override
    protected String toDebugString() {
        return "Getting to block " + _position;
    }

    @Override
    protected Goal newGoal(BaritonePlus mod) {
        return new GoalBlock(_position);
    }

    @Override
    protected void onWander(BaritonePlus mod) {
        super.onWander(mod);
        mod.getBlockTracker().requestBlockUnreachable(_position);
    }
}
