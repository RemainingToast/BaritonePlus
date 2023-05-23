package baritone.plus.main.tasks.resources.wood;

import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.resources.CraftWithMatchingPlanksTask;
import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.item.Item;

public class CollectFenceTask extends CraftWithMatchingPlanksTask {

    public CollectFenceTask(Item[] targets, ItemTarget planks, int count) {
        super(targets, woodItems -> woodItems.fence, createRecipe(planks), new boolean[]{true, false, true, true, false, true, false, false, false}, count);
    }

    public CollectFenceTask(Item target, String plankCatalogueName, int count) {
        this(new Item[]{target}, new ItemTarget(plankCatalogueName, 1), count);
    }

    public CollectFenceTask(int count) {
        this(ItemHelper.WOOD_FENCE, TaskCatalogue.getItemTarget("planks", 1), count);
    }

    private static CraftingRecipe createRecipe(ItemTarget planks) {
        ItemTarget p = planks;
        ItemTarget s = TaskCatalogue.getItemTarget("stick", 1);
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{p, s, p, p, s, p, null, null, null}, 3);
    }
}
