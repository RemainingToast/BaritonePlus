package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.item.Item;

import java.util.function.Function;

public class CraftWithMatchingPlanksTask extends CraftWithMatchingMaterialsTask {

    private final ItemTarget _visualTarget;
    private final Function<ItemHelper.WoodItems, Item> _getTargetItem;

    public CraftWithMatchingPlanksTask(Item[] validTargets, Function<ItemHelper.WoodItems, Item> getTargetItem, CraftingRecipe recipe, boolean[] sameMask, int count) {
        super(new ItemTarget(validTargets, count), recipe, sameMask);
        _getTargetItem = getTargetItem;
        _visualTarget = new ItemTarget(validTargets, count);
    }


    @Override
    protected int getExpectedTotalCountOfSameItem(BaritonePlus mod, Item sameItem) {
        // Include logs
        return mod.getItemStorage().getItemCount(sameItem) + mod.getItemStorage().getItemCount(ItemHelper.planksToLog(sameItem)) * 4;
    }

    @Override
    protected Task getSpecificSameResourceTask(BaritonePlus mod, Item[] toGet) {
        for (Item plankToGet : toGet) {
            Item log = ItemHelper.planksToLog(plankToGet);
            // Convert logs to planks
            if (mod.getItemStorage().getItemCount(log) >= 1) {
                return TaskCatalogue.getItemTask(plankToGet, 1);//new CraftInInventoryTask(new ItemTarget(plankToGet, 1), CraftingRecipe.newShapedRecipe("planks", new ItemTarget[]{new ItemTarget(log, 1), empty, empty, empty}, 4), false, true);
            }
        }
        Debug.logError("CraftWithMatchingPlanks: Should never happen!");
        return null;
    }

    @Override
    protected Item getSpecificItemCorrespondingToMajorityResource(Item majority) {
        for (ItemHelper.WoodItems woodItems : ItemHelper.getWoodItems()) {
            if (woodItems.planks == majority) {
                return _getTargetItem.apply(woodItems);
            }
        }
        return null;
    }


    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CraftWithMatchingPlanksTask task) {
            return task._visualTarget.equals(_visualTarget);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Crafting: " + _visualTarget;
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
        return null;
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {

    }

}
