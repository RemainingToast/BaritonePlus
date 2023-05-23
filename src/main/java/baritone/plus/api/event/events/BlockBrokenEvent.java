package baritone.plus.api.event.events;

import net.minecraft.block.IBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class BlockBrokenEvent {
    public BlockPos blockPos;
    public IBlockState blockState;
    public PlayerEntity player;
}
