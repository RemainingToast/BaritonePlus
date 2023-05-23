package baritone.plus.launch.mixins;

/*
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
}*/
