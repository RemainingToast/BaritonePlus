package baritone.plus.main.tasks.construction;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.movement.DefaultGoToDimensionTask;
import baritone.plus.main.tasks.movement.TimeoutWanderTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.api.util.time.TimerGame;
import net.minecraft.block.Blocks;

import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.function.Predicate;

public class CoverWithSandTask extends Task {
    private static final TimerGame timer = new TimerGame(30);
    private static final Task getSand = TaskCatalogue.getItemTask(Items.SAND, 128);
    private static final Task goToNether = new DefaultGoToDimensionTask(Dimension.NETHER);
    private static final Task goToOverworld = new DefaultGoToDimensionTask(Dimension.OVERWORLD);
    private BlockPos lavaPos;

    @Override
    protected void onStart(BaritonePlus mod) {
        timer.reset();
        mod.getBlockTracker().trackBlock(Blocks.LAVA);
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (getSand != null && getSand.isActive() && !getSand.isFinished(mod)) {
            setDebugState("Getting sands to cover nether lava.");
            timer.reset();
            return getSand;
        }
        if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD &&
                mod.getItemStorage().getItemCount(Items.SAND) < 64) {
            timer.reset();
            return getSand;
        }
        if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD &&
                mod.getItemStorage().getItemCount(Items.SAND) > 64) {
            setDebugState("Going to nether.");
            timer.reset();
            return goToNether;
        }
        if (WorldHelper.getCurrentDimension() == Dimension.NETHER &&
                !mod.getItemStorage().hasItem(Items.SAND)) {
            setDebugState("Going to overworld to get sand.");
            timer.reset();
            return goToOverworld;
        }
        if (coverLavaWithSand(mod) == null) {
            setDebugState("Searching valid lava.");
            timer.reset();
            return new TimeoutWanderTask();
        }
        setDebugState("Covering lava with sand");
        return coverLavaWithSand(mod);
    }

    private Task coverLavaWithSand(BaritonePlus mod) {
        Predicate<BlockPos> validLava = blockPos ->
                mod.getWorld().getBlockState(blockPos).getFluidState().isStill() &&
                        WorldHelper.isAir(mod, blockPos.up()) &&
                        (!WorldHelper.isBlock(mod, blockPos.north(), Blocks.LAVA) ||
                                !WorldHelper.isBlock(mod, blockPos.south(), Blocks.LAVA) ||
                                !WorldHelper.isBlock(mod, blockPos.east(), Blocks.LAVA) ||
                                !WorldHelper.isBlock(mod, blockPos.west(), Blocks.LAVA) ||
                                !WorldHelper.isBlock(mod, blockPos.north().up(), Blocks.LAVA) ||
                                !WorldHelper.isBlock(mod, blockPos.south().up(), Blocks.LAVA) ||
                                !WorldHelper.isBlock(mod, blockPos.east().up(), Blocks.LAVA) ||
                                !WorldHelper.isBlock(mod, blockPos.west().up(), Blocks.LAVA));
        Optional<BlockPos> lava = mod.getBlockTracker().getNearestTracking(validLava, Blocks.LAVA);
        if (lava.isPresent()) {
            if (lavaPos == null) {
                lavaPos = lava.get();
                timer.reset();
            }
            if (timer.elapsed()) {
                lavaPos = lava.get();
                timer.reset();
            }
            if (!WorldHelper.isBlock(mod, lavaPos, Blocks.LAVA) || (!WorldHelper.isAir(mod, lavaPos.up()) &&
                    !WorldHelper.isFallingBlock(lavaPos.up())) ||
                    !mod.getWorld().getBlockState(lavaPos).getFluidState().isStill()) {
                lavaPos = lava.get();
                timer.reset();
            }
            return new PlaceBlockTask(lavaPos.up(), Blocks.SAND);
        }
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.LAVA);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof CoverWithSandTask;
    }

    @Override
    protected String toDebugString() {
        return "Covering nether lava with sand";
    }
}
