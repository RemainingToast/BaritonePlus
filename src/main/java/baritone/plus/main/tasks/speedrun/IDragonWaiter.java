package baritone.plus.main.tasks.speedrun;

import net.minecraft.util.math.BlockPos;

public interface IDragonWaiter {
    void setExitPortalTop(BlockPos top);

    void setPerchState(boolean perching);
}
