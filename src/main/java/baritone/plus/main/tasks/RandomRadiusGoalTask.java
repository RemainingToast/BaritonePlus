package baritone.plus.main.tasks;

import baritone.plus.api.tasks.Task;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.movement.GetToXZTask;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public final class RandomRadiusGoalTask extends Task {
    private BlockPos end;
    private final Random rand;
    private Task goal;
    private double r;
    private boolean finished;

    public RandomRadiusGoalTask(final BlockPos start, final double r) {
        this.rand = new Random();
        this.r = r;
        init(start);
    }

    private void init(final BlockPos start) {
        this.finished = false;

        final double phi = rand.nextInt(360) + rand.nextDouble();
        final double radius = rand.nextInt((int) Math.round(r)) + rand.nextDouble(); //rand.nextDouble(r) + rand.nextDouble();
        final int x = (int) Math.round(radius * Math.sin(phi));
        final int z = (int) Math.round(radius * Math.cos(phi));

        this.end = new BlockPos(start.getX() + x, start.getY(), start.getZ() + z);
        this.goal = new GetToXZTask(this.end.getX(), this.end.getZ());
    }

    public Task next(final BlockPos start) {
        return next(start, 0.3d);
    }

    public Task next(final BlockPos start, final double increaseRadius) {
        if (finished) {
            this.r += increaseRadius;
            init(start);
            //goal.reset();
            this.finished = false;
        }
        return this;
    }

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        this.finished = goal.isFinished(mod);
        return goal;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        this.finished = true;
        goal.stop(mod);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RandomRadiusGoalTask task) {
            return task.end.getX() == end.getX() && task.end.getY() == end.getY() && task.end.getZ() == end.getZ();
        }
        return false;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return goal.isFinished(mod) || this.finished;
    }

    @Override
    protected String toDebugString() {
        return "RandomRadiusGoalTask";
    }
}
