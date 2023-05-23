package baritone.plus.main.chains;

import baritone.api.utils.input.Input;
import baritone.plus.api.tasks.TaskRunner;
import baritone.plus.api.util.Pair;
import baritone.plus.api.util.helpers.*;
import baritone.plus.api.util.slots.PlayerSlot;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.PlusSettings;
import baritone.plus.main.tasks.resources.CollectFoodTask;
import baritone.plus.main.tasks.speedrun.DragonBreathTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FoodChain extends SingleTaskChain {
    private static FoodChainConfig _config;

    static {
        ConfigHelper.loadConfig("configs/food_chain_settings.json", FoodChainConfig::new, FoodChainConfig.class, newConfig -> _config = newConfig);
    }

    private final DragonBreathTracker _dragonBreathTracker = new DragonBreathTracker();
    boolean _hasFood;
    private boolean _isTryingToEat = false;
    private boolean _requestFillup = false;
    private boolean _needsFood = false;
    private Optional<Item> _cachedPerfectFood = Optional.empty();
    private boolean shouldStop = false;

    public FoodChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(BaritonePlus mod) {
        // Nothing.
    }

    private void startEat(BaritonePlus mod, Item food) {
        //Debug.logInternal("EATING " + toUse.getTranslationKey() + " : " + test);
        _isTryingToEat = true;
        _requestFillup = true;
        mod.getSlotHandler().forceEquipItem(new Item[]{food}, true); //"true" because it's food
        mod.getInputControls().hold(Input.CLICK_RIGHT);
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
    }

    private void stopEat(BaritonePlus mod) {
        if (_isTryingToEat) {
            if (mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) {
                if (StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT).getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    _isTryingToEat = false;
                    _requestFillup = false;
                }
            } else {
                _isTryingToEat = false;
                _requestFillup = false;
            }
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
        }
    }

    public boolean isTryingToEat() {
        return _isTryingToEat;
    }

    @Override
    public float getPriority(BaritonePlus mod) {
        if (WorldHelper.isInNetherPortal(mod)) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }
        if (mod.getMobDefenseChain().isPuttingOutFire()) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }
        _dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod)) {
            if (_dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                stopEat(mod);
                return Float.NEGATIVE_INFINITY;
            }
        }
        if (!mod.getModSettings().isAutoEat()) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }

        // do NOT eat while in lava if we are escaping it (spaghetti code dependencies go brrrr)
        if (mod.getPlayer().isInLava()) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }

        /*
        - Eats if:
        - We're hungry and have food that fits
            - We're low on health and maybe a little bit hungry
            - We're very low on health and are even slightly hungry
        - We're kind of hungry and have food that fits perfectly
         */
        // We're in danger, don't eat now!!
        if (!mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isFallingOhNo(mod) ||
                mod.getPlayer().isActiveItemStackBlocking() || shouldStop) {
            stopEat(mod);
            return Float.NEGATIVE_INFINITY;
        }
        Pair<Integer, Optional<Item>> calculation = calculateFood(mod);
        int _cachedFoodScore = calculation.left();
        _cachedPerfectFood = calculation.right();

        boolean hasFood = _cachedFoodScore > 0;
        _hasFood = hasFood;

        // If we requested a fillup but we're full, stop.
        if (_requestFillup && mod.getPlayer().getFoodStats().getFoodLevel() >= 20) {
            _requestFillup = false;
        }
        // If we no longer have food, we no longer can eat.
        if (!hasFood) {
            _requestFillup = false;
        }
        if (hasFood && (needsToEat() || _requestFillup) && _cachedPerfectFood.isPresent() &&
                !mod.getMLGBucketChain().isChorusFruiting() && !mod.getPlayer().isActiveItemStackBlocking()) {
            Item toUse = _cachedPerfectFood.get();
            // Make sure we're not facing a container
            if (!LookHelper.tryAvoidingInteractable(mod)) {
                return Float.NEGATIVE_INFINITY;
            }
            startEat(mod, toUse);
        } else {
            stopEat(mod);
        }

        PlusSettings settings = mod.getModSettings();

        if (_needsFood || _cachedFoodScore < settings.getMinimumFoodAllowed()) {
            _needsFood = _cachedFoodScore < settings.getFoodUnitsToCollect();

            // Only collect if we don't have enough food.
            // If the user inputs invalid settings, the bot would get stuck here.
            if (_cachedFoodScore < settings.getFoodUnitsToCollect()) {
                setTask(new CollectFoodTask(settings.getFoodUnitsToCollect()));
                return 55f;
            }
        }


        // Food eating is handled asynchronously.
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isActive() {
        // We're always checking for food.
        return true;
    }

    @Override
    public String getName() {
        return "Food";
    }

    @Override
    protected void onStop(BaritonePlus mod) {
        super.onStop(mod);
        stopEat(mod);
    }

    public boolean needsToEat() {
        if (!hasFood() || shouldStop) {
            return false;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        assert player != null;
        int foodLevel = player.getFoodStats().getFoodLevel();
        float health = player.getHealth();

        if (health <= 10 && foodLevel <= 19) {
            return true;
        }
        //Debug.logMessage("FOOD: " + foodLevel + " -- HEALTH: " + health);
        if (foodLevel >= 20) {
            // We can't eat.
            return false;
        } else {
            // Eat if we're desperate/need to heal ASAP
            if (player.isBurning() || player.isPotionActive(MobEffects.WITHER) || health < _config.alwaysEatWhenWitherOrFireAndHealthBelow) {
                return true;
            } else if (foodLevel > _config.alwaysEatWhenBelowHunger) {
                if (health < _config.alwaysEatWhenBelowHealth) {
                    return true;
                }
            } else {
                // We have half hunger
                return true;
            }
        }

        // Eat if we're  units hungry and we have a perfect fit.
        if (foodLevel < _config.alwaysEatWhenBelowHungerAndPerfectFit && _cachedPerfectFood.isPresent()) {
            int need = 20 - foodLevel;
            Item best = _cachedPerfectFood.get();
            int fills = (best instanceof ItemFood) ? ((ItemFood) best).getHealAmount(best.getDefaultInstance()) : -1;
            return fills == need;
        }

        return false;
    }

    private Pair<Integer, Optional<Item>> calculateFood(BaritonePlus mod) {
        Item bestFood = null;
        double bestFoodScore = Double.NEGATIVE_INFINITY;
        int foodTotal = 0;
        EntityPlayerSP player = mod.getPlayer();
        float health = player != null ? player.getHealth() : 20;
        //float toHeal = player != null? 20 - player.getHealth() : 0;
        float hunger = player != null ? player.getFoodStats().getFoodLevel() : 20;
        float saturation = player != null ? player.getFoodStats().getSaturationLevel() : 20;
        // Get best food item + calculate food total
        for (ItemStack stack : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            if (stack.getItem() instanceof ItemFood) {
                // Ignore protected items
                if (!ItemHelper.canThrowAwayStack(mod, stack)) continue;

                // Ignore spider eyes
                if (stack.getItem() == Items.SPIDER_EYE) {
                    continue;
                }

                assert player != null;
                FoodStats food = player.getFoodStats();

                float hungerIfEaten = Math.min(hunger + food.getFoodLevel(), 20);
                float saturationIfEaten = Math.min(hungerIfEaten, saturation + food.getSaturationLevel());
                float gainedSaturation = (saturationIfEaten - saturation);
                float gainedHunger = (hungerIfEaten - hunger);
                float hungerNotFilled = 20 - hungerIfEaten;

                float saturationWasted = food.getSaturationLevel() - gainedSaturation;
                float hungerWasted = food.getFoodLevel() - gainedHunger;

                boolean prioritizeSaturation = health < _config.prioritizeSaturationWhenBelowHealth;
                float saturationGoodScore = prioritizeSaturation ? gainedSaturation * _config.foodPickPrioritizeSaturationSaturationMultiplier : gainedSaturation;
                float saturationLossPenalty = prioritizeSaturation ? 0 : saturationWasted * _config.foodPickSaturationWastePenaltyMultiplier;
                float hungerLossPenalty = hungerWasted * _config.foodPickHungerWastePenaltyMultiplier;
                float hungerNotFilledPenalty = hungerNotFilled * _config.foodPickHungerNotFilledPenaltyMultiplier;

                float score = saturationGoodScore - saturationLossPenalty - hungerLossPenalty - hungerNotFilledPenalty;

                if (stack.getItem() == Items.ROTTEN_FLESH) {
                    score -= _config.foodPickRottenFleshPenalty;
                }
                if (score > bestFoodScore) {
                    bestFoodScore = score;
                    bestFood = stack.getItem();
                }

                foodTotal += Objects.requireNonNull(player.getFoodStats()).getFoodLevel() * stack.getCount();
            }
        }

        return new Pair<>(foodTotal, Optional.ofNullable(bestFood));
    }

    // If we need to eat like, NOW.
    public boolean needsToEatCritical() {
        return false;
    }

    public boolean hasFood() {
        return _hasFood;
    }

    public void shouldStop(boolean shouldStopInput) {
        shouldStop = shouldStopInput;
    }

    public boolean isShouldStop() {
        return shouldStop;
    }

    static class FoodChainConfig {
        public int alwaysEatWhenWitherOrFireAndHealthBelow = 6;
        public int alwaysEatWhenBelowHunger = 10;
        public int alwaysEatWhenBelowHealth = 14;
        public int alwaysEatWhenBelowHungerAndPerfectFit = 20 - 5;
        public int prioritizeSaturationWhenBelowHealth = 8;
        public float foodPickPrioritizeSaturationSaturationMultiplier = 8;
        public float foodPickSaturationWastePenaltyMultiplier = 1;
        public float foodPickHungerWastePenaltyMultiplier = 2;
        public float foodPickHungerNotFilledPenaltyMultiplier = 1;
        public float foodPickRottenFleshPenalty = 100;
        public float runDontEatMaxHealth = 3;
        public int runDontEatMaxHunger = 3;
        public int canTankHitsAndEatArmor = 15;
        public int canTankHitsAndEatMaxHunger = 3;
    }
}
