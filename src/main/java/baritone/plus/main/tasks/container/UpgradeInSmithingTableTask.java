package baritone.plus.main.tasks.container;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.slot.EnsureFreeInventorySlotTask;
import baritone.plus.main.tasks.slot.MoveItemToSlotFromInventoryTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.PlayerSlot;
import baritone.plus.api.util.slots.Slot;
import baritone.plus.api.util.slots.SmithingTableSlot;
import baritone.plus.api.util.time.TimerGame;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Optional;

public class UpgradeInSmithingTableTask extends ResourceTask {

    private final ItemTarget _tool;
    private final ItemTarget _material;
    private final ItemTarget _output;

    private final Task _innerTask;

    public UpgradeInSmithingTableTask(ItemTarget tool, ItemTarget material, ItemTarget output) {
        super(output);
        _tool = new ItemTarget(tool, output.getTargetCount());
        _material = new ItemTarget(material, output.getTargetCount());
        _output = output;
        _innerTask = new UpgradeInSmithingTableInternalTask();
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {
    }

    private int getItemsInSlot(Slot slot, ItemTarget match) {
        ItemStack stack = StorageHelper.getItemStackInSlot(slot);
        if (!stack.isEmpty() && match.matches(stack.getItem())) {
            return stack.getCount();
        }
        return 0;
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {
        // if we don't have tools + materials, get them.

        boolean inSmithingTable = (mod.getPlayer().currentScreenHandler instanceof SmithingScreenHandler);

        int materialsInSlot = inSmithingTable ? getItemsInSlot(SmithingTableSlot.INPUT_SLOT_MATERIALS, _material) : 0;
        int toolsInSlot = inSmithingTable ? getItemsInSlot(SmithingTableSlot.INPUT_SLOT_TOOL, _tool) : 0;
        int ouputInSlot = inSmithingTable ? getItemsInSlot(SmithingTableSlot.OUTPUT_SLOT, _output) : 0;

        int desiredOutput = _output.getTargetCount() - ouputInSlot;

        if (mod.getItemStorage().getItemCount(_tool) + toolsInSlot < desiredOutput ||
                mod.getItemStorage().getItemCount(_material) + materialsInSlot < desiredOutput) {
            setDebugState("Getting materials + tools");
            return TaskCatalogue.getSquashedItemTask(_tool, _material);
        }

        // Edge case: We are wearing the armor we want to upgrade. If so, remove it.
        if (StorageHelper.isArmorEquipped(mod, _tool.getMatches())) {
            // Exit out of any screen so we can move our armor
            if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
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
                setDebugState("Quickly removing equipped armor");
                return null;
            }
            // Take off our armor
            if (mod.getItemStorage().hasEmptyInventorySlot()) {
                return new EnsureFreeInventorySlotTask();
            }
            for (Slot armorSlot : PlayerSlot.ARMOR_SLOTS) {
                if (_tool.matches(StorageHelper.getItemStackInSlot(armorSlot).getItem())) {
                    setDebugState("Quickly removing equipped armor");
                    mod.getSlotHandler().clickSlot(armorSlot, 0, SlotActionType.QUICK_MOVE);
                    return null;
                }
            }
        }

        setDebugState("Smithing...");
        return _innerTask;
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof UpgradeInSmithingTableTask task) {
            return task._tool.equals(_tool) && task._output.equals(_output) && task._material.equals(_material);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Upgrading " + _tool.toString() + " + " + _material.toString() + " -> " + _output.toString();
    }

    public ItemTarget getTools() {
        return _tool;
    }

    public ItemTarget getMaterials() {
        return _material;
    }

    private class UpgradeInSmithingTableInternalTask extends DoStuffInContainerTask {

        private final TimerGame _invTimer;

        public UpgradeInSmithingTableInternalTask() {
            super(Blocks.SMITHING_TABLE, new ItemTarget("smithing_table"));
            _invTimer = new TimerGame(0);
        }

        @Override
        protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
            // inner part, don't care
            return true;
        }

        @Override
        public boolean isContainerOpen(BaritonePlus mod) {
            return (mod.getPlayer().currentScreenHandler instanceof SmithingScreenHandler);
        }

        @Override
        protected Task containerSubTask(BaritonePlus mod) {
            setDebugState("Smithing...");
            // We have our tools + materials. Now, do the thing.
            _invTimer.setInterval(mod.getModSettings().getContainerItemMoveDelay());

            // Run once every
            if (!_invTimer.elapsed()) {
                return null;
            }
            _invTimer.reset();

            Slot materialSlot = SmithingTableSlot.INPUT_SLOT_MATERIALS;
            Slot toolSlot = SmithingTableSlot.INPUT_SLOT_TOOL;
            Slot outputSlot = SmithingTableSlot.OUTPUT_SLOT;

            ItemStack currentMaterials = StorageHelper.getItemStackInSlot(materialSlot);
            ItemStack currentTools = StorageHelper.getItemStackInSlot(toolSlot);
            ItemStack currentOutput = StorageHelper.getItemStackInSlot(outputSlot);
            // Grab from output
            if (!currentOutput.isEmpty()) {
                mod.getSlotHandler().clickSlot(outputSlot, 0, SlotActionType.QUICK_MOVE);
                return null;
            }
            // Put materials in slot
            if (currentMaterials.isEmpty() || !_material.matches(currentMaterials.getItem())) {
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(_material, 1), materialSlot);
            }
            // Put tool in slot
            if (currentTools.isEmpty() || !_tool.matches(currentTools.getItem())) {
                return new MoveItemToSlotFromInventoryTask(new ItemTarget(_tool, 1), toolSlot);
            }

            setDebugState("PROBLEM: Nothing to do!");
            return null;
        }

        @Override
        protected double getCostToMakeNew(BaritonePlus mod) {
            int price = 400;
            if (mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 4) {
                price -= 125;
            }
            if (mod.getItemStorage().getItemCount(Items.FLINT) >= 2) {
                price -= 125;
            }
            return price;
        }
    }

}
