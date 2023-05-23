package baritone.plus.api.util.baritone;


import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class GoalBlockSide implements Goal {

    private final BlockPos _block;
    private final EnumFacing _direction;
    private final double _buffer;

    public GoalBlockSide(BlockPos block, EnumFacing direction, double bufferDistance) {
        this._block = block;
        this._direction = direction;
        this._buffer = bufferDistance;
    }

    public GoalBlockSide(BlockPos block, EnumFacing direction) {
        this(block, direction, 1);
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        // We are on the right side
        return getDistanceInRightDirection(x, y, z) > 0;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        // How far are we away
        return Math.min(getDistanceInRightDirection(x, y, z), 0);
    }

    private double getDistanceInRightDirection(int x, int y, int z) {
        Vec3d delta = new Vec3d(x, y, z).subtract(_block.getX(), _block.getY(), _block.getZ());
        Vec3i dir = _direction.getVector();
        double dot = new Vec3d(dir.getX(), dir.getY(), dir.getZ()).dotProduct(delta);
        // WE ASSUME THAT dir IS NORMALIZED
        double distCorrect = dot;
        return distCorrect - this._buffer;
    }
}
