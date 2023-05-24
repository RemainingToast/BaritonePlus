package baritone.plus.main.tasks.container;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.Subscription;
import baritone.plus.api.event.events.SlotClickChangedEvent;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

public class ContainerStoredTracker {
    private final HashMap<Item, Integer> _totalDeposited = new HashMap<>();
    private final Predicate<Slot> _acceptDeposit;

    private Subscription<SlotClickChangedEvent> _slotClickChangedSubscription;

    public ContainerStoredTracker(Predicate<Slot> acceptDeposit) {
        _acceptDeposit = acceptDeposit;
    }

    private void trackChange(Item item, int delta) {
        _totalDeposited.put(item, _totalDeposited.getOrDefault(item, 0) + delta);
    }

    public void startTracking() {
        _slotClickChangedSubscription = EventBus.subscribe(SlotClickChangedEvent.class, evt -> {
            Slot slot = evt.slot;
            if (!slot.isSlotInPlayerInventory() && _acceptDeposit.test(slot)) {
                ItemStack before = evt.before;
                ItemStack after = evt.after;
                if (before.getItem() != after.getItem()) {
                    // Before has been replaced! We lost before and added all of after.
                    if (!before.isEmpty())
                        trackChange(before.getItem(), -1 * before.getCount());
                    if (!after.isEmpty())
                        trackChange(after.getItem(), after.getCount());
                } else {
                    // Before and after are the same, track the difference.
                    trackChange(after.getItem(), after.getCount() - before.getCount());
                }
            }
        });
    }

    public void stopTracking() {
        EventBus.unsubscribe(_slotClickChangedSubscription);
    }

    /**
     * How many items have been ADDED to containers satisfying our conditions?
     */
    public int getStoredCount(Item... items) {
        int result = 0;
        for (Item item : items) {
            result += _totalDeposited.getOrDefault(item, 0);
        }
        return result;
    }

    public boolean matches(ItemTarget target) {
        return getStoredCount(target.getMatches()) >= target.getTargetCount();
    }

    public ItemTarget[] getUnstoredItemTargetsYouCanStore(BaritonePlus mod, ItemTarget[] toStore) {
        return Arrays.stream(toStore)
                .filter(target -> !matches(target) && mod.getItemStorage().hasItem(target.getMatches()))
                // If we don't have enough, reduce the count to what we CAN add
                .map(target -> mod.getItemStorage().getItemCount(target) < target.getTargetCount() ? new ItemTarget(target, mod.getItemStorage().getItemCount(target)) : target)
                .toArray(ItemTarget[]::new);
    }
}
