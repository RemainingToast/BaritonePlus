package baritone.plus.main.tasks.container;

import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.RecipeTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.PlayerSlot;
import baritone.plus.api.util.slots.Slot;
import baritone.plus.api.util.time.TimerGame;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.tasks.CraftGenericManuallyTask;
import baritone.plus.main.tasks.CraftGenericWithRecipeBooksTask;
import baritone.plus.main.tasks.CraftInInventoryTask;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.construction.DestroyBlockTask;
import baritone.plus.main.tasks.movement.PickupDroppedItemTask;
import baritone.plus.main.tasks.movement.TimeoutWanderTask;
import baritone.plus.main.tasks.resources.CollectRecipeCataloguedResourcesTask;
import baritone.plus.main.tasks.slot.EnsureFreeInventorySlotTask;
import baritone.plus.main.tasks.slot.MoveInaccessibleItemToInventoryTask;
import baritone.plus.main.tasks.slot.ReceiveCraftingOutputSlotTask;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Crafts an item in a crafting table, obtaining and placing the table down if none was found.
 */
// TODO - Should pick Crafting Table back up when complete
public class CraftInTableTask extends ResourceTask {

    private final RecipeTarget[] _targets;

    private final DoCraftInTableTask _craftTask;

    public CraftInTableTask(RecipeTarget[] targets) {
        super(extractItemTargets(targets));
        _targets = targets;
        _craftTask = new DoCraftInTableTask(_targets);
    }

    public CraftInTableTask(RecipeTarget target, boolean collect, boolean ignoreUncataloguedSlots) {
        super(new ItemTarget(target.getOutputItem(), target.getTargetCount()));
        _targets = new RecipeTarget[]{target};
        _craftTask = new DoCraftInTableTask(_targets, collect, ignoreUncataloguedSlots);
    }

    public CraftInTableTask(RecipeTarget target) {
        this(target, true, true);
    }

