package baritone.plus.main.tasks.container;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.apache.commons.lang3.NotImplementedException;

// TODO: Anvils
public class CraftInAnvilTask extends DoStuffInContainerTask {
    public CraftInAnvilTask() {
        super(new Block[]{Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL}, new ItemTarget("anvil"));
    }

    @Override
    protected boolean isSubTaskEqual(DoStuffInContainerTask other) {
        throw new NotImplementedException("Anvil Not Implemented, whoops");
    }

    @Override
    public boolean isContainerOpen(BaritonePlus mod) {
        throw new NotImplementedException("Anvil Not Implemented, whoops");
    }

    @Override
    protected Task containerSubTask(BaritonePlus mod) {
        throw new NotImplementedException("Anvil Not Implemented, whoops");
    }

    @Override
    protected double getCostToMakeNew(BaritonePlus mod) {
        throw new NotImplementedException("Anvil Not Implemented, whoops");
    }
}
