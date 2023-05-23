package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.ITaskRequiresGrounded;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;

public class GetToBlockTask extends CustomBaritoneGoalTask implements ITaskRequiresGrounded {

    private final BlockPos _position;
    private final boolean _preferStairs;
    private final Dimension _dimension;

    public GetToBlockTask(BlockPos position, boolean preferStairs) {
        this(position, preferStairs, null);
    }

    public GetToBlockTask(BlockPos position, Dimension dimension) {
        this(position, false, dimension);
    }

    public GetToBlockTask(BlockPos position, boolean preferStairs, Dimension dimension) {
        _dimension = dimension;
        _position = position;
        _preferStairs = preferStairs;
    }

    public GetToBlockTask(BlockPos position) {
        this(position, false);
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (_dimension != null && WorldHelper.getCurrentDimension() != _dimension) {
            return new DefaultGoToDimensionTask(_dimension);
        }
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
        if (other instanceof GetToBlockTask task) {
            return task._position.equals(_position) && task._preferStairs == _preferStairs && task._dimension == _dimension;
        }
        return false;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return super.isFinished(mod) && (_dimension == null || _dimension == WorldHelper.getCurrentDimension());
    }

    @Override
    protected String toDebugString() {
        return "Getting to block " + _position + (_dimension != null ? " in dimension " + _dimension : "");
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
