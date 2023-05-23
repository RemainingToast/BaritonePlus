package baritone.plus.api.control;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.BlockBreakingCancelEvent;
import baritone.plus.api.event.events.BlockBreakingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class PlayerExtraController {

    private final BaritonePlus _mod;
    private BlockPos _blockBreakPos;
    private double _blockBreakProgress;

    public PlayerExtraController(BaritonePlus mod) {
        _mod = mod;

        EventBus.subscribe(BlockBreakingEvent.class, evt -> onBlockBreak(evt.blockPos, evt.progress));
        EventBus.subscribe(BlockBreakingCancelEvent.class, evt -> onBlockStopBreaking());
    }

    private void onBlockBreak(BlockPos pos, double progress) {
        _blockBreakPos = pos;
        _blockBreakProgress = progress;
    }

    private void onBlockStopBreaking() {
        _blockBreakPos = null;
        _blockBreakProgress = 0;
    }

    public BlockPos getBreakingBlockPos() {
        return _blockBreakPos;
    }

    public boolean isBreakingBlock() {
        return _blockBreakPos != null;
    }

    public double getBreakingBlockProgress() {
        return _blockBreakProgress;
    }

    public boolean inRange(Entity entity) {
        return _mod.getPlayer().isInRange(entity, _mod.getModSettings().getEntityReachRange());
    }

    public void attack(Entity entity) {
        if (inRange(entity)) {
            _mod.getController().attackEntity(_mod.getPlayer(), entity);
            _mod.getPlayer().swingHand(Hand.MAIN_HAND);
        }
    }
}
