package baritone.plus.launch.mixins;

/*@Mixin(World.class)
public class WorldBlockModifiedMixin {

    private static boolean hasBlock(IBlockState state, BlockPos pos) {
        return !state.isAir() && state.isSolidBlock(Minecraft.getMinecraft().world, pos);
    }

    @Inject(
            method = "onBlockChanged",
            at = @At("HEAD")
    )
    public void onBlockWasChanged(BlockPos pos, IBlockState oldBlock, IBlockState newBlock, CallbackInfo ci) {
        if (!hasBlock(oldBlock, pos) && hasBlock(newBlock, pos)) {
            BlockPlaceEvent evt = new BlockPlaceEvent(pos, newBlock);
            EventBus.publish(evt);
        }
    }
    //onBlockChanged
}*/
