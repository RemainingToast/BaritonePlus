package baritone.plus.main.tasks.misc;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.movement.SearchWithinBiomeTask;
import baritone.plus.main.tasks.squashed.CataloguedResourceTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;

import java.util.List;

public class RavageDesertTemplesTask extends Task {
    public final Item[] LOOT = {
            Items.BONE,
            Items.ROTTEN_FLESH,
            Items.GUNPOWDER,
            Items.SAND,
            Items.STRING,
            Items.SPIDER_EYE,
            Items.ENCHANTED_BOOK,
            Items.SADDLE,
            Items.GOLDEN_APPLE,
            Items.GOLD_INGOT,
            Items.IRON_INGOT,
            Items.EMERALD,
            Items.IRON_HORSE_ARMOR,
            Items.GOLDEN_HORSE_ARMOR,
            Items.DIAMOND,
            Items.DIAMOND_HORSE_ARMOR,
            Items.ENCHANTED_GOLDEN_APPLE
    };
    private BlockPos _currentTemple;
    private Task _lootTask;
    private Task _pickaxeTask;

    public RavageDesertTemplesTask() {

    }

    @Override
    protected void onStart(BaritonePlus mod) {
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(Blocks.STONE_PRESSURE_PLATE);
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (_pickaxeTask != null && !_pickaxeTask.isFinished(mod)) {
            setDebugState("Need to get pickaxes left");
            return _pickaxeTask;
        }
        if (_lootTask != null && !_lootTask.isFinished(mod)) {
            setDebugState("Looting found temple");
            return _lootTask;
        }
        if (StorageHelper.miningRequirementMetInventory(mod, MiningRequirement.WOOD)) {
            setDebugState("Need to get pickaxes left");
            _pickaxeTask = new CataloguedResourceTask(new ItemTarget(Items.WOODEN_PICKAXE, 2));
            return _pickaxeTask;
        }
        _currentTemple = WorldHelper.getADesertTemple(mod);
        if (_currentTemple != null) {
            _lootTask = new LootDesertTempleTask(_currentTemple, List.of(LOOT));
            setDebugState("Looting found temple");
            return _lootTask;
        }
        return new SearchWithinBiomeTask(BiomeKeys.DESERT);
    }

    @Override
    protected void onStop(BaritonePlus mod, Task task) {
        mod.getBlockTracker().stopTracking(Blocks.STONE_PRESSURE_PLATE);
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof RavageDesertTemplesTask;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Ravaging Desert Temples";
    }
}
