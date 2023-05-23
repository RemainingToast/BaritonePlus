package baritone.plus.api.trackers;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.PlayerCollidedWithEntityEvent;
import baritone.plus.api.trackers.blacklisting.EntityLocateBlacklist;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.baritone.CachedProjectile;
import baritone.plus.api.util.helpers.BaritoneHelper;
import baritone.plus.api.util.helpers.EntityHelper;
import baritone.plus.api.util.helpers.ProjectileHelper;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.main.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;

/**
 * Keeps track of entities so we can search/grab them.
 */
@SuppressWarnings("rawtypes")
public class EntityTracker extends Tracker {

    private final HashMap<Item, List<EntityItem>> _itemDropLocations = new HashMap<>();
    private final HashMap<Class, List<Entity>> _entityMap = new HashMap<>();

    private final List<Entity> _closeEntities = new ArrayList<>();
    private final List<Entity> _hostiles = new ArrayList<>();

    private final List<CachedProjectile> _projectiles = new ArrayList<>();

    private final HashMap<String, EntityPlayer> _playerMap = new HashMap<>();
    private final HashMap<String, Vec3d> _playerLastCoordinates = new HashMap<>();

    private final EntityLocateBlacklist _entityBlacklist = new EntityLocateBlacklist();

    private final HashMap<EntityPlayer, List<Entity>> _entitiesCollidingWithPlayerAccumulator = new HashMap<>();
    private final HashMap<EntityPlayer, HashSet<Entity>> _entitiesCollidingWithPlayer = new HashMap<>();

    public EntityTracker(TrackerManager manager) {
        super(manager);

        // Listen for player collisions
        EventBus.subscribe(PlayerCollidedWithEntityEvent.class, evt -> registerPlayerCollision(evt.player, evt.other));
    }

    /**
     * Squash a class that may have sub classes into one distinguishable class type.
     * For ease of use.
     *
     * @param type: An entity class that may have a 'simpler' class to squash to
     * @return what the given entity class should be read as/catalogued as.
     */
    private static Class squashType(Class type) {
        // Squash types for ease of use
        if (EntityPlayer.class.isAssignableFrom(type)) {
            return EntityPlayer.class;
        }
        return type;
    }

    private void registerPlayerCollision(EntityPlayer player, Entity entity) {
        if (!_entitiesCollidingWithPlayerAccumulator.containsKey(player)) {
            _entitiesCollidingWithPlayerAccumulator.put(player, new ArrayList<>());
        }
        _entitiesCollidingWithPlayerAccumulator.get(player).add(entity);
    }

    public boolean isCollidingWithPlayer(EntityPlayer player, Entity entity) {
        return _entitiesCollidingWithPlayer.containsKey(player) && _entitiesCollidingWithPlayer.get(player).contains(entity);
    }

    public boolean isCollidingWithPlayer(Entity entity) {
        return isCollidingWithPlayer(_mod.getPlayer(), entity);
    }

    public Optional<EntityItem> getClosestItemDrop(Item... items) {
        return getClosestItemDrop(_mod.getPlayer().getPositionVector(), items);
    }

    public Optional<EntityItem> getClosestItemDrop(Vec3d position, Item... items) {
        return getClosestItemDrop(position, entity -> true, items);
    }

    public Optional<EntityItem> getClosestItemDrop(Vec3d position, ItemTarget... items) {
        return getClosestItemDrop(position, entity -> true, items);
    }

    public Optional<EntityItem> getClosestItemDrop(Predicate<EntityItem> acceptPredicate, Item... items) {
        return getClosestItemDrop(_mod.getPlayer().getPositionVector(), acceptPredicate, items);
    }

    public Optional<EntityItem> getClosestItemDrop(Vec3d position, Predicate<EntityItem> acceptPredicate, Item... items) {
        ensureUpdated();
        ItemTarget[] tempTargetList = new ItemTarget[items.length];
        for (int i = 0; i < items.length; ++i) {
//            tempTargetList[i] = new ItemTarget(items[i]).infinite();
            tempTargetList[i] = new ItemTarget(items[i]);
        }
        return getClosestItemDrop(position, acceptPredicate, tempTargetList);
    }

