package baritone.plus.launch.mixins;

import baritone.plus.main.Debug;
import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.TitleScreenEntryEvent;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class EntryMixin {

    private static boolean _initialized = false;

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        if (!_initialized) {
            _initialized = true;
            Debug.logMessage("Global Init");
            EventBus.publish(new TitleScreenEntryEvent());
        }
    }
}

