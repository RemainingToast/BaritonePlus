package adris.altoclef.tasksystem;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.examples.ExampleTask2;
import adris.altoclef.tasks.movement.IdleTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.resources.CollectMeatTask;
import adris.altoclef.tasks.resources.CollectPlanksTask;
import adris.altoclef.tasks.resources.MineAndCollectTask;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.ItemHelper;
import org.apache.commons.lang3.RandomUtils;

import java.util.function.Predicate;

public abstract class Task {

    private String _oldDebugState = "";
    private String _debugState = "";

    private Task _sub = null;

    private boolean _first = true;

    private boolean _stopped = false;

    private boolean _active = false;

    public void tick(AltoClef mod, TaskChain parentChain) {
        parentChain.addTaskToChain(this);
        if (_first) {
            var tts = mod.getBaritoneBrain().getTTS();
            if (tts != null) {
                if (!(this instanceof DestroyBlockTask || this instanceof InteractWithBlockTask))
                    tts.narrateTask(this);
            }

            Debug.logInternal("Task START: " + this);
            _active = true;
            onStart(mod);
            _first = false;
            _stopped = false;
        }
        if (_stopped) return;

        Task newSub = onTick(mod);
        // Debug state print
        if (!_oldDebugState.equals(_debugState)) {
            Debug.logInternal(toString());
            _oldDebugState = _debugState;
        }
        // We have a sub task
        if (newSub != null) {
            if (!newSub.isEqual(_sub)) {
                if (canBeInterrupted(mod, _sub, newSub)) {
                    // Our sub task is new
                    if (_sub != null) {
                        // Our previous sub must be interrupted.
                        _sub.stop(mod, newSub);
                    }

                    _sub = newSub;
                }
            }

            // Run our child
            _sub.tick(mod, parentChain);
        } else {
            // We are null
            if (_sub != null && canBeInterrupted(mod, _sub, null)) {
                // Our previous sub must be interrupted.
                _sub.stop(mod);
                _sub = null;
            }
        }
    }

    public void reset() {
        _first = true;
        _active = false;
        _stopped = false;
    }

    public void stop(AltoClef mod) {
        stop(mod, null);
    }

    /**
     * Stops the task. Next time it's run it will run `onStart`
     */
    public void stop(AltoClef mod, Task interruptTask) {
        if (!_active) return;

        var tts = mod.getBaritoneBrain().getTTS();
        if (tts != null) {
            if (!(this instanceof DestroyBlockTask || this instanceof InteractWithBlockTask))
                tts.removeTask(this);
        }

        Debug.logInternal("Task STOP: " + this + ", interrupted by " + interruptTask);
        if (!_first) {
            onStop(mod, interruptTask);
        }

        if (_sub != null && !_sub.stopped()) {
            _sub.stop(mod, interruptTask);
        }

        _first = true;
        _active = false;
        _stopped = true;
    }

    /**
     * Lets the task know it's execution has been "suspended"
     * <p>
     * STILL RUNS `onStop`
     * <p>
     * Doesn't stop it all-together (meaning `isActive` still returns true)
     */
    public void interrupt(AltoClef mod, Task interruptTask) {
        if (!_active) return;
        if (!_first) {
            onStop(mod, interruptTask);
        }

        if (_sub != null && !_sub.stopped()) {
            _sub.interrupt(mod, interruptTask);
        }

        _first = true;
    }

    protected void setDebugState(String state) {
        if (state == null) {
            state = "";
        }
        _debugState = state;
    }

    // Virtual
    public boolean isFinished(AltoClef mod) {
        return false;
    }

    public boolean isActive() {
        return _active;
    }

    public boolean stopped() {
        return _stopped;
    }

    protected abstract void onStart(AltoClef mod);

    protected abstract Task onTick(AltoClef mod);

    // interruptTask = null if the task stopped cleanly
    protected abstract void onStop(AltoClef mod, Task interruptTask);

    protected abstract boolean isEqual(Task other);

    protected abstract String toDebugString();

    @Override
    public String toString() {
        return "<" + toDebugString() + "> " + _debugState;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task task) {
            return isEqual(task);
        }
        return false;
    }

    public boolean thisOrChildSatisfies(Predicate<Task> pred) {
        Task t = this;
        while (t != null) {
            if (pred.test(t)) return true;
            t = t._sub;
        }
        return false;
    }

    public boolean thisOrChildAreTimedOut() {
        return thisOrChildSatisfies(task -> task instanceof TimeoutWanderTask);
    }

    /**
     * Sometimes a task just can NOT be bothered to be interrupted right now.
     * For instance, if we're in mid air and MUST complete the parkour movement.
     */
    private boolean canBeInterrupted(AltoClef mod, Task subTask, Task toInterruptWith) {
        if (subTask == null) return true;
        // Our task can declare that is FORCES itself to be active NOW.
        return (subTask.thisOrChildSatisfies(task -> {
            if (task instanceof ITaskCanForce canForce) {
                return !canForce.shouldForce(mod, toInterruptWith);
            }
            return true;
        }));
    }

    public String getDebugState() {
        return _debugState;
    }

    public static Task fromString(String string) {
        if (string.contains("food")) {
            return new CollectMeatTask(RandomUtils.nextDouble(1, 64));
        }

        if (string.contains("wood")) {
            return new CollectPlanksTask(RandomUtils.nextInt(1, 64));
        }

        return new ExampleTask2();
    }
}
