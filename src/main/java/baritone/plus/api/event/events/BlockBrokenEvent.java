package baritone.plus.api.event.events;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class BlockBrokenEvent {
    public BlockPos blockPos;
    public BlockState blockState;
    public PlayerEntity player;
}
