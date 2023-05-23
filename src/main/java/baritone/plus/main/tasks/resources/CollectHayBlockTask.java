package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.container.CraftInTableTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.CraftingRecipe;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.RecipeTarget;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

// TODO: This can technically be removed, as it's a mine task followed by a collect task.
public class CollectHayBlockTask extends ResourceTask {

    private final int _count;

    public CollectHayBlockTask(int count) {
        super(Items.HAY_BLOCK, count);
        _count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {
        mod.getBlockTracker().trackBlock(Blocks.HAY_BLOCK);
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {

        if (mod.getBlockTracker().anyFound(Blocks.HAY_BLOCK)) {
            return new MineAndCollectTask(Items.HAY_BLOCK, _count, new Block[]{Blocks.HAY_BLOCK}, MiningRequirement.HAND);
        }

        ItemTarget w = new ItemTarget(Items.WHEAT, 1);
        return new CraftInTableTask(new RecipeTarget(Items.HAY_BLOCK, _count, CraftingRecipe.newShapedRecipe("hay_block", new ItemTarget[]{w, w, w, w, w, w, w, w, w}, 1)));
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.HAY_BLOCK);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectHayBlockTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " hay blocks.";
    }
}
