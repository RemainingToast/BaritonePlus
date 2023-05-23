package baritone.plus.main.tasks.slot;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.CursorSlot;
import baritone.plus.api.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Objects;
import java.util.Optional;

public class MoveInaccessibleItemToInventoryTask extends Task {

    private final ItemTarget _target;

    public MoveInaccessibleItemToInventoryTask(ItemTarget target) {
        _target = target;
    }

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {

        // Ensure inventory is closed.
        if (!StorageHelper.isPlayerInventoryOpen()) {
            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
            if (!cursorStack.isEmpty()) {
                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                if (moveTo.isPresent()) {
                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, SlotActionType.PICKUP);
                    return null;
                }
                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                    return null;
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                // Try throwing away cursor slot if it's garbage
                if (garbage.isPresent()) {
                    mod.getSlotHandler().clickSlot(garbage.get(), 0, SlotActionType.PICKUP);
                    return null;
                }
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            } else {
                StorageHelper.closeScreen();
            }
            setDebugState("Closing screen first (hope this doesn't get spammed a million times)");
            return null;
        }

        Optional<Slot> slotToMove = StorageHelper.getFilledInventorySlotInaccessibleToContainer(mod, _target);
        if (slotToMove.isPresent()) {
            // Force cursor slot if we have one.
            if (_target.matches(StorageHelper.getItemStackInCursorSlot().getItem())) {
                slotToMove = Optional.of(CursorSlot.SLOT);
            }
            // issue is a full cursor slot when trying to clear out bad items.
            // solution: ensure cursor is empty first
            if (!StorageHelper.getItemStackInCursorSlot().isEmpty()) {
                return new EnsureFreeCursorSlotTask();
            }

            Slot toMove = slotToMove.get();
            ItemStack stack = StorageHelper.getItemStackInSlot(toMove);
            Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(stack, false);
            if (toMoveTo.isPresent()) {
                setDebugState("Moving slot " + toMove + " to inventory");
                // Pick up & move
                if (Slot.isCursor(toMove)) {
                    mod.getSlotHandler().clickSlot(toMoveTo.get(), 0, SlotActionType.PICKUP);
                } else {
                    mod.getSlotHandler().clickSlot(toMove, 0, SlotActionType.PICKUP);
                }
                return null;
            } else {
                setDebugState("Free up inventory first.");
                // Make it free first.
                return new EnsureFreeInventorySlotTask();
            }
        }
        setDebugState("NONE FOUND");
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof MoveInaccessibleItemToInventoryTask task) {
            return Objects.equals(task._target, _target);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Making item accessible: " + _target;
    }
}
