package baritone.plus.launch;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.TitleScreenEntryEvent;
import baritone.plus.main.BaritonePlus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = BaritonePlusModInit.MODID, name = BaritonePlusModInit.NAME, version = BaritonePlusModInit.VERSION)
public class BaritonePlusModInit {
    public static final String MODID = "baritone-plus";
    public static final String NAME = "Baritone+";
    public static final String VERSION = "1.12.2-beta1";
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // As such, nothing will be loaded here but basic initialization.
        EventBus.subscribe(TitleScreenEntryEvent.class, evt -> new BaritonePlus().onInitializeLoad());
    }
}
