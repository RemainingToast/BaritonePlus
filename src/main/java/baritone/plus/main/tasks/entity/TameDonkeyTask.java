package baritone.plus.main.tasks.entity;

import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.api.util.slots.Slot;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.movement.DefaultGoToDimensionTask;
import baritone.plus.main.tasks.movement.GetToSurfaceTask;
import baritone.plus.main.tasks.movement.SearchWithinBiomesTask;
import baritone.plus.main.tasks.movement.TimeoutWanderTask;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.world.biome.BiomeKeys;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// TODO - Find Saddle Task
public class TameDonkeyTask extends AbstractTameEntityTask<DonkeyEntity> {

    private final List<Item> DONKEY_FOOD = List.of(Items.WHEAT,
            Items.SUGAR,
            Blocks.HAY_BLOCK.asItem(),
            Items.APPLE,
            Items.GOLDEN_CARROT,
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE
    );

    public TameDonkeyTask() {
        super();
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        return other instanceof TameDonkeyTask;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("Can't find Donkeys here. Going to Overworld.");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }

        if (WorldHelper.isUnderground(mod, 8)) { // Can't search underground lol
            setDebugState("Going back to the surface");
            return new GetToSurfaceTask();
        }

        var task = super.onTick(mod);
        if (task instanceof TimeoutWanderTask) {
            setDebugState("Exploring+Searching for Tamable Donkeys");
            task = new SearchWithinBiomesTask(Set.of(
                    BiomeKeys.PLAINS,
                    BiomeKeys.SUNFLOWER_PLAINS,
                    BiomeKeys.FLOWER_FOREST,
                    BiomeKeys.SAVANNA,
                    BiomeKeys.SAVANNA_PLATEAU,
                    BiomeKeys.WINDSWEPT_SAVANNA
            ));
        }

        return task;
    }

    @Override
    protected Class<DonkeyEntity> getEntityClass() {
        return DonkeyEntity.class;
    }

    @Override
    protected boolean canTame(BaritonePlus mod, DonkeyEntity donkey) {
        return !donkey.isTame() && !donkey.isBaby();
    }

    @Override
    protected Task onTameInteract(BaritonePlus mod, DonkeyEntity donkey) {
        if (!mod.getSlotHandler().forceDeequip(itemStack -> DONKEY_FOOD.contains(itemStack.getItem()))) {
            Debug.logWarning("Failed to Stow Donkey Food!");
            return null;
        }

        if (!mod.getSlotHandler().forceEquipItem(Items.AIR)) {
            Debug.logWarning("Failed to Equip Empty Hand! Inventory Full?");

            // Try throwing away garbage
            var garbage = StorageHelper.getGarbageSlot(mod);

            garbage.ifPresent(slot -> {
                mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);

            });

            return null;
        }

        if (mod.getSlotHandler().forceEquipItem(Items.AIR)) {
            setDebugState("Trying to tame Donkey");
            mod.getController().interactEntity(mod.getPlayer(), donkey, Hand.MAIN_HAND);
        }

        return null;
    }

    @Override
    protected boolean isTamed(BaritonePlus mod, DonkeyEntity donkey) {
        return donkey.isTame();
    }

    @Override
    protected Task onTameFinish(BaritonePlus mod, DonkeyEntity donkey) {
        setDebugState("Chesting Donkey..");

        // Make sure we have a chest
        if (!donkey.hasChest()) {
            if (!StorageHelper.hasCataloguedItem(mod, "sign")) {
                return TaskCatalogue.getItemTask("chest", 1);
            }

            if (mod.getSlotHandler().forceEquipItem(Items.CHEST)) {
                mod.getController().interactEntity(mod.getPlayer(), donkey, Hand.MAIN_HAND);
            }
        }

        stop(mod);
        return null;
    }

    @Override
    protected Optional<Entity> getEntityTarget(BaritonePlus mod) {
        return Optional.ofNullable(findBestHorse(mod.getEntityTracker()
                .getTrackedEntities(DonkeyEntity.class).stream()
                .filter(donkey -> canTame(mod, donkey))
                .collect(Collectors.toList())));
    }

    @Override
    protected String toDebugString() {
        return "Taming Donkey";
    }
}