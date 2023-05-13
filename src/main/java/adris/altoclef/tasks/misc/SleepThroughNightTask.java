package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;

public class SleepThroughNightTask extends Task {

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (!WorldHelper.canSleepOnServer()) {
            return new PlaceBedAndSetSpawnTask();
        }

        return new PlaceBedAndSetSpawnTask().stayInBed();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SleepThroughNightTask;
    }

    @Override
    protected String toDebugString() {
        return "Sleeping through the night";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        // We're in daytime
        int time = (int) (mod.getWorld().getTimeOfDay() % 24000);
        return WorldHelper.canSleepOnServer() || 0 <= time && time < 13000;
    }

}
