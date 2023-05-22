package adris.altoclef.tasks.anarchy.duping;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.entity.AbstractDoToEntityTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.init.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.screen.slot.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.world.biome.BiomeKeys;

import java.util.*;

// TODO - Finish
//  - Explores, Finds Donkey, Tames Donkey, Crafts Chest, Find Saddle, Completes on Donkey with Saddle and Chest
public class TameDonkeyTask extends AbstractDoToEntityTask {

    private final List<Item> DONKEY_FOOD = List.of(Items.WHEAT,
            Items.SUGAR,
            Blocks.HAY_BLOCK.asItem(),
            Items.APPLE,
            Items.GOLDEN_CARROT,
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE
    );

    public TameDonkeyTask() {
        super(0, -1, -1);
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        return other instanceof TameDonkeyTask;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("Can't find Donkeys here. Going to Overworld.");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }

        if (mod.getPlayer().getY() < 50) {
            setDebugState("Going to surface");
            return new GetToYTask(100);
        }

        var task = super.onTick(mod);
        if (task instanceof TimeoutWanderTask) {
            setDebugState("Exploring+Searching for Tamable Donkeys");
            task = new SearchWithinBiomeTask(BiomeKeys.PLAINS);
        }

        return task;
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        if (entity instanceof DonkeyEntity donkey) { // Not necessary just helpful
            if (!mod.getSlotHandler().forceDeequip(itemStack -> DONKEY_FOOD.contains(itemStack.getItem()))) {
                Debug.logWarning("Failed to Stow Donkey Food!");
                return null;
            }

            if (!mod.getSlotHandler().forceEquipItem(Items.AIR)) {
                Debug.logWarning("Failed to Equip Empty Hand! Inventory Full?");

                // Try throwing away garbage
                var garbage = StorageHelper.getGarbageSlot(mod);

                garbage.ifPresent(slot -> {
                    mod.getSlotHandler().clickSlot(slot, 0, ClickType.PICKUP);
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, ClickType.PICKUP);

                });

                return null;
            }

            if (mod.getSlotHandler().forceEquipItem(Items.AIR)) {
                setDebugState("Trying to tame Donkey");
                mod.getController().interactEntity(mod.getPlayer(), donkey, Hand.MAIN_HAND);

//                var vehicle = mod.getPlayer().getVehicle();
//                if (vehicle != null && donkey == vehicle) {
//                    if (donkey.isTame()) {
//                        Debug.logMessage("Donkey Tamed! %s".formatted(donkey.getPos().toString()));
//                        stop(mod);
//                        return null;
//                    }
//
//                    // Hop Off Donkey
//                    if (donkey.getOwnerUuid() == null
//                            && !donkey.isTame()
//                            && !donkey.isBaby()) {
//                        donkey.removeAllPassengers();
//                    }
//                }
            }
        }

        return null;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(),
                entity -> {
                    if (entity instanceof DonkeyEntity donkey) {
                        return donkey.getOwnerUuid() == null
                                && !donkey.isTame()
                                && !donkey.isBaby();
                    }
                    return false;
                }, DonkeyEntity.class
        );
    }

    @Override
    protected String toDebugString() {
        return "Taming Donkey";
    }
}
