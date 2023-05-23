package baritone.plus.main.chains;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.api.tasks.TaskChain;
import baritone.plus.api.tasks.TaskRunner;
import baritone.plus.api.util.time.TimerGame;
import baritone.plus.api.util.time.TimerReal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import baritone.plus.launch.mixins.DeathScreenAccessor;
import net.minecraft.text.Text;

public class DeathMenuChain extends TaskChain {

    // Sometimes we fuck up, so we might want to retry considering the death screen.
    private final TimerReal _deathRetryTimer = new TimerReal(8);
    private final TimerGame _reconnectTimer = new TimerGame(1);
    private final TimerGame _waitOnDeathScreenBeforeRespawnTimer = new TimerGame(2);
    private ServerInfo _prevServerEntry = null;
    private boolean _reconnecting = false;
    private int _deathCount = 0;
    private Class<?> _prevScreen = null;


    public DeathMenuChain(TaskRunner runner) {
        super(runner);
    }

    private boolean shouldAutoRespawn(BaritonePlus mod) {
        return mod.getModSettings().isAutoRespawn();
    }

    private boolean shouldAutoReconnect(BaritonePlus mod) {
        return mod.getModSettings().isAutoReconnect();
    }

    @Override
    protected void onStop(BaritonePlus mod) {

    }

    @Override
    public void onInterrupt(BaritonePlus mod, TaskChain other) {

    }

    @Override
    protected void onTick(BaritonePlus mod) {

    }

    @Override
    public float getPriority(BaritonePlus mod) {
        //Minecraft.getMinecraft().getCurrentServerEntry().address;
//        Minecraft.getMinecraft().
        Screen screen = Minecraft.getMinecraft().currentScreen;

        // This might fix Weird fail to respawn that happened only once
        if (_prevScreen == DeathScreen.class) {
            if (_deathRetryTimer.elapsed()) {
                Debug.logMessage("(RESPAWN RETRY WEIRD FIX...)");
                _deathRetryTimer.reset();
                _prevScreen = null;
            }
        } else {
            _deathRetryTimer.reset();
        }
        // Keep track of the last server we were on so we can re-connect.
        if (BaritonePlus.inGame()) {
            _prevServerEntry = Minecraft.getMinecraft().getCurrentServerEntry();
        }

        if (screen instanceof DeathScreen) {
            if (_waitOnDeathScreenBeforeRespawnTimer.elapsed()) {
                _waitOnDeathScreenBeforeRespawnTimer.reset();
                if (shouldAutoRespawn(mod)) {
                    _deathCount++;
                    Debug.logMessage("RESPAWNING... (this is death #" + _deathCount + ")");
                    assert Minecraft.getMinecraft().player != null;
                    Text deathText = ((DeathScreenAccessor) screen).getMessage();
                    String deathMessage = deathText == null ? "" : deathText.getString(); //"(not implemented yet)"; //screen.children().toString();
                    Minecraft.getMinecraft().player.requestRespawn();
                    Minecraft.getMinecraft().setScreen(null);
                    for (String i :  mod.getModSettings().getDeathCommand().split(" & ")) {
                        String command = i.replace("{deathmessage}", deathMessage);
                        String prefix = mod.getModSettings().getCommandPrefix();
                        while (Minecraft.getMinecraft().player.isAlive()) {
                            if (!command.equals("")){
                                if (command.startsWith(prefix)) {
                                    /*AltoClef.getCommandExecutor().execute(command, () -> {

                                    }, Throwable::printStackTrace);*/
                                } else if (command.startsWith("/")) {
                                    Minecraft.getMinecraft().player.networkHandler.sendChatCommand(command.substring(1));
                                } else {
                                    Minecraft.getMinecraft().player.networkHandler.sendChatMessage(command);
                                }
                            }
                        }
                    }
                } else {
                    // Cancel if we die and are not auto-respawning.
                    mod.cancelUserTask();
                }
            }
        } else {
            if (BaritonePlus.inGame()) {
                _waitOnDeathScreenBeforeRespawnTimer.reset();
            }
            if (screen instanceof DisconnectedScreen) {
                if (shouldAutoReconnect(mod)) {
                    Debug.logMessage("RECONNECTING: Going to Multiplayer Screen");
                    _reconnecting = true;
                    Minecraft.getMinecraft().setScreen(new MultiplayerScreen(new TitleScreen()));
                } else {
                    // Cancel if we disconnect and are not auto-reconnecting.
                    mod.cancelUserTask();
                }
            } else if (screen instanceof MultiplayerScreen && _reconnecting && _reconnectTimer.elapsed()) {
                _reconnectTimer.reset();
                Debug.logMessage("RECONNECTING: Going ");
                _reconnecting = false;

                if (_prevServerEntry == null) {
                    Debug.logWarning("Failed to re-connect to server, no server entry cached.");
                } else {
                    Minecraft client = Minecraft.getMinecraft();
                    ConnectScreen.connect(screen, client, ServerAddress.parse(_prevServerEntry.address), _prevServerEntry);
                    //client.setScreen(new ConnectScreen(screen, client, _prevServerEntry));
                }
            }
        }
        if (screen != null) {
//            System.out.println(screen.getClass());
            _prevScreen = screen.getClass();
        }
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getName() {
        return "Death Menu Respawn Handling";
    }
}
