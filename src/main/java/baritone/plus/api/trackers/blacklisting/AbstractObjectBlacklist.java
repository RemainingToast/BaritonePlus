package baritone.plus.api.trackers.blacklisting;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.StorageHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

/**
 * Sometimes we will try to access something and fail TOO many times.
 * <p>
 * This lets us know that a block is unreachable, and will ignore it from the search intelligently.
 */
public abstract class AbstractObjectBlacklist<T> {

    private final HashMap<T, BlacklistEntry> _entries = new HashMap<>();

    public void blackListItem(BaritonePlus mod, T item, int numberOfFailuresAllowed) {
        if (!_entries.containsKey(item)) {
            BlacklistEntry entry = new BlacklistEntry();
            entry.numberOfFailuresAllowed = numberOfFailuresAllowed;
            entry.numberOfFailures = 0;
            entry.bestDistanceSq = Double.POSITIVE_INFINITY;
            entry.bestTool = MiningRequirement.HAND;
            _entries.put(item, entry);
        }
        BlacklistEntry entry = _entries.get(item);
        double newDistance = getPos(item).squaredDistanceTo(mod.getPlayer().getPos());
        MiningRequirement newTool = StorageHelper.getCurrentMiningRequirement(mod);
        // For distance, add a slight threshold so it doesn't reset EVERY time we move a tiny bit closer.
        if (newTool.ordinal() > entry.bestTool.ordinal() || (newDistance < entry.bestDistanceSq - 1)) {
            if (newTool.ordinal() > entry.bestTool.ordinal()) entry.bestTool = newTool;
            if (newDistance < entry.bestDistanceSq) entry.bestDistanceSq = newDistance;
            entry.numberOfFailures = 0;
            Debug.logInternal("Blacklist RESET: " + item.toString());
        }
        entry.numberOfFailures++;
        entry.numberOfFailuresAllowed = numberOfFailuresAllowed;
        Debug.logInternal("Blacklist: " + item.toString() + ": Try " + entry.numberOfFailures + " / " + entry.numberOfFailuresAllowed);
    }

    protected abstract Vec3d getPos(T item);

    public boolean unreachable(T item) {
        if (_entries.containsKey(item)) {
            BlacklistEntry entry = _entries.get(item);
            return entry.numberOfFailures > entry.numberOfFailuresAllowed;
        }
        return false;
    }

    public void clear() {
        _entries.clear();
    }

    // Key: BlockPos
    private static class BlacklistEntry {
        public int numberOfFailuresAllowed;
        public int numberOfFailures;
        public double bestDistanceSq;
        public MiningRequirement bestTool;
    }
}
