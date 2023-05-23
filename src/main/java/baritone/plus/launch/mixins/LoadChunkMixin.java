package baritone.plus.launch.mixins;

/*@Mixin(ClientChunkManager.class)
public class LoadChunkMixin {

    @Inject(
            method = "loadChunkFromPacket",
            at = @At("RETURN")
    )
    private void onLoadChunk(int x, int z, PacketByteBuf buf, NbtCompound nbt, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> ci) {
        EventBus.publish(new ChunkLoadEvent(ci.getReturnValue()));
    }

    @Inject(
            method = "unload",
            at = @At("TAIL")
    )
    private void onChunkUnload(int x, int z, CallbackInfo ci) {
        EventBus.publish(new ChunkUnloadEvent(new ChunkPos(x, z)));
    }
}*/
