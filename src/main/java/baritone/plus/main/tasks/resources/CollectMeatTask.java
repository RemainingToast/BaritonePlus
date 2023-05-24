package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.container.SmeltInSmokerTask;
import baritone.plus.main.tasks.movement.PickupDroppedItemTask;
import baritone.plus.main.tasks.movement.TimeoutWanderTask;
import baritone.plus.main.tasks.speedrun.MarvionBeatMinecraftTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.SmeltTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.SmokerSlot;
import baritone.plus.api.util.time.TimerGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;

import java.util.Objects;
import java.util.Optional;

public class CollectMeatTask extends Task {
    private static final CookableFoodTarget[] COOKABLE_FOODS = new CookableFoodTarget[]{
            new CookableFoodTarget("beef", CowEntity.class),
            new CookableFoodTarget("porkchop", PigEntity.class),
            new CookableFoodTarget("chicken", ChickenEntity.class),
            new CookableFoodTarget("mutton", SheepEntity.class)
    };
    private final double _unitsNeeded;
    private final TimerGame _checkNewOptionsTimer = new TimerGame(10);
    private SmeltInSmokerTask _smeltTask = null;
    private Task _currentResourceTask = null;

    public CollectMeatTask(double unitsNeeded) {
        _unitsNeeded = unitsNeeded;
    }

    private static double getFoodPotential(ItemStack food) {
        if (food == null) return 0;
        int count = food.getCount();
        if (count <= 0) return 0;
        for (CookableFoodTarget cookable : COOKABLE_FOODS) {
            if (food.getItem() == cookable.getRaw()) {
                assert cookable.getCooked().getFoodComponent() != null;
                return count * cookable.getCooked().getFoodComponent().getHunger();
            }
        }
        return 0;
    }

    private static double calculateFoodPotential(BaritonePlus mod) {
        double potentialFood = 0;
        for (ItemStack food : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            potentialFood += getFoodPotential(food);
        }
        // Check smelting
        ScreenHandler screen = mod.getPlayer().currentScreenHandler;
        if (screen instanceof SmokerScreenHandler) {
            potentialFood += getFoodPotential(StorageHelper.getItemStackInSlot(SmokerSlot.INPUT_SLOT_MATERIALS));
            potentialFood += getFoodPotential(StorageHelper.getItemStackInSlot(SmokerSlot.OUTPUT_SLOT));
        }
        return potentialFood;
    }

