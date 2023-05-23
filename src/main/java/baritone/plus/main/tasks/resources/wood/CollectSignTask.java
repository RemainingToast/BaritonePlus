package baritone.plus.main.tasks.resources.wood;

import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.resources.CraftWithMatchingPlanksTask;
import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.item.Item;

public class CollectSignTask extends CraftWithMatchingPlanksTask {

    public CollectSignTask(Item[] targets, ItemTarget planks, int count) {
        // Top 6 are planks, must be the same.
        super(targets, woodItems -> woodItems.sign, createRecipe(planks), new boolean[]{true, true, true, true, true, true, false, false, false}, count);
    }

    public CollectSignTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    public CollectSignTask(int count) {
        this(ItemHelper.WOOD_SIGN, TaskCatalogue.getItemTarget("planks", 1), count);
    }


    private static CraftingRecipe createRecipe(ItemTarget planks) {
        ItemTarget p = planks;
        ItemTarget stick = TaskCatalogue.getItemTarget("stick", 1);
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{p, p, p, p, p, p, null, stick, null}, 3);
    }
}