    private static ItemTarget[] extractItemTargets(RecipeTarget[] recipeTargets) {
        List<ItemTarget> result = new ArrayList<>(recipeTargets.length);
        for (RecipeTarget target : recipeTargets) {
            result.add(new ItemTarget(target.getOutputItem(), target.getTargetCount()));
        }
        return result.toArray(ItemTarget[]::new);
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {

    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {
        if (!_craftTask.isFinished(mod)) {
            return _craftTask;
        }
        // Pickup Crafting Table when we finish
        Optional<BlockPos> _nearestTable = mod.getBlockTracker().getNearestTracking(Blocks.CRAFTING_TABLE);
        Optional<ItemEntity> _nearestDropped = mod.getEntityTracker().getClosestItemDrop(Items.CRAFTING_TABLE);
        if (!mod.getItemStorage().hasItem(Items.CRAFTING_TABLE)) {
            setDebugState("Picking Crafting Table Backup.");
            if (!mod.getItemStorage().hasEmptyInventorySlot()) {
                return new EnsureFreeInventorySlotTask();
            }
            if (_nearestTable.isPresent()
                    && _nearestTable.get().isWithinDistance(mod.getPlayer().getPos(), 10)) {
                return new DestroyBlockTask(_nearestTable.get());
            } else if (_nearestDropped.isPresent()
                    && _nearestDropped.get().distanceTo(mod.getPlayer()) <= 15
            ) {
                return new PickupDroppedItemTask(Items.CRAFTING_TABLE, 1);
            }
        }
        return null;
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CraftInTableTask task) {
            return _craftTask.isEqual(task._craftTask);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return _craftTask.toDebugString();
    }

    public RecipeTarget[] getRecipeTargets() {
        return _targets;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return _craftTask.isFinished(mod) && mod.getItemStorage().hasItem(Items.CRAFTING_TABLE);
    }
}


class DoCraftInTableTask extends DoStuffInContainerTask {

    private final float CRAFT_RESET_TIMER_BONUS_SECONDS = 10;

    private final RecipeTarget[] _targets;

    private final boolean _collect;

    private final CollectRecipeCataloguedResourcesTask _collectTask;
    private final TimerGame _craftResetTimer = new TimerGame(CRAFT_RESET_TIMER_BONUS_SECONDS);
    private int _craftCount;

    public DoCraftInTableTask(RecipeTarget[] targets, boolean collect, boolean ignoreUncataloguedSlots) {
        super(Blocks.CRAFTING_TABLE, new ItemTarget("crafting_table"));
        _collectTask = new CollectRecipeCataloguedResourcesTask(ignoreUncataloguedSlots, targets);
        _targets = targets;
        _collect = collect;
    }

    public DoCraftInTableTask(RecipeTarget[] targets) {
        this(targets, true, false);
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        super.onStart(mod);
        mod.getBehaviour().push();
        _craftCount = 0;
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
        _collectTask.reset();
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
        super.onStop(mod, interruptTask);
        mod.getBehaviour().pop();
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        mod.getBehaviour().addProtectedItems(getMaterialsArray());
        List<BlockPos> craftingTablePos = mod.getBlockTracker().getKnownLocations(Blocks.CRAFTING_TABLE);
        if (!craftingTablePos.isEmpty() && mod.getItemStorage().hasItem(Items.CRAFTING_TABLE)) {
            for (BlockPos CraftingTablePos : craftingTablePos) {
                mod.getBehaviour().avoidBlockBreaking(CraftingTablePos);
            }
        }
        // TODO: This shouldn't be here.
        // This is duct tape for the following scenario:
        //
        //      The Collect Recipe Resources task does NOT actually grab all of the resources we "claim" to need.
        //      It will finish while we STILL need resources.
        //
        //
        //      When is this OK?
        //
        //      Only if we ASSUME that hasRecipeMaterials is TOO STRICT and the Collect Task is CORRECT.
        //

        // Grab from output FIRST
        if (StorageHelper.isPlayerInventoryOpen()) {
            if (StorageHelper.getItemStackInCursorSlot().isEmpty()) {
                Item outputItem = StorageHelper.getItemStackInSlot(PlayerSlot.CRAFT_OUTPUT_SLOT).getItem();
                for (RecipeTarget target : _targets) {
                    if (target.getOutputItem() == outputItem && mod.getItemStorage().getItemCount(target.getOutputItem()) < target.getTargetCount()) {
                        return new ReceiveCraftingOutputSlotTask(PlayerSlot.CRAFT_OUTPUT_SLOT, target.getTargetCount());
                    }
                }
            }
        }

        if (_collect) {
            if (!_collectTask.isFinished(mod)) {
                if (!StorageHelper.hasRecipeMaterialsOrTarget(mod, _targets)) {
                    setDebugState("Getting recipe materials: " + Arrays.toString(_targets));
                    return _collectTask;
                }
            }
        }

        if (!isContainerOpen(mod)) {
            _craftResetTimer.reset();
        }

        // Make sure our recipe items are accessible in our inventory
        if (!thisOrChildSatisfies(task -> task instanceof CraftInInventoryTask)) {
            for (RecipeTarget target : _targets) {
                for (int slot = 0; slot < target.getRecipe().getSlotCount(); ++slot) {
                    ItemTarget toCheck = target.getRecipe().getSlot(slot);
                    if (StorageHelper.isItemInaccessibleToContainer(mod, toCheck)) {
                        return new MoveInaccessibleItemToInventoryTask(toCheck);
                    }
                }
            }
        }

        return super.onTick(mod);
    }

    @Override
    protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
        if (other instanceof DoCraftInTableTask task) {
            return Arrays.equals(task._targets, _targets);
        }
        return false;
    }

    @Override
    public boolean isContainerOpen(BaritonePlus mod) {
        return (mod.getPlayer().currentScreenHandler instanceof CraftingScreenHandler);
    }

    @Override
    protected Task containerSubTask(BaritonePlus mod) {
        // Refresh crafting table Juuust in case
        _craftResetTimer.setInterval(mod.getModSettings().getContainerItemMoveDelay() * 10 + CRAFT_RESET_TIMER_BONUS_SECONDS);
        if (_craftResetTimer.elapsed()) {
            Debug.logMessage("Refreshing crafting table.");
            return new TimeoutWanderTask(5);
        }

        for (RecipeTarget target : _targets) {
            if (mod.getItemStorage().getItemCount(target.getOutputItem()) >= target.getTargetCount())
                continue;
            // No need to free, handled automatically I believe.
            setDebugState("Crafting");

            return mod.getModSettings().shouldUseCraftingBookToCraft()
                    ? new CraftGenericWithRecipeBooksTask(target)
                    : new CraftGenericManuallyTask(target);
        }

        setDebugState("DONE? Shouldn't be here");
        return null;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return _craftCount >= _targets.length;//_crafted;
    }

    @Override
    protected double getCostToMakeNew(BaritonePlus mod) {
        Optional<BlockPos> closestCraftingTable = mod.getBlockTracker().getNearestTracking(Blocks.CRAFTING_TABLE);
        if (closestCraftingTable.isPresent()) {
            if (closestCraftingTable.get().isWithinDistance(mod.getPlayer().getPos(), 40)) {
                return Double.POSITIVE_INFINITY;
            }
        }
        // If we have an axe, lower the cost.
        boolean axe = mod.getItemStorage().hasItem(ItemHelper.TOOLS_AXES);
        if (axe || mod.getItemStorage().hasItem(ItemHelper.LOG) || mod.getItemStorage().getItemCount(ItemHelper.PLANKS) >= 4) {
            return 10;
        }
        // TODO: If cached and the closest log is really far away, strike the price UP
        return 100;
    }

    private Item[] getMaterialsArray() {
        List<Item> result = new ArrayList<>();
        for (RecipeTarget target : _targets) {
            for (int i = 0; i < target.getRecipe().getSlotCount(); ++i) {
                ItemTarget materialTarget = target.getRecipe().getSlot(i);
                if (materialTarget == null || materialTarget.getMatches() == null) continue;
                Collections.addAll(result, materialTarget.getMatches());
            }
        }

        return result.toArray(Item[]::new);
    }

}
