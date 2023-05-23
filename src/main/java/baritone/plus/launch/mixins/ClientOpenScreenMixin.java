package baritone.plus.launch.mixins;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.ScreenOpenEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MinecraftClient.class)
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
}
