package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.baritone.GoalDirectionXZ;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

public class GoInDirectionXZTask extends CustomBaritoneGoalTask {

    private final Vec3d _origin;
    private final Vec3d _delta;
    private final double _sidePenalty;

    public GoInDirectionXZTask(Vec3d origin, Vec3d delta, double sidePenalty) {
        _origin = origin;
        _delta = delta;
        _sidePenalty = sidePenalty;
    }

    private static boolean closeEnough(Vec3d a, Vec3d b) {
        return a.squaredDistanceTo(b) < 0.001;
    }

    @Override
    protected Goal newGoal(BaritonePlus mod) {
        try {
            return new GoalDirectionXZ(_origin, _delta, _sidePenalty);
        } catch (Exception e) {
            Debug.logMessage("Invalid goal direction XZ (probably zero distance)");
            return null;
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GoInDirectionXZTask) {
            GoInDirectionXZTask task = (GoInDirectionXZTask) other;
            return (closeEnough(task._origin, _origin) && closeEnough(task._delta, _delta));
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Going in direction: <" + _origin.x + "," + _origin.z + "> direction: <" + _delta.x + "," + _delta.z + ">";
    }
}
