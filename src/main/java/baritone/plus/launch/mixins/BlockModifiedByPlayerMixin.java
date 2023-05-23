package baritone.plus.launch.mixins;

/*@Mixin(Block.class)
public class BlockModifiedByPlayerMixin {

    @Inject(
            method = "onBreak",
            at = @At("HEAD")
    )
    public void onBlockBroken(World world, BlockPos pos, IBlockState state, PlayerEntity player, CallbackInfo ci) {
        if (player.world == world) {
            BlockBrokenEvent evt = new BlockBrokenEvent();
            evt.blockPos = pos;
            evt.blockState = state;
            evt.player = player;
            EventBus.publish(evt);
        }
    }

    @Inject(
            method = "onPlaced",
            at = @At("HEAD")
    )
    public void onBlockPlaced(World world, BlockPos pos, IBlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        // This one is weirdly unreliable.
        //Debug.logInternal("[TEMP] global place");
        //StaticMixinHookups.onBlockPlaced(world, pos, state, placer, itemStack);
    }

}*/
