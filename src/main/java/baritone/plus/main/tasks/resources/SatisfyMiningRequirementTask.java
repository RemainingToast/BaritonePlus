package baritone.plus.main.tasks.resources;

import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import net.minecraft.block.BlockState;

/**
 * Make sure we have a tool at or above a mining level.
 */
public class SatisfyMiningRequirementTask extends Task {

    private final MiningRequirement _requirement;
    private final BlockState _toMine;

    public SatisfyMiningRequirementTask(MiningRequirement requirement, BlockState toMine) {
        _requirement = requirement;
        _toMine = toMine;
    }

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        var _tool = _requirement.getBestTool(_toMine);
        if (_tool == null) return null;
        return TaskCatalogue.getItemTask(_tool, 1);
        /*switch (_requirement) {
            case HAND:
                // Will never happen if you program this right
                break;
            case WOOD:
                return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
            case STONE:
                return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 1);
            case IRON:
                return TaskCatalogue.getItemTask(Items.IRON_PICKAXE, 1);
            case DIAMOND:
                return TaskCatalogue.getItemTask(Items.DIAMOND_PICKAXE, 1);
        }
        return null;*/
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof SatisfyMiningRequirementTask task) {
            return task._requirement == _requirement;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Satisfy Mining Req: " + _requirement;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return StorageHelper.miningRequirementMetInventory(mod, _requirement, _toMine);
    }
}
