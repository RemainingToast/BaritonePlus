package baritone.plus.api.tasks;

import baritone.plus.main.BaritonePlus;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class TaskChain {

    private final List<Task> _cachedTaskChain = new ArrayList<>();
    @Getter
    private boolean paused = false;

    public TaskChain(TaskRunner runner) {
        runner.addTaskChain(this);
    }

    public void tick(BaritonePlus mod) {
        if (paused) {
            return; // return if paused
        }
        _cachedTaskChain.clear();
        onTick(mod);
    }

    public void stop(BaritonePlus mod) {
        _cachedTaskChain.clear();
        onStop(mod);
    }

    // TODO - onPause/onUnpause I guess
    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    protected abstract void onStop(BaritonePlus mod);

    public abstract void onInterrupt(BaritonePlus mod, TaskChain other);

    protected abstract void onTick(BaritonePlus mod);

    public abstract float getPriority(BaritonePlus mod);

    public abstract boolean isActive();

    public abstract String getName();

    public List<Task> getTasks() {
        return _cachedTaskChain;
    }

    void addTaskToChain(Task task) {
        _cachedTaskChain.add(task);
    }

    public String toString() {
        return getName();
    }

}
