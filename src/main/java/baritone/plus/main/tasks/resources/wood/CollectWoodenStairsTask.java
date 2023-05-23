package baritone.plus.main.tasks.resources.wood;

import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.resources.CraftWithMatchingPlanksTask;
import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.item.Item;

public class CollectWoodenStairsTask extends CraftWithMatchingPlanksTask {

    public CollectWoodenStairsTask(Item[] targets, ItemTarget planks, int count) {
        super(targets, woodItems -> woodItems.stairs, createRecipe(planks), new boolean[]{true, false, false, true, true, false, true, true, true}, count);
    }

    public CollectWoodenStairsTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    public CollectWoodenStairsTask(int count) {
        this(ItemHelper.WOOD_STAIRS, TaskCatalogue.getItemTarget("planks", 1), count);
    }

    private static CraftingRecipe createRecipe(ItemTarget planks) {
        ItemTarget p = planks;
        ItemTarget o = null;
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{p, o, o, p, p, o, p, p, p}, 4);
    }
}
