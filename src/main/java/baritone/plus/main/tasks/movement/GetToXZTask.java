package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalXZ;
import net.minecraft.util.math.BlockPos;

public class GetToXZTask extends CustomBaritoneGoalTask {

    private final int _x, _z;
    private final Dimension _dimension;

    public GetToXZTask(BlockPos pos) {
        this(pos.getX(), pos.getZ(), null);
    }

    public GetToXZTask(int x, int z) {
        this(x, z, null);
    }

    public GetToXZTask(int x, int z, Dimension dimension) {
        _x = x;
        _z = z;
        _dimension = dimension;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (_dimension != null && WorldHelper.getCurrentDimension() != _dimension) {
            return new DefaultGoToDimensionTask(_dimension);
        }
        return super.onTick(mod);
    }

    @Override
    protected Goal newGoal(BaritonePlus mod) {
        return new GoalXZ(_x, _z);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToXZTask task) {
            return task._x == _x && task._z == _z && task._dimension == _dimension;
        }
        return false;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        BlockPos cur = mod.getPlayer().getBlockPos();
        return (cur.getX() == _x && cur.getZ() == _z && (_dimension == null || _dimension == WorldHelper.getCurrentDimension()));
    }

    @Override
    protected String toDebugString() {
        return "Getting to (" + _x + "," + _z + ")" + (_dimension != null ? " in dimension " + _dimension : "");
    }
}
