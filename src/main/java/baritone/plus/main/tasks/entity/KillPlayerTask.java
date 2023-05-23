package baritone.plus.main.tasks.entity;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.progresscheck.IProgressChecker;
import baritone.plus.api.util.progresscheck.LinearProgressChecker;
import baritone.plus.api.util.progresscheck.ProgressCheckerRetry;
import net.minecraft.entity.Entity;

import java.util.Optional;

/**
 * Kill a player given their username
 */
public class KillPlayerTask extends AbstractKillEntityTask {

    private final String _playerName;

    private final IProgressChecker<Double> _distancePlayerCheck = new ProgressCheckerRetry<>(new LinearProgressChecker(5, -2), 3);

    public KillPlayerTask(String name) {
        super(7, 1);
        _playerName = name;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        // If we're closer to the player, our task isn't bad.
        Optional<Entity> player = getEntityTarget(mod);
        if (player.isEmpty()) {
            _distancePlayerCheck.reset();
        } else {
            double distSq = player.get().getDistanceSq(mod.getPlayer());
            if (distSq < 10 * 10) {
                _distancePlayerCheck.reset();
            }
            _distancePlayerCheck.setProgress(-1 * distSq);
            if (!_distancePlayerCheck.failed()) {
                _progress.reset();
            }
        }
        return super.onTick(mod);
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        if (other instanceof KillPlayerTask task) {
            return task._playerName.equals(_playerName);
        }
        return false;
    }

    @Override
    protected Optional<Entity> getEntityTarget(BaritonePlus mod) {
        if (mod.getEntityTracker().isPlayerLoaded(_playerName)) {
            return mod.getEntityTracker().getPlayerEntity(_playerName).map(Entity.class::cast);
        }
        return Optional.empty();
    }

    @Override
    protected String toDebugString() {
        return "Punking " + _playerName;
    }
}
