package baritone.plus.brain.tasks;

import baritone.plus.main.BaritonePlus;
import baritone.plus.brain.BaritoneBrain;
import baritone.plus.api.tasks.Task;

public class BrainTask extends Task {

    private BaritoneBrain brain = null;

    @Override
    protected void onStart(BaritonePlus mod) {
        brain = mod.getBaritoneBrain();
        brain.updateWorldState();
        setDebugState("Thinking...");
//        Debug.logMessage(brain.chatGPT.generateTask(brain.getWorldState()));
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        return brain.process(this);
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        brain.onStop(mod, interruptTask);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BrainTask;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
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
