package baritone.plus.main.tasks.slot;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.PlayerSlot;
import baritone.plus.api.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class EnsureFreePlayerCraftingGridTask extends Task {
    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        setDebugState("Clearing the 2x2 crafting grid");
        for (Slot slot : PlayerSlot.CRAFT_INPUT_SLOTS) {
            ItemStack items = StorageHelper.getItemStackInSlot(slot);
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
            if (!cursor.isEmpty()) {
                return new EnsureFreeCursorSlotTask();
            }
            if (!items.isEmpty()) {
                mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof EnsureFreePlayerCraftingGridTask;
    }

    @Override
    protected String toDebugString() {
        return "Breaking the crafting grid";
    }
}
