package baritone.plus.main.tasks;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.slot.EnsureFreePlayerCraftingGridTask;
import baritone.plus.main.tasks.slot.ReceiveCraftingOutputSlotTask;
import baritone.plus.api.tasks.ITaskUsesCraftingGrid;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.RecipeTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.CraftingTableSlot;
import baritone.plus.api.util.slots.PlayerSlot;
import baritone.plus.api.util.slots.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ClickType;

import java.util.Optional;

public class CraftGenericWithRecipeBooksTask extends Task implements ITaskUsesCraftingGrid {

    private final RecipeTarget _target;

    public CraftGenericWithRecipeBooksTask(RecipeTarget target) {
        _target = target;
    }

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        boolean bigCrafting = StorageHelper.isBigCraftingOpen();
        if (!bigCrafting && !StorageHelper.isPlayerInventoryOpen()) {
            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
            if (!cursorStack.isEmpty()) {
                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                if (moveTo.isPresent()) {
                    mod.getSlotHandler().clickSlot(moveTo.get(), 0, ClickType.PICKUP);
                    return null;
                }
                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ClickType.PICKUP);
                    return null;
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                // Try throwing away cursor slot if it's garbage
                if (garbage.isPresent()) {
                    mod.getSlotHandler().clickSlot(garbage.get(), 0, ClickType.PICKUP);
                    return null;
                }
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ClickType.PICKUP);
            } else {
                StorageHelper.closeScreen();
            }
        }
        Slot outputSlot = bigCrafting ? CraftingTableSlot.OUTPUT_SLOT : PlayerSlot.CRAFT_OUTPUT_SLOT;
        ItemStack output = StorageHelper.getItemStackInSlot(outputSlot);
        if (_target.getOutputItem() == output.getItem() && mod.getItemStorage().getItemCount(_target.getOutputItem()) <
                _target.getTargetCount()) {
            setDebugState("Getting output.");
            return new ReceiveCraftingOutputSlotTask(outputSlot, _target.getTargetCount());
        }
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            if (moveTo.isPresent()) {
                mod.getSlotHandler().clickSlot(moveTo.get(), 0, ClickType.PICKUP);
                return null;
            }
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ClickType.PICKUP);
                return null;
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
            if (garbage.isPresent()) {
                mod.getSlotHandler().clickSlot(garbage.get(), 0, ClickType.PICKUP);
                return null;
            }
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ClickType.PICKUP);
            return null;
        }
        if (!bigCrafting) {
            PlayerSlot[] playerInputSlot = PlayerSlot.CRAFT_INPUT_SLOTS;
            for (PlayerSlot PlayerInputSlot : playerInputSlot) {
                ItemStack playerInput = StorageHelper.getItemStackInSlot(PlayerInputSlot);
                if (!playerInput.isEmpty()) {
                    return new EnsureFreePlayerCraftingGridTask();
                }
            }
        }
        setDebugState("Crafting.");
        if (mod.getSlotHandler().canDoSlotAction()) {
            StorageHelper.instantFillRecipeViaBook(mod, _target.getRecipe(), _target.getOutputItem(), true);
            mod.getSlotHandler().registerSlotAction();
        }
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CraftGenericWithRecipeBooksTask task) {
            return task._target.equals(_target);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Crafting (w/ RECIPE): " + _target;
    }
}
