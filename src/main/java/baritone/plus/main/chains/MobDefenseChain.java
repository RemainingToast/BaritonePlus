package baritone.plus.main.chains;

import baritone.Baritone;
import baritone.api.utils.input.Input;
import baritone.plus.api.control.KillAura;
import baritone.plus.api.tasks.TaskRunner;
import baritone.plus.api.util.baritone.CachedProjectile;
import baritone.plus.api.util.helpers.*;
import baritone.plus.api.util.slots.PlayerSlot;
import baritone.plus.api.util.slots.Slot;
import baritone.plus.api.util.time.TimerGame;
import baritone.plus.launch.mixins.EntityCreeperAccessor;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.tasks.entity.KillEntitiesTask;
import baritone.plus.main.tasks.movement.CustomBaritoneGoalTask;
import baritone.plus.main.tasks.movement.DodgeProjectilesTask;
import baritone.plus.main.tasks.movement.RunAwayFromCreepersTask;
import baritone.plus.main.tasks.movement.RunAwayFromHostilesTask;
import baritone.plus.main.tasks.speedrun.DragonBreathTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class MobDefenseChain extends SingleTaskChain {
    private static final double DANGER_KEEP_DISTANCE = 50;
    private static final double CREEPER_KEEP_DISTANCE = 20;
    private static final double ARROW_KEEP_DISTANCE_HORIZONTAL = 4;//2;
    private static final double ARROW_KEEP_DISTANCE_VERTICAL = 15;//10;
    private static final double SAFE_KEEP_DISTANCE = 16;
    private final DragonBreathTracker _dragonBreathTracker = new DragonBreathTracker();
    private final KillAura _killAura = new KillAura();
    private final HashMap<Entity, TimerGame> _closeAnnoyingEntities = new HashMap<>();
    private Entity _targetEntity;
    private boolean _shielding = false;
    private boolean _doingFunkyStuff = false;
    private boolean _wasPuttingOutFire = false;
    private CustomBaritoneGoalTask _runAwayTask;

    private float _cachedLastPriority;

    public MobDefenseChain(TaskRunner runner) {
        super(runner);
    }

    public static double getCreeperSafety(Vec3d pos, EntityCreeper creeper) {
        double distance = creeper.getDistanceSq(pos.x, pos.y, pos.z);
        float fuse = ((EntityCreeperAccessor) creeper).fuseTime();

        // Not fusing.
        if (fuse <= 0.001f) return distance;
        return distance * 0.2; // less is WORSE
    }

    @Override
    public float getPriority(BaritonePlus mod) {
        _cachedLastPriority = getPriorityInner(mod);
        return _cachedLastPriority;
    }

    private void startShielding(BaritonePlus mod) {
        _shielding = true;
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.CLICK_RIGHT);
        mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
        if (!mod.getPlayer().isActiveItemStackBlocking()) {
            ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
            if (handItem.getItem() instanceof ItemFood) {
                List<ItemStack> spaceSlots = mod.getItemStorage().getItemStacksPlayerInventory(false);
                if (!spaceSlots.isEmpty()) {
                    for (ItemStack spaceSlot : spaceSlots) {
                        if (spaceSlot.isEmpty()) {
                            mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(), 0, ClickType.QUICK_MOVE);
                            return;
                        }
                    }
                }
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                garbage.ifPresent(slot -> mod.getSlotHandler().forceEquipItem(StorageHelper.getItemStackInSlot(slot).getItem()));
            }
        }
    }

    private void stopShielding(BaritonePlus mod) {
        if (_shielding) {
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
            if (cursor.getItem() instanceof ItemFood) {
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
                if (toMoveTo.isPresent()) {
                    Slot garbageSlot = toMoveTo.get();
                    mod.getSlotHandler().clickSlot(garbageSlot, 0, ClickType.PICKUP);
                }
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            _shielding = false;
        }
    }

    private boolean escapeDragonBreath(BaritonePlus mod) {
        _dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod)) {
            if (_dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                return true;
            }
        }
        return false;
    }

    public float getPriorityInner(BaritonePlus mod) {
        if (!BaritonePlus.inGame()) {
            return Float.NEGATIVE_INFINITY;
        }

        if (!mod.getModSettings().isMobDefense() || mod.getBehaviour().isDefenseDisabled()) {
            return Float.NEGATIVE_INFINITY;
        }

        // Apply avoidance if we're vulnerable, avoiding mobs if at all possible.
        // mod.getClientBaritoneSettings().avoidance.value = isVulnurable(mod);
        // Doing you a favor by disabling avoidance


        // Pause if we're not loaded into a world.
        if (!BaritonePlus.inGame()) return Float.NEGATIVE_INFINITY;

        // Put out fire if we're standing on one like an idiot
        BlockPos fireBlock = isInsideFireAndOnFire(mod);
        if (fireBlock != null) {
            putOutFire(mod, fireBlock);
            _wasPuttingOutFire = true;
        } else {
            // Stop putting stuff out if we no longer need to put out a fire.
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
            _wasPuttingOutFire = false;
        }

        if (mod.getFoodChain().needsToEat() || mod.getMLGBucketChain().isFallingOhNo(mod) ||
                !mod.getMLGBucketChain().doneMLG() || mod.getMLGBucketChain().isChorusFruiting()) {
            _killAura.stopShielding(mod);
            stopShielding(mod);
            return Float.NEGATIVE_INFINITY;
        }

        // Force field
        doForceField(mod);


        // Tell baritone to avoid mobs if we're vulnurable.
        // Costly.
        //mod.getClientBaritoneSettings().avoidance.value = isVulnurable(mod);

        // Run away if a weird mob is close by.
        Optional<Entity> universallyDangerous = getUniversallyDangerousMob(mod);
        if (universallyDangerous.isPresent()) {
//            mod.getBlockTracker().requestBlockUnreachable(universallyDangerous.get().getPosition(), 0);
            _runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
            setTask(_runAwayTask);
            return 70;
        }

        _doingFunkyStuff = false;
        // Run away from creepers
        EntityCreeper blowingUp = getClosestFusingCreeper(mod);
        if (blowingUp != null) {
            if (!mod.getFoodChain().needsToEat() && (mod.getItemStorage().hasItem(Items.SHIELD) ||
                    mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) &&
                    !mod.getEntityTracker().entityFound(EntityPotion.class) && _runAwayTask == null) {
                _doingFunkyStuff = true;
                LookHelper.lookAt(mod, blowingUp.getPositionEyes(1.0F));
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
            } else {
                _doingFunkyStuff = true;
                //Debug.logMessage("RUNNING AWAY!");
//                mod.getBlockTracker().requestBlockUnreachable(blowingUp.getPosition(), 0);
                _runAwayTask = new RunAwayFromCreepersTask(CREEPER_KEEP_DISTANCE);
                setTask(_runAwayTask);
                return 50 + ((EntityCreeperAccessor) blowingUp).fuseTime() * 50;
            }
        } else {
            if (!isProjectileClose(mod)) {
                stopShielding(mod);
            }
        }
        // Block projectiles with shield
        PlayerSlot offhandSlot = PlayerSlot.OFFHAND_SLOT;
        Item offhandItem = StorageHelper.getItemStackInSlot(offhandSlot).getItem();
        if (!mod.getFoodChain().needsToEat() && mod.getModSettings().isDodgeProjectiles() && isProjectileClose(mod) &&
                (mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) &&
                !mod.getEntityTracker().entityFound(EntityPotion.class) && _runAwayTask == null &&
                !mod.getPlayer().getCooldownTracker().hasCooldown(offhandItem)) {
            ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
            if (shieldSlot.getItem() != Items.SHIELD) {
                mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
            } else {
                startShielding(mod);
            }
        } else {
            if (blowingUp == null) {
                stopShielding(mod);
            }
        }
        // Dodge projectiles
        if (mod.getPlayer().getHealth() <= 10 || _runAwayTask != null || mod.getEntityTracker().entityFound(EntityPotion.class) ||
                (!mod.getItemStorage().hasItem(Items.SHIELD) && !mod.getItemStorage().hasItemInOffhand(Items.SHIELD))) {
            if (!mod.getFoodChain().needsToEat() && mod.getModSettings().isDodgeProjectiles() && isProjectileClose(mod)) {
                if (isVulnurable(mod)) {
                    _runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
                    setTask(_runAwayTask);
                    return 70;
                }

                _doingFunkyStuff = true;
                //Debug.logMessage("DODGING");
                _runAwayTask = new DodgeProjectilesTask(ARROW_KEEP_DISTANCE_HORIZONTAL, ARROW_KEEP_DISTANCE_VERTICAL);
                setTask(_runAwayTask);
                return 65;
            }
        }

        // Dodge all mobs cause we boutta die son
        if (isInDanger(mod) && !escapeDragonBreath(mod) && !mod.getFoodChain().isShouldStop()) {
            if (_targetEntity == null) {
//                for (Entity entity : mod.getEntityTracker().getHostiles()) {
//                    mod.getBlockTracker().requestBlockUnreachable(entity.getPosition(), 0);
//                }

                _runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
                setTask(_runAwayTask);
                return 70;
            }
        }

        if (mod.getModSettings().shouldDealWithAnnoyingHostiles()) {
            // Deal with hostiles because they are annoying.
            List<Entity> hostiles = mod.getEntityTracker().getHostiles();
            // TODO: I don't think this lock is necessary at all.

            ItemSword bestSword = null;
            for (Item item : new Item[]{/*Items.NETHERITE_SWORD, */
                    Items.DIAMOND_SWORD,
                    Items.IRON_SWORD,
                    Items.GOLDEN_SWORD,
                    Items.STONE_SWORD,
                    Items.WOODEN_SWORD
            }) {
                if (mod.getItemStorage().hasItem(item)) {
                    bestSword = (ItemSword) item;
                }
            }

            List<Entity> toDealWith = new ArrayList<>();

            // TODO: I don't think this lock is necessary at all.
            if (!hostiles.isEmpty()) {
                synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                    for (Entity hostile : hostiles) {
                        int annoyingRange = (hostile instanceof EntitySkeleton || hostile instanceof EntityWitch 
                                /*|| hostile instanceof PillagerEntity*/ || hostile instanceof EntityPigZombie || hostile instanceof EntityStray) ? 15 : 8;
                        boolean isClose = hostile.getDistanceSq(mod.getPlayer()) <= annoyingRange;

                        if (isClose) {
                            isClose = LookHelper.seesPlayer(hostile, mod.getPlayer(), annoyingRange);
                        }

                        // Give each hostile a timer, if they're close for too long deal with them.
                        if (isClose) {
                            if (!_closeAnnoyingEntities.containsKey(hostile)) {
//                                boolean wardenAttacking = hostile instanceof WardenEntity;
                                boolean witherAttacking = hostile instanceof EntityWither;
                                boolean endermanAttacking = hostile instanceof EntityEnderman;
                                boolean blazeAttacking = hostile instanceof EntityBlaze;
                                boolean witherSkeletonAttacking = hostile instanceof EntityWitherSkeleton;
//                                boolean hoglinAttacking = hostile instanceof HoglinEntity;
//                                boolean zoglinAttacking = hostile instanceof ZoglinEntity;
//                                boolean piglinBruteAttacking = hostile instanceof PiglinBruteEntity;
//                                boolean phantomAttacking = hostile instanceof PhantomEntity;
                                if (blazeAttacking || witherSkeletonAttacking 
                                        /*|| hoglinAttacking || zoglinAttacking || piglinBruteAttacking */
                                        || endermanAttacking || witherAttacking
                                        /*|| wardenAttacking || phantomAttacking*/
                                ) {
                                    if (isVulnurable(mod)) {
                                        _closeAnnoyingEntities.put(hostile, new TimerGame(0));
                                    } else {
                                        _closeAnnoyingEntities.put(hostile, new TimerGame(Float.POSITIVE_INFINITY));
                                    }
                                } else {
                                    _closeAnnoyingEntities.put(hostile, new TimerGame(0));
                                }
                                _closeAnnoyingEntities.get(hostile).reset();
                            }
                            if (_closeAnnoyingEntities.get(hostile).elapsed()) {
                                toDealWith.add(hostile);
                            }
                        } else {
                            _closeAnnoyingEntities.remove(hostile);
                        }
                    }
                }
            }

            // Clear dead/non existing hostiles
            List<Entity> toRemove = new ArrayList<>();
            if (!_closeAnnoyingEntities.keySet().isEmpty()) {
                for (Entity check : _closeAnnoyingEntities.keySet()) {
                    if (!check.isEntityAlive()) {
                        toRemove.add(check);
                    }
                }
            }
            if (!toRemove.isEmpty()) {
                for (Entity remove : toRemove) _closeAnnoyingEntities.remove(remove);
            }
            int numberOfProblematicEntities = toDealWith.size();
            if (!toDealWith.isEmpty()) {
                for (Entity ToDealWith : toDealWith) {
                    if (ToDealWith.getClass() == EntitySlime.class || ToDealWith.getClass() == EntityMagmaCube.class) {
                        numberOfProblematicEntities = 1;
                        break;
                    }
                }
            }
            if (numberOfProblematicEntities > 0) {

                // Depending on our weapons/armor, we may chose to straight up kill hostiles if we're not dodging their arrows.

                // wood 0 : 1 skeleton
                // stone 1 : 1 skeleton
                // iron 2 : 2 hostiles
                // diamond 3 : 3 hostiles
                // netherite 4 : 4 hostiles

                // Armor: (do the math I'm not boutta calculate this)
                // leather: ?1 skeleton
                // iron: ?2 hostiles
                // diamond: ?3 hostiles

                // 7 is full set of leather
                // 15 is full set of iron.
                // 20 is full set of diamond.
                // Diamond+netherite have bonus "toughness" parameter (we can simply add them I think, for now.)
                // full diamond has 8 bonus toughness
                // full netherite has 12 bonus toughness
                int armor = mod.getPlayer().getTotalArmorValue();
                float damage = bestSword == null ? 0 : (1 + bestSword.getAttackDamage());
                boolean hasShield = mod.getItemStorage().hasItem(Items.SHIELD) ||
                        mod.getItemStorage().hasItemInOffhand(Items.SHIELD);
                int shield = hasShield ? 20 : 0;
                int canDealWith = (int) Math.ceil((armor * 3.6 / 20.0) + (damage * 0.8) + (shield));
                canDealWith += 1;
                if (canDealWith > numberOfProblematicEntities) {
                    // We can deal with it.
                    _runAwayTask = null;
                    for (Entity _dealWith : toDealWith) {
                        if (_dealWith instanceof EntitySkeleton || _dealWith instanceof EntityWitch ||
                                /*_dealWith instanceof PillagerEntity ||*/ _dealWith instanceof EntityPigZombie ||
                                _dealWith instanceof EntityStray) {
                            setTask(new KillEntitiesTask(_dealWith.getClass()));
                            return 65;
                        }
                        setTask(new KillEntitiesTask(_dealWith.getClass()));
                        return 65;
                    }
                    return 65;
                } else {
                    // We can't deal with it
//                    for (Entity entity : toDealWith) {
//                        mod.getBlockTracker().requestBlockUnreachable(entity.getPosition(), 0);
//                    }

                    _runAwayTask = new RunAwayFromHostilesTask(DANGER_KEEP_DISTANCE, true);
                    setTask(_runAwayTask);
                    return 80;
                }
            }
        }
        // By default if we aren't "immediately" in danger but were running away, keep running away until we're good.
        if (_runAwayTask != null && !_runAwayTask.isFinished(mod)) {
            setTask(_runAwayTask);
            return _cachedLastPriority;
        } else {
            _runAwayTask = null;
        }
        return 0;
    }

    private BlockPos isInsideFireAndOnFire(BaritonePlus mod) {
        boolean onFire = mod.getPlayer().isBurning();
        if (!onFire) return null;
        BlockPos p = mod.getPlayer().getPosition();
        BlockPos[] toCheck = new BlockPos[]{
                p,
                p.add(1, 0, 0),
                p.add(1, 0, -1),
                p.add(0, 0, -1),
                p.add(-1, 0, -1),
                p.add(-1, 0, 0),
                p.add(-1, 0, 1),
                p.add(0, 0, 1),
                p.add(1, 0, 1)
        };
        for (BlockPos check : toCheck) {
            Block b = mod.getWorld().getBlockState(check).getBlock();
            if (b instanceof BlockFire) {
                return check;
            }
        }
        return null;
    }

    private void putOutFire(BaritonePlus mod, BlockPos pos) {
        LookHelper.lookAt(mod, pos);
        Baritone b = mod.getClientBaritone();
        if (LookHelper.isLookingAt(mod, pos)) {
            b.getPathingBehavior().requestPause();
            b.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        }
    }

    private void doForceField(BaritonePlus mod) {

        _killAura.tickStart();

        // Hit all hostiles close to us.
        List<Entity> entities = mod.getEntityTracker().getCloseEntities();
        try {
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    boolean shouldForce = false;
                    if (mod.getBehaviour().shouldExcludeFromForcefield(entity)) continue;
                    if (entity instanceof EntityMob) {
                        if (EntityHelper.isGenerallyHostileToPlayer(mod, entity)) {
                            if (LookHelper.seesPlayer(entity, mod.getPlayer(), 10)) {
                                shouldForce = true;
                            }
                        }
                    } else if (entity instanceof EntityFireball) {
                        // Ghast ball
                        shouldForce = true;
                    } else if (entity instanceof EntityPlayer player && mod.getBehaviour().shouldForceFieldPlayers()) {
                        if (!player.equals(mod.getPlayer())) {
                            String name = player.getName();
                            if (!mod.getButler().isUserAuthorized(name)) {
                                shouldForce = true;
                            }
                        }
                    }
                    if (shouldForce) {
                        applyForceField(entity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        _killAura.tickEnd(mod);
    }

    private void applyForceField(Entity entity) {
        _killAura.applyAura(entity);
    }

    private EntityCreeper getClosestFusingCreeper(BaritonePlus mod) {
        double worstSafety = Float.POSITIVE_INFINITY;
        EntityCreeper target = null;
        try {
            List<EntityCreeper> creepers = mod.getEntityTracker().getTrackedEntities(EntityCreeper.class);
            if (!creepers.isEmpty()) {
                for (EntityCreeper creeper : creepers) {
                    if (creeper == null) continue;
                    if (((EntityCreeperAccessor) creeper).fuseTime() < 0.001) continue;

                    // We want to pick the closest creeper, but FIRST pick creepers about to blow
                    // At max fuse, the cost goes to basically zero.
                    double safety = getCreeperSafety(mod.getPlayer().getPositionVector(), creeper);
                    if (safety < worstSafety) {
                        target = creeper;
                    }
                }
            }
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            // IDK why but these exceptions happen sometimes. It's extremely bizarre and I have no idea why.
            Debug.logWarning("Weird Exception caught and ignored while scanning for creepers: " + e.getMessage());
            return target;
        }
        return target;
    }

    private boolean isProjectileClose(BaritonePlus mod) {
        List<CachedProjectile> projectiles = mod.getEntityTracker().getProjectiles();
        try {
            if (!projectiles.isEmpty()) {
                for (CachedProjectile projectile : projectiles) {
                    if (projectile.position.squareDistanceTo(mod.getPlayer().getPositionVector()) < 150) {
                        boolean isGhastBall = projectile.projectileType == EntityFireball.class;
                        if (isGhastBall) {
                            Optional<Entity> ghastBall = mod.getEntityTracker().getClosestEntity(EntityFireball.class);
                            Optional<Entity> ghast = mod.getEntityTracker().getClosestEntity(EntityGhast.class);
                            if (ghastBall.isPresent() && ghast.isPresent() && _runAwayTask == null) {
                                mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                                LookHelper.lookAt(mod, ghast.get().getPositionEyes(1.0F));
                            }
                            return false;
                            // Ignore ghast balls
                        }
                        
                        if (projectile.projectileType == EntityDragonFireball.class) {
                            // Ignore dragon fireballs
                            return false;
                        }

                        Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, mod.getPlayer());

                        Vec3d delta = mod.getPlayer().getPositionVector().subtract(expectedHit);

                        //Debug.logMessage("EXPECTED HIT OFFSET: " + delta + " ( " + projectile.gravity + ")");

                        double horizontalDistanceSq = delta.x * delta.x + delta.z * delta.z;
                        double verticalDistance = Math.abs(delta.y);
                        if (horizontalDistanceSq < ARROW_KEEP_DISTANCE_HORIZONTAL * ARROW_KEEP_DISTANCE_HORIZONTAL && verticalDistance < ARROW_KEEP_DISTANCE_VERTICAL) {
                            if (_runAwayTask == null) {
                                mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                                LookHelper.lookAt(mod, projectile.position);
                            }
                            return true;
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
        return false;
    }

    private Optional<Entity> getUniversallyDangerousMob(BaritonePlus mod) {
        // Wither skeletons are dangerous because of the wither effect. Oof kinda obvious.
        // If we merely force field them, we will run into them and get the wither effect which will kill us.
        /*Optional<Entity> warden = mod.getEntityTracker().getClosestEntity(WardenEntity.class);
        if (warden.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (warden.get().getDistanceSq(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, warden.get())) {
                return warden;
            }
        }*/
        Optional<Entity> wither = mod.getEntityTracker().getClosestEntity(EntityWither.class);
        if (wither.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (wither.get().getDistanceSq(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, wither.get())) {
                return wither;
            }
        }
        Optional<Entity> witherSkeleton = mod.getEntityTracker().getClosestEntity(EntityWitherSkeleton.class);
        if (witherSkeleton.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (witherSkeleton.get().getDistanceSq(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, witherSkeleton.get())) {
                return witherSkeleton;
            }
        }
        // Hoglins are dangerous because we can't push them with the force field.
        // If we merely force field them and stand still our health will slowly be chipped away until we die
        /*Optional<Entity> hoglin = mod.getEntityTracker().getClosestEntity(HoglinEntity.class);
        if (hoglin.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (hoglin.get().getDistanceSq(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, hoglin.get())) {
                return hoglin;
            }
        }
        Optional<Entity> zoglin = mod.getEntityTracker().getClosestEntity(ZoglinEntity.class);
        if (zoglin.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (zoglin.get().getDistanceSq(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, zoglin.get())) {
                return zoglin;
            }
        }
        Optional<Entity> piglinBrute = mod.getEntityTracker().getClosestEntity(PiglinBruteEntity.class);
        if (piglinBrute.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (piglinBrute.get().getDistanceSq(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, piglinBrute.get())) {
                return piglinBrute;
            }
        }*/
        Optional<Entity> zombiePig = mod.getEntityTracker().getClosestEntity(EntityPigZombie.class);
        if (zombiePig.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (zombiePig.get().getDistanceSq(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, zombiePig.get())) {
                return zombiePig;
            }
        }
        return Optional.empty();
    }

    private boolean isInDanger(BaritonePlus mod) {
        Optional<Entity> witch = mod.getEntityTracker().getClosestEntity(EntityWitch.class);
        boolean hasFood = mod.getFoodChain().hasFood();
        float health = mod.getPlayer().getHealth();
        if (health <= 10 && hasFood && witch.isEmpty()) {
            return true;
        }
        if (mod.getPlayer().isPotionActive(MobEffects.WITHER) ||
                (mod.getPlayer().isPotionActive(MobEffects.POISON) && witch.isEmpty())) {
            return true;
        }
        if (isVulnurable(mod)) {
            // If hostile mobs are nearby...
            try {
                EntityPlayerSP player = mod.getPlayer();
                List<Entity> hostiles = mod.getEntityTracker().getHostiles();
                if (!hostiles.isEmpty()) {
                    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                        for (Entity entity : hostiles) {
                            if (entity.getDistanceSq(player) <= SAFE_KEEP_DISTANCE && !mod.getBehaviour().shouldExcludeFromForcefield(entity) && EntityHelper.isAngryAtPlayer(mod, entity)) {
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Debug.logWarning("Weird multithread exception. Will fix later.");
            }
        }
        return false;
    }

    private boolean isVulnurable(BaritonePlus mod) {
        int armor = mod.getPlayer().getTotalArmorValue();
        float health = mod.getPlayer().getHealth();
        if (armor <= 15 && health < 3) return true;
        if (armor < 10 && health < 10) return true;
        return armor < 5 && health < 18;
    }

    public void setTargetEntity(Entity entity) {
        _targetEntity = entity;
    }

    public void resetTargetEntity() {
        _targetEntity = null;
    }

    public void setForceFieldRange(double range) {
        _killAura.setRange(range);
    }

    public void resetForceField() {
        _killAura.setRange(Double.POSITIVE_INFINITY);
    }

    public boolean isDoingAcrobatics() {
        return _doingFunkyStuff;
    }

    public boolean isPuttingOutFire() {
        return _wasPuttingOutFire;
    }

    @Override
    public boolean isActive() {
        // We're always checking for mobs
        return true;
    }

    @Override
    protected void onTaskFinish(BaritonePlus mod) {
        // Task is done, so I guess we move on?
    }

    @Override
    public String getName() {
        return "Mob Defense";
    }

    public CustomBaritoneGoalTask getRunAwayTask() {
        return _runAwayTask;
    }
}