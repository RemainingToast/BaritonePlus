package baritone.plus.main.tasks.misc;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.helpers.WorldHelper;

public class SleepThroughNightTask extends Task {

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (!WorldHelper.canSleepOnServer()) {
            return new PlaceBedAndSetSpawnTask();
        }

        return new PlaceBedAndSetSpawnTask().stayInBed();
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

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
    public boolean isFinished(BaritonePlus mod) {
        // We're in daytime
        int time = (int) (mod.getWorld().getTimeOfDay() % 24000);
        return !WorldHelper.canSleepOnServer() || 0 <= time && time < 13000;
    }

}
