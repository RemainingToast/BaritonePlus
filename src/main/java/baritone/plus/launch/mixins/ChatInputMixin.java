package baritone.plus.launch.mixins;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.SendChatEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayNetworkHandler.class)
public final class ChatInputMixin {
    @Inject(
            method = "sendChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sendChatMessage(String content, CallbackInfo ci) {
        SendChatEvent event = new SendChatEvent(content);
        EventBus.publish(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}