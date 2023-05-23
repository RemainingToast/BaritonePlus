package baritone.plus.api.trackers.storage;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.BlockInteractEvent;
import baritone.plus.api.event.events.ScreenOpenEvent;
import baritone.plus.api.trackers.Tracker;
import baritone.plus.api.trackers.TrackerManager;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.Pair;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.main.Debug;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;

/**
 * Keeps track of items in containers
 */
public class ContainerSubTracker extends Tracker {

    private final HashMap<Dimension, HashMap<BlockPos, ContainerCache>> _containerCaches = new HashMap<>();
    private boolean _containerOpen;
    private BlockPos _lastBlockPosInteraction;
    private Block _lastBlockInteraction;
    private ContainerCache _enderChestCache;
    private boolean _hasSentError;

    public ContainerSubTracker(TrackerManager manager) {
        super(manager);
        for (Dimension dimension : Dimension.values()) {
            _containerCaches.put(dimension, new HashMap<>());
        }

        // Listen for when we interact with a block
        EventBus.subscribe(BlockInteractEvent.class, evt -> {
            BlockPos blockPos = evt.hitResult.getPosition();
            IBlockState bs = _mod.getWorld().getBlockState(blockPos);
            onBlockInteract(blockPos, bs.getBlock());
        });
        EventBus.subscribe(ScreenOpenEvent.class, evt -> {
            if (evt.preOpen) {
                onScreenOpenFirstTick(evt.screen);
            } else {
                if (evt.screen == null)
                    onScreenClose();
            }
        });
    }

    private void onBlockInteract(BlockPos pos, Block block) {
        if (block instanceof BlockFurnace ||
                block instanceof BlockChest ||
                block instanceof BlockEnderChest ||
                block instanceof BlockHopper ||
                block instanceof BlockShulkerBox ||
                block instanceof BlockDispenser
                /*|| block instanceof BarrelBlock*/) {
            _lastBlockPosInteraction = pos;
            _lastBlockInteraction = block;
        }
    }

    private void onScreenOpenFirstTick(final GuiScreen screen) {
        _containerOpen = screen instanceof GuiFurnace
//                || screen instanceof GuiContainer
//                || screen instanceof SmokerScreen
//                || screen instanceof BlastFurnaceScreen
                || screen instanceof GuiHopper
                || screen instanceof GuiShulkerBox;
    }

    private void onScreenClose() {
        _containerOpen = false;
        _lastBlockPosInteraction = null;
        _lastBlockInteraction = null;
        _hasSentError = false;
    }

