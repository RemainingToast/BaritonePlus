package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.entity.ShearSheepTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

import java.util.Arrays;
import java.util.HashSet;

public class CollectWoolTask extends ResourceTask {

    private final int _count;

    private final HashSet<DyeColor> _colors;
    private final Item[] _wools;

    public CollectWoolTask(DyeColor[] colors, int count) {
        super(new ItemTarget(ItemHelper.WOOL, count, "any wool"));
        _colors = new HashSet<>(Arrays.asList(colors));
        _count = count;
        _wools = getWoolColorItems(colors);
    }

    public CollectWoolTask(DyeColor color, int count) {
        this(new DyeColor[]{color}, count);
    }

    public CollectWoolTask(int count) {
        this(DyeColor.values(), count);
    }

    private static Item[] getWoolColorItems(DyeColor[] colors) {
        Item[] result = new Item[colors.length];
        for (int i = 0; i < result.length; ++i) {
            result[i] = ItemHelper.getColorfulItems(colors[i]).wool;
        }
        return result;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {
        mod.getBlockTracker().trackBlock(ItemHelper.itemsToBlocks(_wools));
    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {

        // TODO: If we don't find good color wool blocks
        // and we DONT find good color sheep:
        // USE DYES + REGULAR WOOL TO CRAFT THE WOOL COLOR!!

        // If we find a wool block, break it.
        Block[] woolBlocks = ItemHelper.itemsToBlocks(_wools);
        if (mod.getBlockTracker().anyFound(woolBlocks)) {
            return new MineAndCollectTask(new ItemTarget(_wools, "any wool"), woolBlocks, MiningRequirement.HAND);
        }

        // If we have shears, right click nearest sheep
        // Otherwise, kill + loot wool.

        // Dimension
        if (isInWrongDimension(mod) && !mod.getEntityTracker().entityFound(SheepEntity.class)) {
            return getToCorrectDimensionTask(mod);
        }

        if (mod.getItemStorage().hasItem(Items.SHEARS)) {
            // Shear sheep.
            return new ShearSheepTask();
        }

        // Only option left is to Kill la Kill.
        return new KillAndLootTask(SheepEntity.class, entity -> {
            if (entity instanceof SheepEntity sheep) {
                // Hunt sheep of the same color.
                return _colors.contains(sheep.getColor()) && !sheep.isSheared();
            }
            return false;
        }, new ItemTarget(_wools, _count, "any wool"));
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(ItemHelper.itemsToBlocks(_wools));
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectWoolTask && ((CollectWoolTask) other)._count == _count;
    }

    @Override
    protected String toDebugStringName() {
        return "Collect wool";
    }

}
