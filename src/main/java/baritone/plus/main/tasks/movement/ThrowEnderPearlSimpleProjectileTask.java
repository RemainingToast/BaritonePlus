package baritone.plus.main.tasks.movement;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.helpers.LookHelper;
import baritone.plus.api.util.helpers.ProjectileHelper;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.plus.api.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ThrowEnderPearlSimpleProjectileTask extends Task {

    private final TimerGame _thrownTimer = new TimerGame(5);
    private final BlockPos _target;

    private boolean _thrown = false;

    public ThrowEnderPearlSimpleProjectileTask(BlockPos target) {
        _target = target;
    }

    private static boolean cleanThrow(BaritonePlus mod, float yaw, float pitch) {
        Rotation rotation = new Rotation(yaw, -1 * pitch);
        float range = 3f;
        Vec3d delta = LookHelper.toVec3d(rotation).multiply(range);
        Vec3d start = LookHelper.getCameraPos(mod);
        return LookHelper.cleanLineOfSight(start.add(delta), range);
    }

    private static Rotation calculateThrowLook(BaritonePlus mod, BlockPos end) {
        Vec3d start = ProjectileHelper.getThrowOrigin(mod.getPlayer());
        Vec3d endCenter = WorldHelper.toVec3d(end);
        double gravity = ProjectileHelper.THROWN_ENTITY_GRAVITY_ACCEL;
        double speed = 1.5;
        float yaw = LookHelper.getLookRotation(mod, end).getYaw();
        double flatDistance = WorldHelper.distanceXZ(start, endCenter);
        double[] pitches = ProjectileHelper.calculateAnglesForSimpleProjectileMotion(start.y - endCenter.y, flatDistance, speed, gravity);
        double pitch = cleanThrow(mod, yaw, (float) pitches[0]) ? pitches[0] : pitches[1];
        return new Rotation(yaw, -1 * (float) pitch);
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        _thrownTimer.forceElapse();
        _thrown = false;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        // TODO: Unlikely/minor nitpick, but there could be other people throwing ender pearls, which would delay the bot.
        if (mod.getEntityTracker().entityFound(EnderPearlEntity.class)) {
            _thrownTimer.reset();
        }
        if (_thrownTimer.elapsed()) {
            if (mod.getSlotHandler().forceEquipItem(Items.ENDER_PEARL)) {
                Rotation lookTarget = calculateThrowLook(mod, _target);
                LookHelper.lookAt(mod, lookTarget);
                if (LookHelper.isLookingAt(mod, lookTarget)) {
                    mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                    _thrown = true;
                    _thrownTimer.reset();
                }
            }
        }
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return _thrown && _thrownTimer.elapsed() || (!_thrown && !mod.getItemStorage().hasItem(Items.ENDER_PEARL));
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ThrowEnderPearlSimpleProjectileTask task) {
            return task._target.equals(_target);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Simple Ender Pearling to " + _target;
    }
}
