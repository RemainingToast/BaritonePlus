package adris.altoclef.baritone.brain.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.baritone.brain.BaritoneBrain;
import adris.altoclef.tasksystem.Task;

public class BrainTask extends Task {

    private BaritoneBrain brain = null;

    @Override
    protected void onStart(AltoClef mod) {
        brain = mod.getBaritoneBrain();
        brain.updateWorldState();
        setDebugState("Thinking...");
//        Debug.logMessage(brain.chatGPT.generateTask(brain.getWorldState()));
    }

    @Override
    protected Task onTick(AltoClef mod) {
        return brain.process(this);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        brain.onStop(mod, interruptTask);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BrainTask;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return super.isFinished(mod);
    }

    @Override
    protected String toDebugString() {
        return "Thinking with Chat GPT...";
    }

    @Override
    public void setDebugState(String state) {
        super.setDebugState(state);
    }
}
