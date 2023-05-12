package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

public class SleepThroughNightTask extends Task {

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (isServerCheck(mod)) {
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
        return isServerCheck(mod) || 0 <= time && time < 13000;
    }

    private boolean isServerCheck(AltoClef mod) {
        if (!mod.getWorld().isClient()) {
            var server = mod.getWorld().getServer();
            if (server != null) {
                int i = mod.getWorld().getServer().getCurrentPlayerCount();
                return i > 1;
            }
        }
        return false;
    }
}
