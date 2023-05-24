package baritone.plus.main.tasks.resources.wood;

import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.resources.CraftWithMatchingPlanksTask;
import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.item.Item;

public class CollectWoodenTrapDoorTask extends CraftWithMatchingPlanksTask {

    public CollectWoodenTrapDoorTask(Item[] targets, ItemTarget planks, int count) {
        super(targets, woodItems -> woodItems.trapdoor, createRecipe(planks), new boolean[]{true, true, true, true, true, true, false, false, false}, count);
    }

    public CollectWoodenTrapDoorTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    public CollectWoodenTrapDoorTask(int count) {
        this(ItemHelper.WOOD_TRAPDOOR, TaskCatalogue.getItemTarget("planks", 1), count);
    }

    private static CraftingRecipe createRecipe(ItemTarget planks) {
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{
                planks, planks, planks,
                planks, planks, planks,
                null, null, null
        }, 2);
    }
}
