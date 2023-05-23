package baritone.plus.api.trackers;

import baritone.plus.main.BaritonePlus;

public abstract class Tracker {

    protected BaritonePlus _mod;
    // Needs to update
    private boolean _dirty = true;

    public Tracker(TrackerManager manager) {
        manager.addTracker(this);
    }

    public void setDirty() {
        _dirty = true;
    }

    // Virtual
    protected boolean isDirty() {
        return _dirty;
    }

    protected void ensureUpdated() {
        if (isDirty()) {
            updateState();
            _dirty = false;
        }
    }

    protected abstract void updateState();

    protected abstract void reset();
}
