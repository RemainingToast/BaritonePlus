package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.movement.DefaultGoToDimensionTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public class CollectQuartzTask extends ResourceTask {

    private final int _count;

    public CollectQuartzTask(int count) {
        super(Items.QUARTZ, count);
        _count = count;
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
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            setDebugState("Going to nether");
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        }

        setDebugState("Mining");
        return new MineAndCollectTask(new ItemTarget(Items.QUARTZ, _count), new Block[]{Blocks.NETHER_QUARTZ_ORE}, MiningRequirement.WOOD);
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectQuartzTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " quartz";
    }
}
