package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.init.Items;

public class ShearAndCollectBlockTask extends MineAndCollectTask {

    public ShearAndCollectBlockTask(ItemTarget[] itemTargets, Block... blocksToMine) {
        super(itemTargets, blocksToMine, MiningRequirement.HAND);
    }

    public ShearAndCollectBlockTask(Item[] items, int count, Block... blocksToMine) {
        this(new ItemTarget[]{new ItemTarget(items, count)}, blocksToMine);
    }

    public ShearAndCollectBlockTask(Item item, int count, Block... blocksToMine) {
        this(new Item[]{item}, count, blocksToMine);
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().forceUseTool((blockState, itemStack) ->
                itemStack.getItem() == Items.SHEARS && ItemHelper.areShearsEffective(blockState.getBlock())
        );
        super.onStart(mod);
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        mod.getBehaviour().pop();
        super.onStop(mod, interruptTask);
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {
        if (!mod.getItemStorage().hasItem(Items.SHEARS)) {
            return TaskCatalogue.getItemTask(Items.SHEARS, 1);
        }
        return super.onResourceTick(mod);
    }
}
