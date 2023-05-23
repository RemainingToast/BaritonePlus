package baritone.plus.launch.mixins;

/*@Mixin(Minecraft.class)
public final class ClientOpenScreenMixin {
    @Inject(
            method = "setScreen",
            at = @At("HEAD")
    )
    private void onScreenOpenBegin(@Nullable Screen screen, CallbackInfo ci) {
        EventBus.publish(new ScreenOpenEvent(screen, true));
    }

    @Inject(
            method = "setScreen",
            at = @At("TAIL")
    )
    private void onScreenOpenEnd(@Nullable Screen screen, CallbackInfo ci) {
        EventBus.publish(new ScreenOpenEvent(screen, false));
    }
}*/
