package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.DoToClosestBlockTask;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.construction.DestroyBlockTask;
import baritone.plus.main.tasks.movement.SearchWithinBiomeTask;
import baritone.plus.api.tasks.Task;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashSet;
import java.util.function.Predicate;

public class CollectCocoaBeansTask extends ResourceTask {
    private final int _count;
    private final HashSet<BlockPos> _wasFullyGrown = new HashSet<>();

    public CollectCocoaBeansTask(int targetCount) {
        super(Items.COCOA_BEANS, targetCount);
        _count = targetCount;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {
        mod.getBlockTracker().trackBlock(Blocks.COCOA);
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {

        Predicate<BlockPos> validCocoa = (blockPos) -> {
            if (!mod.getChunkTracker().isChunkLoaded(blockPos)) {
                return _wasFullyGrown.contains(blockPos);
            }

            BlockState s = mod.getWorld().getBlockState(blockPos);
            boolean mature = s.get(CocoaBlock.AGE) == 2;
            if (_wasFullyGrown.contains(blockPos)) {
                if (!mature) _wasFullyGrown.remove(blockPos);
            } else {
                if (mature) _wasFullyGrown.add(blockPos);
            }
            return mature;
        };

        // Break mature cocoa blocks
        if (mod.getBlockTracker().anyFound(validCocoa, Blocks.COCOA)) {
            setDebugState("Breaking cocoa blocks");
            return new DoToClosestBlockTask(DestroyBlockTask::new, validCocoa, Blocks.COCOA);
        }

        // Dimension
        if (isInWrongDimension(mod)) {
            return getToCorrectDimensionTask(mod);
        }

        // Search for jungles
        setDebugState("Exploring around jungles");
        return new SearchWithinBiomeTask(BiomeKeys.JUNGLE);
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.COCOA);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectCocoaBeansTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " cocoa beans.";
    }
}
