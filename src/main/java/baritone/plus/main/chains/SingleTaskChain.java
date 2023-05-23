package baritone.plus.main.chains;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.tasks.TaskChain;
import baritone.plus.api.tasks.TaskRunner;
import baritone.plus.api.util.time.Stopwatch;

public abstract class SingleTaskChain extends TaskChain {

    private final Stopwatch _taskStopwatch = new Stopwatch();
    protected Task _mainTask = null;
    private boolean _interrupted = false;

    private BaritonePlus _mod;

    public SingleTaskChain(TaskRunner runner) {
        super(runner);
        _mod = runner.getMod();
    }

    @Override
    protected void onTick(BaritonePlus mod) {
        if (!isActive()) return;

        if (_interrupted) {
            _interrupted = false;
            if (_mainTask != null) {
                _mainTask.reset();
            }
        }

        if (_mainTask != null) {
            if ((_mainTask.isFinished(mod)) || _mainTask.stopped()) {
                onTaskFinish(mod);
            } else {
                _mainTask.tick(mod, this);
            }
        }
    }

    protected void onStop(BaritonePlus mod) {
        if (isActive() && _mainTask != null) {
            _mainTask.stop(mod);
            _mainTask = null;
        }
    }

    public void setTask(Task task) {
        if (_mainTask == null || !_mainTask.equals(task)) {
            if (_mainTask != null) {
                _mainTask.stop(_mod, task);
            }
            _mainTask = task;
            if (task != null) task.reset();
        }
    }


    @Override
    public boolean isActive() {
        return _mainTask != null;
    }

    protected abstract void onTaskFinish(BaritonePlus mod);

    @Override
    public void onInterrupt(BaritonePlus mod, TaskChain other) {
        if (other != null) {
            Debug.logInternal("Chain Interrupted: " + this + " by " + other);
        }
        // Stop our task. When we're started up again, let our task know we need to run.
        _interrupted = true;
        if (_mainTask != null && _mainTask.isActive()) {
            _mainTask.interrupt(mod, null);
        }
    }

    protected boolean isCurrentlyRunning(BaritonePlus mod) {
        return !_interrupted && _mainTask.isActive() && !_mainTask.isFinished(mod);
    }

    public Task getCurrentTask() {
        return _mainTask;
    }
}