    public void onServerTick() {
        if (Minecraft.getMinecraft().player == null)
            return;
        // If we haven't registered interacting with a block, try the currently "looking at" block
        if (_containerOpen && _lastBlockPosInteraction == null && _lastBlockInteraction == null) {
            if (Minecraft.getMinecraft().crosshairTarget instanceof BlockHitResult bhit) {
                Debug.logWarning("Screen open but no block interaction detected, using the block we're currently looking at.");
                _lastBlockPosInteraction = bhit.getPosition();
                _lastBlockInteraction = _mod.getWorld().getBlockState(_lastBlockPosInteraction).getBlock();
            }
        }
        if (_containerOpen && _lastBlockPosInteraction != null && _lastBlockInteraction != null) {
            BlockPos containerPos = _lastBlockPosInteraction;
            ScreenHandler handler = Minecraft.getMinecraft().player.currentScreenHandler;
            if (handler == null)
                return;

            HashMap<BlockPos, ContainerCache> dimCache = _containerCaches.get(WorldHelper.getCurrentDimension());

            // Container Type Mismatch, reset.
            if (dimCache.containsKey(containerPos)) {
                ContainerType currentType = dimCache.get(containerPos).getContainerType();
                if (!ContainerType.screenHandlerMatches(currentType, handler)) {
                    if (!_hasSentError) {
                        Debug.logMessage("Mismatched container screen at " + containerPos.toShortString() + ", will overwrite container data: " + handler.getType() + " ?=> " + currentType);
                        _hasSentError = true;
                    }
                    dimCache.remove(containerPos);
                }
            }

            // New container found
            if (!dimCache.containsKey(containerPos)) {
                Block containerBlock = _lastBlockInteraction;
                ContainerType interactType = ContainerType.getFromBlock(containerBlock);
                ContainerCache newCache = new ContainerCache(WorldHelper.getCurrentDimension(), containerPos, interactType);
                dimCache.put(containerPos, newCache);
                // Special ender chest cache
                if (interactType == ContainerType.ENDER_CHEST) {
                    _enderChestCache = newCache;
                }
            }

            ContainerCache toUpdate = dimCache.get(containerPos);
            toUpdate.update(handler, stack -> {

            });
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isContainerCacheValid(Dimension dimension, ContainerCache cache) {
        BlockPos pos = cache.getPosition();
        if (WorldHelper.getCurrentDimension() == dimension && _mod.getChunkTracker().isChunkLoaded(pos)) {
            ContainerType actualType = ContainerType.getFromBlock(_mod.getWorld().getBlockState(pos).getBlock());
            if (actualType == ContainerType.EMPTY) {
                return false;
            }
            return actualType == cache.getContainerType();
        }
        return true;
    }

    public Optional<ContainerCache> getContainerAtPosition(Dimension dimension, BlockPos pos) {
        Optional<ContainerCache> cache = Optional.ofNullable(_containerCaches.get(dimension).getOrDefault(pos, null));
        if (cache.isPresent() && !isContainerCacheValid(dimension, cache.get())) {
            _containerCaches.get(dimension).remove(pos);
            return Optional.empty();
        }
        return cache;
    }

    public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
        return getContainerAtPosition(WorldHelper.getCurrentDimension(), pos);
    }

    public Optional<ContainerCache> getEnderChestStorage() {
        return Optional.ofNullable(_enderChestCache);
    }

    public List<ContainerCache> getCachedContainers(Predicate<ContainerCache> accept) {
        List<ContainerCache> result = new ArrayList<>();
        List<Pair<Dimension, BlockPos>> toRemove = new ArrayList<>();
        for (Dimension dim : _containerCaches.keySet()) {
            HashMap<BlockPos, ContainerCache> map = _containerCaches.get(dim);
            for (ContainerCache cache : map.values()) {
                if (!isContainerCacheValid(dim, cache)) {
                    toRemove.add(new Pair<>(dim, cache.getPosition()));
                    continue;
                }
                if (accept.test(cache))
                    result.add(cache);
            }
        }
        for (Pair<Dimension, BlockPos> remove : toRemove) {
            _containerCaches.get(remove.left()).remove(remove.right());
        }
        return result;
    }

    public List<ContainerCache> getCachedContainers(ContainerType... types) {
        Set<ContainerType> typeSet = new HashSet<>(Arrays.asList(types));
        return getCachedContainers(cache -> typeSet.contains(cache.getContainerType()));
    }

    public Optional<ContainerCache> getClosestTo(Vec3d pos, Predicate<ContainerCache> accept) {
        double bestDist = Double.POSITIVE_INFINITY;
        Dimension dim = WorldHelper.getCurrentDimension();

        List<BlockPos> toRemove = new ArrayList<>();

        ContainerCache bestCache = null;
        for (ContainerCache cache : _containerCaches.get(dim).values()) {
            if (!isContainerCacheValid(dim, cache)) {
                toRemove.add(cache.getPosition());
                continue;
            }
            double dist = cache.getPosition().getSquaredDistance(pos);
            if (dist < bestDist) {
                if (accept.test(cache)) {
                    bestDist = dist;
                    bestCache = cache;
                }
            }
        }
        // Clear anything invalid
        for (BlockPos remove : toRemove) {
            _containerCaches.get(dim).remove(remove);
        }
        return Optional.ofNullable(bestCache);
    }

    public Optional<ContainerCache> getClosestTo(Vec3d pos, ContainerType... types) {
        Set<ContainerType> typeSet = new HashSet<>(Arrays.asList(types));
        return getClosestTo(pos, cache -> typeSet.contains(cache.getContainerType()));
    }

    public List<ContainerCache> getContainersWithItem(Item... items) {
        return getCachedContainers(cache -> cache.hasItem(items));
    }

    public Optional<ContainerCache> getClosestWithItem(Vec3d pos, Item... items) {
        return getClosestTo(pos, cache -> cache.hasItem(items));
    }

    public boolean hasItem(Predicate<ContainerCache> accept, Item... items) {
        for (HashMap<BlockPos, ContainerCache> map : _containerCaches.values()) {
            for (ContainerCache cache : map.values()) {
                if (cache.hasItem(items) && accept.test(cache))
                    return true;
            }
        }
        return false;
    }

    public boolean hasItem(Item... items) {
        return hasItem(cache -> true, items);
    }

    public BlockPos getLastBlockPosInteraction() {
        return _lastBlockPosInteraction;
    }

    @Override
    protected void updateState() {
        // umm lol
    }

    @Override
    protected void reset() {
        for (Dimension key : _containerCaches.keySet()) {
            _containerCaches.get(key).clear();
        }
    }

}
