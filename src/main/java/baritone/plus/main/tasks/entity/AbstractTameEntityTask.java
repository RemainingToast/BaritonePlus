package baritone.plus.main.tasks.entity;

import baritone.plus.api.tasks.Task;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractTameEntityTask<E extends Entity> extends AbstractDoToEntityTask {

    private final List<E> _tamedEntities = new ArrayList<>();

    public AbstractTameEntityTask() {
        super(0, -1, -1);
    }

    public AbstractTameEntityTask(int maintainDistance, int combatGuardLR, int combatGuardLFR) {
        super(maintainDistance, combatGuardLR, combatGuardLFR);
    }

    @Override
    protected Task onEntityInteract(BaritonePlus mod, Entity entity) {
        var _entity = (E) entity;
        addTamedEntity(_entity);
        if (entity.getClass().isAssignableFrom(getEntityClass()) && canTame(mod, _entity)) {
            return onTameInteract(mod, _entity);
        }
        return null;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (!_tamedEntities.isEmpty()) {
            var toRemove = new ArrayList<E>();
            for (E entity : _tamedEntities) {
                if (isTamed(mod, entity)) {
                    toRemove.add(entity);
                    Debug.logMessage("Tamed %s", entity.toString());
                    return onTameFinish(mod, entity);
                }
            }
            _tamedEntities.removeAll(toRemove);
        }
        return super.onTick(mod);
    }

    private void addTamedEntity(E entity) {
        if (!_tamedEntities.contains(entity)) {
            _tamedEntities.add(entity);
        }
    }

    protected abstract Class<E> getEntityClass();

    protected abstract boolean canTame(BaritonePlus mod, E entity);

    protected abstract Task onTameInteract(BaritonePlus mod, E entity);

    protected abstract boolean isTamed(BaritonePlus mod, E entity);

    protected abstract Task onTameFinish(BaritonePlus mod, E entity);

//    @Override
//    public boolean isFinished(BaritonePlus mod) {
//        return !_tamedEntities.isEmpty() && _tamedEntities.stream().allMatch(e -> isTamed(mod, e));
//    }

    /**
     * This method finds the best horse among a given list of horses.
     * "Best" is defined as the horse with the highest score,
     * calculated as the sum of its max health, movement speed, and jump strength attributes.
     *
     * @param horses The list of horses to evaluate.
     * @return The horse with the highest score. If the list is empty, null is returned.
     */
    protected <T extends AbstractHorseEntity> T findBestHorse(List<T> horses) {
        return horses.stream()
                .max((horse1, horse2) -> Float.compare(
                        getHorseScore(horse1),
                        getHorseScore(horse2)))
                .orElse(null);
    }

    /**
     * This method calculates a "score" for a given horse.
     * The score is the sum of the horse's max health, movement speed, and jump strength attributes.
     * If any of these attributes are missing (i.e., null), they are considered as 0 in the sum.
     *
     * @param horse The horse to evaluate.
     * @return The calculated score for the horse.
     */
    private float getHorseScore(AbstractHorseEntity horse) {
        var attributes = horse.getAttributes();
        return (float) (
                Optional.ofNullable(attributes.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH))
                        .map(EntityAttributeInstance::getValue).orElse(0.0) +
                Optional.ofNullable(attributes.getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED))
                        .map(EntityAttributeInstance::getValue).orElse(0.0) +
                Optional.ofNullable(attributes.getCustomInstance(EntityAttributes.HORSE_JUMP_STRENGTH))
                        .map(EntityAttributeInstance::getValue).orElse(0.0)
        );
    }
}