    public Optional<EntityItem> getClosestItemDrop(Vec3d position, Predicate<EntityItem> acceptPredicate, ItemTarget... targets) {
        ensureUpdated();
        if (targets.length == 0) {
            Debug.logError("You asked for the drop position of zero items... Most likely a typo.");
            return Optional.empty();
        }
        if (!itemDropped(targets)) {
            return Optional.empty();
        }

        EntityItem closestEntity = null;
        float minCost = Float.POSITIVE_INFINITY;
        for (ItemTarget target : targets) {
            for (Item item : target.getMatches()) {
                if (!itemDropped(item)) continue;
                for (EntityItem entity : _itemDropLocations.get(item)) {
                    if (_entityBlacklist.unreachable(entity)) continue;
                    if (!entity.getItem().getItem().equals(item)) continue;
                    if (!acceptPredicate.test(entity)) continue;

                    float cost = (float) BaritoneHelper.calculateGenericHeuristic(position, entity.getPositionVector());
                    if (cost < minCost) {
                        minCost = cost;
                        closestEntity = entity;
                    }
                }
            }
        }
        return Optional.ofNullable(closestEntity);
    }

    public Optional<Entity> getClosestEntity(Class... entityTypes) {
        return getClosestEntity(_mod.getPlayer().getPositionVector(), entityTypes);
    }

    public Optional<Entity> getClosestEntity(Vec3d position, Class... entityTypes) {
        return this.getClosestEntity(position, (entity) -> true, entityTypes);
    }

    public Optional<Entity> getClosestEntity(Predicate<Entity> acceptPredicate, Class... entityTypes) {
        return getClosestEntity(_mod.getPlayer().getPositionVector(), acceptPredicate, entityTypes);
    }

