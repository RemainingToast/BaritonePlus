package baritone.plus.api.trackers;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.InteractWithBlockTask;

import java.util.ArrayList;

public class TrackerManager {

    private final ArrayList<Tracker> _trackers = new ArrayList<>();

    private final BaritonePlus _mod;

    private boolean _wasInGame = false;

    public TrackerManager(BaritonePlus mod) {
        _mod = mod;
    }

    public void tick() {
        boolean inGame = BaritonePlus.inGame();
        if (!inGame && _wasInGame) {
            // Reset when we leave our world
            for (Tracker tracker : _trackers) {
                tracker.reset();
            }
            // This is a a spaghetti. Fix at some point.
            _mod.getChunkTracker().reset(_mod);
            _mod.getMiscBlockTracker().reset();

            var _tts = _mod.getBaritoneBrain().getTTS();
            if (_tts != null) {
                _tts.clearDialogue();
            }
        }
        _wasInGame = inGame;

        for (Tracker tracker : _trackers) {
            tracker.setDirty();
        }
    }

    public void addTracker(Tracker tracker) {
        tracker._mod = _mod;
        _trackers.add(tracker);
    }
}
