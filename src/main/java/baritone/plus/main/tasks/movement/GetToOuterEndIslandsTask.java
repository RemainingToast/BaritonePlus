package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.tasks.InteractWithBlockTask;
import baritone.plus.main.tasks.resources.GetBuildingMaterialsTask;
import baritone.plus.main.tasks.speedrun.BeatMinecraft2Task;
import baritone.plus.main.tasks.squashed.CataloguedResourceTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.baritone.GoalAnd;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalYLevel;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class GetToOuterEndIslandsTask extends Task {
    public final int END_ISLAND_START_RADIUS = 800;
    public final Vec3i[] OFFSETS = {
            new Vec3i(1, -1, 1),
            new Vec3i(1, -1, -1),
            new Vec3i(-1, -1, 1),
            new Vec3i(-1, -1, -1),
            new Vec3i(2, -1, 0),
            new Vec3i(0, -1, 2),
            new Vec3i(-2, -1, 0),
            new Vec3i(0, -1, -2)
    };
    private Task _beatTheGame;

    public GetToOuterEndIslandsTask() {

    }

    @Override
    protected void onStart(BaritonePlus mod) {
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(Blocks.END_GATEWAY);
        _beatTheGame = new BeatMinecraft2Task();
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (mod.getBlockTracker().anyFound(Blocks.END_GATEWAY)) {
            if (!mod.getItemStorage().hasItemInventoryOnly(Items.ENDER_PEARL)) {
                setDebugState("Getting an ender pearl");
                return new CataloguedResourceTask(new ItemTarget(Items.ENDER_PEARL, 1));
            }
            BlockPos gateway = mod.getBlockTracker().getNearestTracking(Blocks.END_GATEWAY).get();
            int blocksNeeded = Math.abs(mod.getPlayer().getBlockY() - gateway.getY()) +
                    Math.abs(mod.getPlayer().getBlockX() - gateway.getX()) +
                    Math.abs(mod.getPlayer().getBlockZ() - gateway.getZ()) - 3;
            if (StorageHelper.getBuildingMaterialCount(mod) < blocksNeeded) {
                setDebugState("Getting building materials");
                return new GetBuildingMaterialsTask(blocksNeeded);
            }
            GoalAnd goal = makeGoal(gateway);
            Debug.logMessage(mod.getPlayer().getPosition().toString());
            if (!goal.isInGoal(mod.getPlayer().getPosition()) || !mod.getPlayer().isOnGround()) {
                mod.getClientBaritone().getCustomGoalProcess().setGoal(goal);
                if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                    mod.getClientBaritone().getCustomGoalProcess().path();
                }
                setDebugState("Getting close to gateway...");
                return null;
            }
            setDebugState("Throwing the pearl inside");
            return new InteractWithBlockTask(Items.ENDER_PEARL, gateway);
        }
        setDebugState("Beating the Game to get to an end gateway");
        return _beatTheGame;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.END_GATEWAY);
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof GetToOuterEndIslandsTask;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return WorldHelper.getCurrentDimension() == Dimension.END &&
                !WorldHelper.inRangeXZ(new Vec3d(0, 64, 0), mod.getPlayer().getPos(), END_ISLAND_START_RADIUS);
    }

    @Override
    protected String toDebugString() {
        return "Going to outer end islands";
    }

    private GoalAnd makeGoal(BlockPos gateway) {
        return new GoalAnd(new GoalComposite(
                new GoalGetToBlock(gateway.add(OFFSETS[0])),
                new GoalGetToBlock(gateway.add(OFFSETS[1])),
                new GoalGetToBlock(gateway.add(OFFSETS[2])),
                new GoalGetToBlock(gateway.add(OFFSETS[3])),
                new GoalGetToBlock(gateway.add(OFFSETS[4])),
                new GoalGetToBlock(gateway.add(OFFSETS[5])),
                new GoalGetToBlock(gateway.add(OFFSETS[6])),
                new GoalGetToBlock(gateway.add(OFFSETS[7]))
        ), new GoalYLevel(74));
    }
}
