package baritone.plus.main.tasks.container;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.InteractWithBlockTask;
import baritone.plus.main.tasks.construction.DestroyBlockTask;
import baritone.plus.main.tasks.movement.TimeoutWanderTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.trackers.storage.ContainerCache;
import baritone.plus.api.trackers.storage.ContainerType;
import baritone.plus.api.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * Opens a STORAGE container and does whatever you want inside of it
 */
public abstract class AbstractDoToStorageContainerTask extends Task {

    private ContainerType _currentContainerType = null;

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        Optional<BlockPos> containerTarget = getContainerTarget();

        // No container found
        if (containerTarget.isEmpty()) {
            setDebugState("Wandering");
            _currentContainerType = null;
            return onSearchWander(mod);
        }

        BlockPos targetPos = containerTarget.get();

        // We're open
        if (_currentContainerType != null && ContainerType.screenHandlerMatches(_currentContainerType)) {

            // Optional<BlockPos> lastInteracted = mod.getItemStorage().getLastBlockPosInteraction();
            //if (lastInteracted.isPresent() && lastInteracted.get().equals(targetPos)) {
            Optional<ContainerCache> cache = mod.getItemStorage().getContainerAtPosition(targetPos);
            if (cache.isPresent()) {
                return onContainerOpenSubtask(mod, cache.get());
            }
            //}
        }

        // Get to the container
        if (mod.getChunkTracker().isChunkLoaded(targetPos)) {
            Block type = mod.getWorld().getBlockState(targetPos).getBlock();
            _currentContainerType = ContainerType.getFromBlock(type);
        }
        if (WorldHelper.isChest(mod, targetPos) && WorldHelper.isSolid(mod, targetPos.up()) && WorldHelper.canBreak(mod, targetPos.up())) {
            setDebugState("Clearing block above chest");
            return new DestroyBlockTask(targetPos.up());
        }
        setDebugState("Opening container: " + targetPos.toShortString());
        return new InteractWithBlockTask(targetPos);
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    protected abstract Optional<BlockPos> getContainerTarget();

    protected abstract Task onContainerOpenSubtask(BaritonePlus mod, ContainerCache containerCache);

    // Virtual
    // TODO: Interface this
    protected Task onSearchWander(BaritonePlus mod) {
        return new TimeoutWanderTask();
    }
}
