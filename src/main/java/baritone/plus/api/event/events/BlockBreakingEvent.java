package baritone.plus.api.event.events;

import net.minecraft.util.math.BlockPos;

public class BlockBreakingEvent {
    public BlockPos blockPos;
    public double progress;

    public BlockBreakingEvent(BlockPos blockPos, double progress) {
        this.blockPos = blockPos;
        this.progress = progress;
    }
}