    public Optional<Entity> getClosestEntity(Vec3d position, Predicate<Entity> acceptPredicate, Class... entityTypes) {
        Entity closestEntity = null;
        double minCost = Float.POSITIVE_INFINITY;
        for (Class toFind : entityTypes) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                if (_entityMap.containsKey(toFind)) {
                    for (Entity entity : _entityMap.get(toFind)) {
                        // Don't accept entities that no longer exist
                        if (_entityBlacklist.unreachable(entity)) continue;
                        if (!entity.isEntityAlive()) continue;
                        if (!acceptPredicate.test(entity)) continue;
                        double cost = entity.getDistanceSq(position.x, position.y, position.z);
                        if (cost < minCost) {
                            minCost = cost;
                            closestEntity = entity;
                        }
                    }
                }
            }
        }
        return Optional.ofNullable(closestEntity);
    }

    public boolean itemDropped(Item... items) {
        ensureUpdated();
        for (Item item : items) {
            if (_itemDropLocations.containsKey(item)) {
                // Find a non-blacklisted item
                for (EntityItem entity : _itemDropLocations.get(item)) {
                    if (!_entityBlacklist.unreachable(entity)) return true;
                }
            }
        }
        return false;
    }

    public boolean itemDropped(ItemTarget... targets) {
        ensureUpdated();
        for (ItemTarget target : targets) {
            if (itemDropped(target.getMatches())) return true;
        }
        return false;
    }

    public List<EntityItem> getDroppedItems() {
        ensureUpdated();
        return _itemDropLocations.values().stream().reduce(new ArrayList<>(), (result, drops) -> {
            result.addAll(drops);
            return result;
        });
    }

    public boolean entityFound(Predicate<Entity> shouldAccept, Class... types) {
        ensureUpdated();
        for (Class type : types) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                for (Entity entity : _entityMap.getOrDefault(type, Collections.emptyList())) {
                    if (shouldAccept.test(entity))
                        return true;
                }
            }
        }
        return false;
    }

    public boolean entityFound(Class... types) {
        return entityFound(check -> true, types);
    }

    public <T extends Entity> List<T> getTrackedEntities(Class<T> type) {
        ensureUpdated();
        if (!entityFound(type)) {
            return Collections.emptyList();
        }
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            //noinspection unchecked
            return (List<T>) _entityMap.get(type);
        }
    }

    /**
     * Gets all entities that are within our interact range
     */
    public List<Entity> getCloseEntities() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _closeEntities;
        }
    }

    /**
     * Gets a list of projectiles that we've cached/stored information about.
     */
    public List<CachedProjectile> getProjectiles() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _projectiles;
        }
    }

    public List<Entity> getHostiles() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _hostiles;
        }
    }

    /**
     * Is a player loaded/within render distance?
     *
     * @param name Username on a multiplayer server
     */
    public boolean isPlayerLoaded(String name) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _playerMap.containsKey(name);
        }
    }

    /**
     * Get where we last saw a player, if we saw them at all.
     *
     * @return Username on a multiplayer server.
     */
    public Optional<Vec3d> getPlayerMostRecentPosition(String name) {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return Optional.ofNullable(_playerLastCoordinates.getOrDefault(name, null));
        }
    }

    /**
     * Gets the player entity corresponding to a username, if they're loaded/within render distance.
     *
     * @param name Username on a multiplayer server.
     */
    public Optional<EntityPlayer> getPlayerEntity(String name) {
        if (isPlayerLoaded(name)) {
            synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                return Optional.of(_playerMap.get(name));
            }
        }
        return Optional.empty();
    }

    /**
     * Tells the entity tracker that we were unable to reach this entity.
     */
    public void requestEntityUnreachable(Entity entity) {
        _entityBlacklist.blackListItem(_mod, entity, 3);
    }

    /**
     * Whether we have decided that this entity is unreachable.
     */
    public boolean isEntityReachable(Entity entity) {
        return !_entityBlacklist.unreachable(entity);
    }

    @Override
    protected synchronized void updateState() {
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            _itemDropLocations.clear();
            _entityMap.clear();
            _closeEntities.clear();
            _projectiles.clear();
            _hostiles.clear();
            _playerMap.clear();
            if (Minecraft.getMinecraft().world == null) return;

            // Store/Register All accumulated player collisions for this frame.
            _entitiesCollidingWithPlayer.clear();
            for (Map.Entry<EntityPlayer, List<Entity>> collisions : _entitiesCollidingWithPlayerAccumulator.entrySet()) {
                _entitiesCollidingWithPlayer.put(collisions.getKey(), new HashSet<>());
                _entitiesCollidingWithPlayer.get(collisions.getKey()).addAll(collisions.getValue());
            }
            _entitiesCollidingWithPlayerAccumulator.clear();

            // Loop through all entities and track 'em
            for (Entity entity : Minecraft.getMinecraft().world.getLoadedEntityList()) {

                // Catalogue based on type. Some types may get "squashed" or combined into one.
                Class type = entity.getClass();
                type = squashType(type);

                //noinspection ConstantConditions
                if (entity == null || !entity.isEntityAlive()) continue;

                // Don't catalogue our own player.
                if (type == EntityPlayer.class && entity.equals(_mod.getPlayer())) continue;

                if (!_entityMap.containsKey(type)) {
                    _entityMap.put(type, new ArrayList<>());
                }
                _entityMap.get(type).add(entity);

                if (_mod.getControllerExtras().inRange(entity)) {
                    _closeEntities.add(entity);
                }

                if (entity instanceof EntityItem ientity) {
                    Item droppedItem = ientity.getItem().getItem();

                    // Only cared about GROUNDED item entities
                    if (ientity.onGround || ientity.isInWater() || WorldHelper.isSolid(_mod, ientity.getPosition().down(2)) || WorldHelper.isSolid(_mod, ientity.getPosition().down(3))) {
                        if (!_itemDropLocations.containsKey(droppedItem)) {
                            _itemDropLocations.put(droppedItem, new ArrayList<>());
                        }
                        _itemDropLocations.get(droppedItem).add(ientity);
                    }
                }
                if (entity instanceof EntityMob) {
                    if (EntityHelper.isAngryAtPlayer(_mod, entity)) {

                        // Check if the mob is facing us or is close enough
                        boolean closeEnough = entity.getDistance(_mod.getPlayer()) <= 26;

                        //Debug.logInternal("TARGET: " + hostile.is);
                        if (closeEnough) {
                            _hostiles.add(entity);
                        }
                    }
                } else if (entity instanceof EntityThrowable projEntity) {
                    if (!_mod.getBehaviour().shouldAvoidDodgingProjectile(entity)) {
                        CachedProjectile proj = new CachedProjectile();

                        // Get projectile "inGround" variable
                        boolean inGround = projEntity.onGround;
                        /*if (entity instanceof PersistentProjectileEntity) {
                            inGround = ((PersistentProjectileEntityAccessor) entity).isInGround();
                        }*/

                        // Ignore some of the harlmess projectiles
                        if (projEntity instanceof EntityEnderPearl || projEntity instanceof EntityExpBottle)
                            continue;

                        if (!inGround) {
                            proj.position = projEntity.getPositionVector();
                            proj.velocity = new Vec3d(projEntity.motionX, projEntity.motionY, projEntity.motionZ);
                            proj.gravity = ProjectileHelper.hasGravity(projEntity) ? ProjectileHelper.ARROW_GRAVITY_ACCEL : 0;
                            proj.projectileType = projEntity.getClass();
                            _projectiles.add(proj);
                        }
                    }
                } else if (entity instanceof EntityPlayer player) {
                    String name = player.getName();
                    _playerMap.put(name, player);
                    _playerLastCoordinates.put(name, player.getPositionVector());
                }
            }
        }
    }

    @Override
    protected void reset() {
        // Dirty clears everything else.
        _entityBlacklist.clear();
    }
}
