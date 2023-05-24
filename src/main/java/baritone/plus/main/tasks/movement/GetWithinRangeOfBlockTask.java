package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.util.math.BlockPos;

public class GetWithinRangeOfBlockTask extends CustomBaritoneGoalTask {

    private final BlockPos _blockPos;
    private final int _range;

    public GetWithinRangeOfBlockTask(BlockPos blockPos, int range) {
        _blockPos = blockPos;
        _range = range;
    }

    @Override
    protected Goal newGoal(BaritonePlus mod) {
        return new GoalNear(_blockPos, _range);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetWithinRangeOfBlockTask task) {
            return task._blockPos.equals(_blockPos) && task._range == _range;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Getting within " + _range + " blocks of " + _blockPos.toShortString();
    }
}
