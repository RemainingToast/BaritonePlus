package baritone.plus.main.chains;

import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import baritone.plus.api.tasks.TaskRunner;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.LookHelper;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.api.util.time.TimerGame;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.DoToClosestBlockTask;
import baritone.plus.main.tasks.InteractWithBlockTask;
import baritone.plus.main.tasks.construction.PutOutFireTask;
import baritone.plus.main.tasks.movement.EnterNetherPortalTask;
import baritone.plus.main.tasks.movement.EscapeFromLavaTask;
import baritone.plus.main.tasks.movement.GetToBlockTask;
import baritone.plus.main.tasks.movement.SafeRandomShimmyTask;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class WorldSurvivalChain extends SingleTaskChain {

    private final TimerGame _wasInLavaTimer = new TimerGame(1);
    private final TimerGame _portalStuckTimer = new TimerGame(5);
    private boolean _wasAvoidingDrowning;

    private BlockPos _extinguishWaterPosition;

    public WorldSurvivalChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(BaritonePlus mod) {

    }

    @Override
    public float getPriority(BaritonePlus mod) {
        if (!BaritonePlus.inGame()) return Float.NEGATIVE_INFINITY;

        // Drowning
        handleDrowning(mod);

        // Lava Escape
        if (isInLavaOhShit(mod) && mod.getBehaviour().shouldEscapeLava()) {
            setTask(new EscapeFromLavaTask());
            return 100;
        }

        // Fire escape
        if (isInFire(mod)) {
            setTask(new DoToClosestBlockTask(PutOutFireTask::new, Blocks.FIRE/*, Blocks.SOUL_FIRE*/));
            return 100;
        }

        // Extinguish with water
        if (mod.getModSettings().shouldExtinguishSelfWithWater()) {
            if (!(_mainTask instanceof EscapeFromLavaTask && isCurrentlyRunning(mod)) && mod.getPlayer().isBurning() && !mod.getPlayer().isPotionActive(MobEffects.FIRE_RESISTANCE)/* && !mod.getWorld().getDimension().ultrawarm()*/) {
                // Extinguish ourselves
                if (mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
                    BlockPos targetWaterPos = mod.getPlayer().getPosition();
                    if (WorldHelper.isSolid(mod, targetWaterPos.down()) && WorldHelper.canPlace(mod, targetWaterPos)) {
                        Optional<Rotation> reach = LookHelper.getReach(targetWaterPos.down(), EnumFacing.UP);
                        if (reach.isPresent()) {
                            mod.getClientBaritone().getLookBehavior().updateTarget(reach.get(), true);
                            if (mod.getClientBaritone().getPlayerContext().isLookingAt(targetWaterPos.down())) {
                                if (mod.getSlotHandler().forceEquipItem(Items.WATER_BUCKET)) {
                                    _extinguishWaterPosition = targetWaterPos;
                                    mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                                    setTask(null);
                                    return 90;
                                }
                            }
                        }
                    }
                }
                setTask(new DoToClosestBlockTask(GetToBlockTask::new, Blocks.WATER));
                return 90;
            } else if (mod.getItemStorage().hasItem(Items.BUCKET) && _extinguishWaterPosition != null && mod.getBlockTracker().blockIsValid(_extinguishWaterPosition, Blocks.WATER)) {
                // Pick up the water
                setTask(new InteractWithBlockTask(new ItemTarget(Items.BUCKET, 1), EnumFacing.UP, _extinguishWaterPosition.down(), true));
                return 60;
            } else {
                _extinguishWaterPosition = null;
            }
        }

        // Portal stuck
        if (isStuckInNetherPortal(mod)) {
            // We can't break or place while inside a portal (not really)
            mod.getExtraBaritoneSettings().setInteractionPaused(true);
        } else {
            // We're no longer stuck, but we might want to move AWAY from our stuck position.
            _portalStuckTimer.reset();
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
        }
        if (_portalStuckTimer.elapsed()) {
            // We're stuck inside a portal, so get out.
            // Don't allow breaking while we're inside the portal.
            setTask(new SafeRandomShimmyTask());
            return 60;
        }

        return Float.NEGATIVE_INFINITY;
    }

    private void handleDrowning(BaritonePlus mod) {
        // Swim
        boolean avoidedDrowning = false;
        if (mod.getModSettings().shouldAvoidDrowning()) {
            if (!mod.getClientBaritone().getPathingBehavior().isPathing()) {
                // TODO Calculate Depth?
                if (mod.getPlayer().isInWater() && mod.getPlayer().getAir() < 300 /*mod.getPlayer().getMaxAir()*/) {
                    // Swim up!
                    mod.getInputControls().hold(Input.JUMP);
                    //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.JUMP, true);
                    avoidedDrowning = true;
                    _wasAvoidingDrowning = true;
                }
            }
        }
        // Stop swimming up if we just swam.
        if (_wasAvoidingDrowning && !avoidedDrowning) {
            _wasAvoidingDrowning = false;
            mod.getInputControls().release(Input.JUMP);
            //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.JUMP, false);
        }
    }

    private boolean isInLavaOhShit(BaritonePlus mod) {
        if (mod.getPlayer().isInLava() && !mod.getPlayer().isPotionActive(MobEffects.FIRE_RESISTANCE)) {
            _wasInLavaTimer.reset();
            return true;
        }
        return mod.getPlayer().isBurning() && !_wasInLavaTimer.elapsed();
    }

    private boolean isInFire(BaritonePlus mod) {
        if (mod.getPlayer().isBurning() && !mod.getPlayer().isPotionActive(MobEffects.FIRE_RESISTANCE)) {
            for (BlockPos pos : WorldHelper.getBlocksTouchingPlayer(mod)) {
                Block b = mod.getWorld().getBlockState(pos).getBlock();
                if (b instanceof BlockFire) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isStuckInNetherPortal(BaritonePlus mod) {
        return WorldHelper.isInNetherPortal(mod) && !mod.getUserTaskChain().getCurrentTask().thisOrChildSatisfies(task -> task instanceof EnterNetherPortalTask);
    }

    @Override
    public String getName() {
        return "Misc World Survival Chain";
    }

    @Override
    public boolean isActive() {
        // Always check for survival.
        return true;
    }

    @Override
    protected void onStop(BaritonePlus mod) {
        super.onStop(mod);
    }
}