    @Override
    protected void onStart(BaritonePlus mod) {

    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        for (CookableFoodTarget cookable : COOKABLE_FOODS) {
            if (mod.getEntityTracker().entityFound(cookable.mobToKill)) {
                Optional<Entity> chickens = mod.getEntityTracker().getClosestEntity(cookable.mobToKill);
                if (chickens.isPresent()) {
                    Iterable<Entity> entities = mod.getWorld().getEntities();
                    for (Entity entity : entities) {
                        if (entity instanceof HostileEntity || entity instanceof SlimeEntity) {
                            if (chickens.get().hasPassenger(entity)) {
                                if (mod.getEntityTracker().isEntityReachable(entity)) {
                                    Debug.logMessage("Blacklisting chicken jockey.");
                                    mod.getEntityTracker().requestEntityUnreachable(chickens.get());
                                }
                            }
                        }
                    }
                }
            }
        }

        // If we were previously smelting, keep on smelting.
        if (_smeltTask != null && _smeltTask.isActive() && !_smeltTask.isFinished(mod)) {
            setDebugState("Cooking...");
            if (MarvionBeatMinecraftTask.getConfig().renderDistanceManipulation) {
                MinecraftClient.getInstance().options.getViewDistance().setValue(2);
                MinecraftClient.getInstance().options.getEntityDistanceScaling().setValue(0.5);
            }
            return _smeltTask;
        } else {
            _smeltTask = null;
        }
        if (_checkNewOptionsTimer.elapsed()) {
            // Try a new resource task
            _checkNewOptionsTimer.reset();
            _currentResourceTask = null;
        }
        if (_currentResourceTask != null && _currentResourceTask.isActive() && !_currentResourceTask.isFinished(mod) && !_currentResourceTask.thisOrChildAreTimedOut()) {
            return _currentResourceTask;
        }
        // Calculate potential
        double potentialFood = calculateFoodPotential(mod);
        if (potentialFood >= _unitsNeeded) {
            // Convert our raw foods
            // PLAN:
            // - If we have raw foods, smelt all of them
            // Convert raw foods -> cooked foods
            for (CookableFoodTarget cookable : COOKABLE_FOODS) {
                int rawCount = mod.getItemStorage().getItemCount(cookable.getRaw());
                if (rawCount > 0) {
                    //Debug.logMessage("STARTING COOK OF " + cookable.getRaw().getTranslationKey());
                    int toSmelt = rawCount + mod.getItemStorage().getItemCount(cookable.getCooked());
                    _smeltTask = new SmeltInSmokerTask(new SmeltTarget(new ItemTarget(cookable.cookedFood, toSmelt), new ItemTarget(cookable.rawFood, rawCount)));
                    _smeltTask.ignoreMaterials();
                    return _smeltTask;
                }
            }
        } else {
            // Pick up raw/cooked foods on ground
            for (CookableFoodTarget cookable : COOKABLE_FOODS) {
                Task t = this.pickupTaskOrNull(mod, cookable.getRaw(), 20);
                if (t == null) t = this.pickupTaskOrNull(mod, cookable.getCooked(), 40);
                if (t != null) {
                    setDebugState("Picking up Cookable food");
                    _currentResourceTask = t;
                    return _currentResourceTask;
                }
            }
            // Cooked foods
            double bestScore = 0;
            Entity bestEntity = null;
            Item bestRawFood = null;
            for (CookableFoodTarget cookable : COOKABLE_FOODS) {
                if (!mod.getEntityTracker().entityFound(cookable.mobToKill)) continue;
                Optional<Entity> nearest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), cookable.mobToKill);
                if (nearest.isEmpty()) continue; // ?? This crashed once?
                if (nearest.get() instanceof LivingEntity livingEntity) {
                    // Peta
                    if (livingEntity.isBaby()) continue;
                }
                int hungerPerformance = cookable.getCookedUnits();
                double sqDistance = nearest.get().squaredDistanceTo(mod.getPlayer());
                double score = (double) 100 * hungerPerformance / (sqDistance);
                if (score > bestScore) {
                    bestScore = score;
                    bestEntity = nearest.get();
                    bestRawFood = cookable.getRaw();
                }
            }
            if (bestEntity != null) {
                setDebugState("Killing " + bestEntity.getType().getTranslationKey());
                _currentResourceTask = killTaskOrNull(bestEntity, bestRawFood);
                return _currentResourceTask;
            }
        }

        for (Item raw : ItemHelper.RAW_FOODS) {
            if (mod.getItemStorage().hasItem(raw)) {
                Optional<Item> cooked = ItemHelper.getCookedFood(raw);
                if (cooked.isPresent()) {
                    int targetCount = mod.getItemStorage().getItemCount(cooked.get()) + mod.getItemStorage().getItemCount(raw);
                    _smeltTask = new SmeltInSmokerTask(new SmeltTarget(new ItemTarget(cooked.get(), targetCount), new ItemTarget(raw, targetCount)));
                    return _smeltTask;
                }
            }
        }
        // Look for food.
        setDebugState("Searching...");
        return new TimeoutWanderTask();
    }

    private Task killTaskOrNull(Entity entity, Item itemToGrab) {
        return new KillAndLootTask(entity.getClass(), new ItemTarget(itemToGrab, 1));
    }

    private Task pickupTaskOrNull(BaritonePlus mod, Item itemToGrab, double maxRange) {
        Optional<ItemEntity> nearestDrop = Optional.empty();
        if (mod.getEntityTracker().itemDropped(itemToGrab)) {
            nearestDrop = mod.getEntityTracker().getClosestItemDrop(mod.getPlayer().getPos(), itemToGrab);
        }
        if (nearestDrop.isPresent()) {
            if (nearestDrop.get().isInRange(mod.getPlayer(), maxRange)) {
                return new PickupDroppedItemTask(new ItemTarget(itemToGrab), true);
            }
            //return new GetToBlockTask(nearestDrop.getBlockPos(), false);
        }
        return null;
    }

    private Task pickupTaskOrNull(BaritonePlus mod, Item itemToGrab) {
        return pickupTaskOrNull(mod, itemToGrab, Double.POSITIVE_INFINITY);
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return StorageHelper.calculateInventoryFoodScore(mod) >= _unitsNeeded && _smeltTask == null;
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CollectMeatTask task) {
            return task._unitsNeeded == _unitsNeeded;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Collect " + _unitsNeeded + " units of meat.";
    }

    private static class CookableFoodTarget {
        public String rawFood;
        public String cookedFood;
        public Class<?> mobToKill;

        public CookableFoodTarget(String rawFood, String cookedFood, Class<?> mobToKill) {
            this.rawFood = rawFood;
            this.cookedFood = cookedFood;
            this.mobToKill = mobToKill;
        }

        public CookableFoodTarget(String rawFood, Class<?> mobToKill) {
            this(rawFood, "cooked_" + rawFood, mobToKill);
        }

        private Item getRaw() {
            return Objects.requireNonNull(TaskCatalogue.getItemMatches(rawFood))[0];
        }

        private Item getCooked() {
            return Objects.requireNonNull(TaskCatalogue.getItemMatches(cookedFood))[0];
        }

        public int getCookedUnits() {
            assert getCooked().getFoodComponent() != null;
            return getCooked().getFoodComponent().getHunger();
        }
    }
}
