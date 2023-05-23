package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.MiningRequirement;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public class CollectWheatSeedsTask extends ResourceTask {

    private final int _count;

    public CollectWheatSeedsTask(int count) {
        super(Items.WHEAT_SEEDS, count);
        _count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {
        mod.getBlockTracker().trackBlock(Blocks.WHEAT);
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {
        // If wheat block found, collect wheat but don't pick up the wheat.
        if (mod.getBlockTracker().anyFound(Blocks.WHEAT)) {
            return new CollectCropTask(Items.AIR, 999, Blocks.WHEAT, Items.WHEAT_SEEDS);
        }
        // Otherwise, break grass blocks.
        return new MineAndCollectTask(Items.WHEAT_SEEDS, _count, new Block[]{Blocks.GRASS, Blocks.TALL_GRASS}, MiningRequirement.HAND);
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectWheatSeedsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " wheat seeds.";
    }
}
