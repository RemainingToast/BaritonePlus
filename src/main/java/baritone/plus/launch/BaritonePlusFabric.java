package baritone.plus.launch;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.TitleScreenEntryEvent;
import baritone.plus.main.BaritonePlus;
import net.fabricmc.api.ModInitializer;

public class BaritonePlusFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // As such, nothing will be loaded here but basic initialization.
        EventBus.subscribe(TitleScreenEntryEvent.class, evt -> new BaritonePlus().onInitializeLoad());
    }
}
