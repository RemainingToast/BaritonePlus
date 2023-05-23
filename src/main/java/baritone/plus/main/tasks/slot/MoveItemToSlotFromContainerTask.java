package baritone.plus.main.tasks.slot;

import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.slots.Slot;

public class MoveItemToSlotFromContainerTask extends MoveItemToSlotTask {
    public MoveItemToSlotFromContainerTask(ItemTarget toMove, Slot destination) {
        super(toMove, destination, mod -> mod.getItemStorage().getSlotsWithItemContainer(toMove.getMatches()));
    }
}
