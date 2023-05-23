package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class RunAwayFromPositionTask extends CustomBaritoneGoalTask {

    private final BlockPos[] _dangerBlocks;
    private final double _distance;
    private final Integer _maintainY;

    public RunAwayFromPositionTask(double distance, BlockPos... toRunAwayFrom) {
        this(distance, null, toRunAwayFrom);
    }

    public RunAwayFromPositionTask(double distance, Integer maintainY, BlockPos... toRunAwayFrom) {
        _distance = distance;
        _dangerBlocks = toRunAwayFrom;
        _maintainY = maintainY;
    }

    @Override
    protected Goal newGoal(BaritonePlus mod) {
        return new GoalRunAway(_distance, _maintainY, _dangerBlocks);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RunAwayFromPositionTask task) {
            return Arrays.equals(task._dangerBlocks, _dangerBlocks);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Running away from " + Arrays.toString(_dangerBlocks);
    }
}
