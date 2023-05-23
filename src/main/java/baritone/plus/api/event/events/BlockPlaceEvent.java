package baritone.plus.api.event.events;

import net.minecraft.block.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockPlaceEvent {
    public BlockPos blockPos;
    public IBlockState blockState;

    public BlockPlaceEvent(BlockPos blockPos, IBlockState blockState) {
        this.blockPos = blockPos;
        this.blockState = blockState;
    }
}
