package baritone.plus.main.tasks.construction.compound;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.InteractWithBlockTask;
import baritone.plus.main.tasks.construction.DestroyBlockTask;
import baritone.plus.main.tasks.construction.PlaceBlockTask;
import baritone.plus.main.tasks.movement.TimeoutWanderTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.api.util.time.TimerGame;
import net.minecraft.block.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EnumFacing;
import net.minecraft.util.math.Vec3i;

/**
 * Build a nether portal with obsidian blocks.
 */
public class ConstructNetherPortalObsidianTask extends Task {

    // There's some code duplication between here and ConstructNetherPortalBucketTask...
    // but it's so heavily intertwined/changed that it would take forever to untangle and
    // retangle the two together.

    // Order here matters
    private static final Vec3i[] PORTAL_FRAME = new Vec3i[]{
            // Left side
            new Vec3i(0, 0, -1),
            new Vec3i(0, 1, -1),
            new Vec3i(0, 2, -1),
            // Right side
            new Vec3i(0, 0, 2),
            new Vec3i(0, 1, 2),
            new Vec3i(0, 2, 2),
            // Top
            new Vec3i(0, 3, 0),
            new Vec3i(0, 3, 1),
            // Bottom
            new Vec3i(0, -1, 0),
            new Vec3i(0, -1, 1)
    };

    private static final Vec3i[] PORTAL_INTERIOR = new Vec3i[]{
            //Inside
            new Vec3i(0, 0, 0),
            new Vec3i(0, 1, 0),
            new Vec3i(0, 2, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(0, 1, 1),
            new Vec3i(0, 2, 1),
            //Outside 1
            new Vec3i(1, 0, 0),
            new Vec3i(1, 1, 0),
            new Vec3i(1, 2, 0),
            new Vec3i(1, 0, 1),
            new Vec3i(1, 1, 1),
            new Vec3i(1, 2, 1),
            //Outside 2
            new Vec3i(-1, 0, 0),
            new Vec3i(-1, 1, 0),
            new Vec3i(-1, 2, 0),
            new Vec3i(-1, 0, 1),
            new Vec3i(-1, 1, 1),
            new Vec3i(-1, 2, 1)
    };

    private static final Vec3i PORTALABLE_REGION_SIZE = new Vec3i(3, 6, 6);

    private final TimerGame _areaSearchTimer = new TimerGame(5);

    private BlockPos _origin;

    private BlockPos _destroyTarget;

    private static BlockPos getBuildableAreaNearby(BaritonePlus mod) {
        BlockPos checkOrigin = mod.getPlayer().getPosition();
        for (BlockPos toCheck : WorldHelper.scanRegion(mod, checkOrigin, checkOrigin.add(PORTALABLE_REGION_SIZE))) {
            if (Minecraft.getMinecraft().world == null) {
                return null;
            }
            IBlockState state = Minecraft.getMinecraft().world.getBlockState(toCheck);
            boolean validToWorld = (WorldHelper.canPlace(mod, toCheck) || WorldHelper.canBreak(mod, toCheck));
            if (!validToWorld || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.BEDROCK) {
                return null;
            }
        }
        return checkOrigin;
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        mod.getBehaviour().push();

        // Avoid breaking portal frame if we're obsidian.
        mod.getBehaviour().avoidBlockBreaking(block -> {
            if (_origin != null) {
                // Don't break frame
                for (Vec3i framePosRelative : PORTAL_FRAME) {
                    BlockPos framePos = _origin.add(framePosRelative);
                    if (block.equals(framePos)) {
                        return mod.getWorld().getBlockState(framePos).getBlock() == Blocks.OBSIDIAN;
                    }
                }
            }
            return false;
        });
        mod.getBehaviour().addProtectedItems(Items.FLINT_AND_STEEL);
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (_origin != null) {
            if (mod.getWorld().getBlockState(_origin.up()).getBlock() == Blocks.NETHER_PORTAL) {
                setDebugState("Done constructing nether portal.");
                mod.getBlockTracker().addBlock(Blocks.NETHER_PORTAL, _origin.up());
                return null;
            }
        }
        int neededObsidian = 10;
        BlockPos placeTarget = null;
        if (_origin != null) {
            for (Vec3i frameOffs : PORTAL_FRAME) {
                BlockPos framePos = _origin.add(frameOffs);
                if (!mod.getBlockTracker().blockIsValid(framePos, Blocks.OBSIDIAN)) {
                    placeTarget = framePos;
                    break;
                }
                neededObsidian--;
            }
        }

        // Get obsidian if we don't have.
        if (mod.getItemStorage().getItemCount(Items.OBSIDIAN) < neededObsidian) {
            setDebugState("Getting obsidian");
            return TaskCatalogue.getItemTask(Items.OBSIDIAN, neededObsidian);
        }

        // Find spot
        if (_origin == null) {
            if (_areaSearchTimer.elapsed()) {
                _areaSearchTimer.reset();
                Debug.logMessage("(Searching for area to build portal nearby...)");
                _origin = getBuildableAreaNearby(mod);
            }
            setDebugState("Looking for portalable area...");
            return new TimeoutWanderTask();
        }

        // Get flint and steel
        if (!mod.getItemStorage().hasItem(Items.FLINT_AND_STEEL)) {
            setDebugState("Getting flint and steel");
            return TaskCatalogue.getItemTask(Items.FLINT_AND_STEEL, 1);
        }

        // Place frame
        if (placeTarget != null) {
            setDebugState("Placing frame...");
            return new PlaceBlockTask(placeTarget, Blocks.OBSIDIAN);
        }

        // Clear middle
        if (_destroyTarget != null && !WorldHelper.isAir(mod, _destroyTarget)) {
            return new DestroyBlockTask(_destroyTarget);
        }
        for (Vec3i middleOffs : PORTAL_INTERIOR) {
            BlockPos middlePos = _origin.add(middleOffs);
            if (!WorldHelper.isAir(mod, middlePos)) {
                _destroyTarget = middlePos;
                return new DestroyBlockTask(_destroyTarget);
            }
        }
        // Flint and steel
        return new InteractWithBlockTask(new ItemTarget(Items.FLINT_AND_STEEL, 1), EnumFacing.UP, _origin.down(), true);
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof ConstructNetherPortalObsidianTask;
    }

    @Override
    protected String toDebugString() {
        return "Building nether portal with obsidian";
    }
}
