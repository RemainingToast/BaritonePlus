package baritone.plus.main.tasks.slot;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.helpers.LookHelper;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ClickType;

import java.util.Optional;

public class EnsureFreeInventorySlotTask extends Task {
    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
        if (cursorStack.isEmpty()) {
            if (garbage.isPresent()) {
                mod.getSlotHandler().clickSlot(garbage.get(), 0, ClickType.PICKUP);
                return null;
            }
        }
        if (!cursorStack.isEmpty()) {
            LookHelper.randomOrientation(mod);
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ClickType.PICKUP);
            return null;
        }
        setDebugState("All items are protected.");
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task obj) {
        return obj instanceof EnsureFreeInventorySlotTask;
    }

    @Override
    protected String toDebugString() {
        return "Ensuring inventory is free";
    }
}
