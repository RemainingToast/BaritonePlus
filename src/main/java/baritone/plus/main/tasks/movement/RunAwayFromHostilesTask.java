package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.baritone.GoalRunAwayFromEntities;
import baritone.plus.api.util.helpers.BaritoneHelper;
import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RunAwayFromHostilesTask extends CustomBaritoneGoalTask {

    private final double _distanceToRun;
    private final boolean _includeSkeletons;

    public RunAwayFromHostilesTask(double distance, boolean includeSkeletons) {
        _distanceToRun = distance;
        _includeSkeletons = includeSkeletons;
    }

    public RunAwayFromHostilesTask(double distance) {
        this(distance, false);
    }


    @Override
    protected Goal newGoal(BaritonePlus mod) {
        // We want to run away NOW
        mod.getClientBaritone().getPathingBehavior().forceCancel();
        return new GoalRunAwayFromHostiles(mod, _distanceToRun);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RunAwayFromHostilesTask task) {
            return Math.abs(task._distanceToRun - _distanceToRun) < 1;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Run Away From Mobs..";
    }

    private class GoalRunAwayFromHostiles extends GoalRunAwayFromEntities {

        public GoalRunAwayFromHostiles(BaritonePlus mod, double distance) {
            super(mod, distance, false, 0.8);
        }

        @Override
        protected List<Entity> getEntities(BaritonePlus mod) {
            List<Entity> result;
            Stream<Entity> stream = mod.getEntityTracker().getHostiles().stream();
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                if (!_includeSkeletons) {
                    stream = stream.filter(hostile -> !(hostile instanceof SkeletonEntity));
                }
                var list = stream.collect(Collectors.toList());
                setDebugState(String.format("Running from %s hostiles", list.size()));
                return list;
            }
        }
    }
}
