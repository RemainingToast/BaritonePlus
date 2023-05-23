package baritone.plus.main.tasks.misc;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.container.LootContainerTask;
import baritone.plus.main.tasks.movement.TimeoutWanderTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RavageRuinedPortalsTask extends Task {
    public final Item[] LOOT = {
            Items.IRON_NUGGET,
            Items.FLINT,
            Items.OBSIDIAN,
            Items.FIRE_CHARGE,
            Items.FLINT_AND_STEEL,
            Items.GOLD_NUGGET,
            Items.GOLDEN_APPLE,
            Items.GOLDEN_AXE,
            Items.GOLDEN_HOE,
            Items.GOLDEN_PICKAXE,
            Items.GOLDEN_SHOVEL,
            Items.GOLDEN_SWORD,
            Items.GOLDEN_HELMET,
            Items.GOLDEN_CHESTPLATE,
            Items.GOLDEN_LEGGINGS,
            Items.GOLDEN_BOOTS,
            Items.GLISTERING_MELON_SLICE,
            Items.GOLDEN_CARROT,
            Items.GOLD_INGOT,
            Items.CLOCK,
            Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Items.GOLDEN_HORSE_ARMOR,
            Items.GOLD_BLOCK,
            Items.BELL,
            Items.ENCHANTED_GOLDEN_APPLE
    };
    private List<BlockPos> _notRuinedPortalChests = new ArrayList<>();
    private Task _lootTask;

    public RavageRuinedPortalsTask() {

    }

    @Override
    protected void onStart(BaritonePlus mod) {
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(Blocks.CHEST);
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (_lootTask != null && _lootTask.isActive() && !_lootTask.isFinished(mod)) {
            return _lootTask;
        }
        Optional<BlockPos> closest = locateClosestUnopenedRuinedPortalChest(mod);
        if (closest.isPresent()) {
            _lootTask = new LootContainerTask(closest.get(), List.of(LOOT));
            return _lootTask;
        }
        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(BaritonePlus mod, Task task) {
        mod.getBlockTracker().stopTracking(Blocks.CHEST);
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task task) {
        return task instanceof RavageRuinedPortalsTask;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Ravaging Ruined Portals";
    }

    private boolean canBeLootablePortalChest(BaritonePlus mod, BlockPos blockPos) {
        if (mod.getWorld().getBlockState(blockPos.up(1)).getBlock() == Blocks.WATER || blockPos.getY() < 50) {
            return false;
        }
        for (BlockPos check : WorldHelper.scanRegion(mod, blockPos.add(-4, -2, -4), blockPos.add(4, 2, 4))) {
            if (mod.getWorld().getBlockState(check).getBlock() == Blocks.NETHERRACK) {
                return true;
            }
        }
        _notRuinedPortalChests.add(blockPos);
        return false;
    }

    private Optional<BlockPos> locateClosestUnopenedRuinedPortalChest(BaritonePlus mod) {
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            return Optional.empty();
        }
        return mod.getBlockTracker().getNearestTracking(blockPos -> !_notRuinedPortalChests.contains(blockPos) && WorldHelper.isUnopenedChest(mod, blockPos) && canBeLootablePortalChest(mod, blockPos), Blocks.CHEST);
    }
}
