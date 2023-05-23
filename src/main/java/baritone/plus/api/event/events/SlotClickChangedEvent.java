package baritone.plus.api.event.events;

import baritone.plus.api.util.slots.Slot;
import net.minecraft.item.ItemStack;

public class SlotClickChangedEvent {
    public Slot slot;
    public ItemStack before;
    public ItemStack after;

    public SlotClickChangedEvent(Slot slot, ItemStack before, ItemStack after) {
        this.slot = slot;
        this.before = before;
        this.after = after;
    }
}
