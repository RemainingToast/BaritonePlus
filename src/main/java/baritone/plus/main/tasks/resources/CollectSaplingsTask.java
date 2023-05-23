package baritone.plus.main.tasks.resources;

import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.ItemHelper;

public class CollectSaplingsTask extends MineAndCollectTask {
    public CollectSaplingsTask(int count) {
        super(new ItemTarget(ItemHelper.SAPLINGS, count), ItemHelper.SAPLING_SOURCES,
                MiningRequirement.HAND);
    }
}
