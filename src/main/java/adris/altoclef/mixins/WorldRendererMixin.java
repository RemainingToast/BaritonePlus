package adris.altoclef.mixins;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    /**
     * Full Bright
     * @src <a href="https://github.com/MeteorDevelopment/meteor-client/blob/262b9d6a127afc28f8040896b403fae60f1efd4a/src/main/java/meteordevelopment/meteorclient/mixin/WorldRendererMixin.java#L126">...</a>
     */
    @ModifyVariable(method = "getLightmapCoordinates(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "STORE"), ordinal = 0)
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        // TODO Config
        return Math.max(10, sky);
    }

}
