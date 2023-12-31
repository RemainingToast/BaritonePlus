package baritone.plus.launch.mixins;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.ClientRenderEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public final class ClientUIMixin {
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void clientRender(MatrixStack stack, float tickDelta, CallbackInfo ci) {
        EventBus.publish(new ClientRenderEvent(stack, tickDelta));
    }
}
