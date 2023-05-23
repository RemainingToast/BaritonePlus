package baritone.plus.api.util.time;

import baritone.plus.launch.mixins.ClientConnectionAccessor;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;

// Simple timer
public class TimerGame extends BaseTimer {

    private NetHandlerPlayClient _lastConnection;

    public TimerGame(double intervalSeconds) {
        super(intervalSeconds);
    }

    private static double getTime(NetHandlerPlayClient connection) {
        if (connection == null) return 0;
        return (double) ((ClientConnectionAccessor) connection).getTicks() / 20.0;
    }

    @Override
    protected double currentTime() {
        if (!BaritonePlus.inGame()) {
            Debug.logError("Running game timer while not in game.");
            return 0;
        }
        // If we change connections, our game time will also be reset. In that case, offset our time to reflect that change.
        NetHandlerPlayClient currentConnection = null;
        if (Minecraft.getMinecraft().getConnection() != null) {
            currentConnection = Minecraft.getMinecraft().getConnection();
        }
        if (currentConnection != _lastConnection) {
            if (_lastConnection != null) {
                double prevTimeTotal = getTime(_lastConnection);
                Debug.logInternal("(TimerGame: New connection detected, offsetting by " + prevTimeTotal + " seconds)");
                setPrevTimeForce(getPrevTime() - prevTimeTotal);
            }
            _lastConnection = currentConnection;
        }
        // Use ticks for timing. 20TPS is normal, if we go slower that's fine.
        // Adding a "mod" argument here would be hell across the board. Not happening.
        return getTime(currentConnection);
    }
}
