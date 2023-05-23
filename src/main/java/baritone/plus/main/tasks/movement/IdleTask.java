package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Playground;
import baritone.plus.api.tasks.Task;

/**
 * Do nothing.
 */
public class IdleTask extends Task {
    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        // Do nothing except maybe test code
        Playground.IDLE_TEST_TICK_FUNCTION(mod);
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        // Never finish
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IdleTask;
    }

    @Override
    protected String toDebugString() {
        return "Idle";
    }
}
