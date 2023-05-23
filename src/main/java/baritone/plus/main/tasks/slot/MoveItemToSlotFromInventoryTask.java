package baritone.plus.main.tasks.slot;

import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.slots.Slot;

public class MoveItemToSlotFromInventoryTask extends MoveItemToSlotTask {
    public MoveItemToSlotFromInventoryTask(ItemTarget toMove, Slot destination) {
        super(toMove, destination, mod -> mod.getItemStorage().getSlotsWithItemPlayerInventory(false, toMove.getMatches()));
    }
}
