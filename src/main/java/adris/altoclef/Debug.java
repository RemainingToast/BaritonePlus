package adris.altoclef;

import baritone.Baritone;
import baritone.api.utils.Helper;
import net.minecraft.client.MinecraftClient;

// TODO: Debug library or use Minecraft's built in debugger
public class Debug {

    public static AltoClef jankModInstance;

    public static void logInternal(String message) {
        // TODO Use a logger
        System.out.println("BaritonePlus: " + message);
    }

    public static void logInternal(String format, Object... args) {
        logInternal(String.format(format, args));
    }

    public static void logMessage(String message) {
        logInternal(message);

        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
            Helper.HELPER.logDirect(message);
        }
    }

    public static void logMessage(String format, Object... args) {
        logMessage(String.format(format, args));
    }

    public static void logWarning(String message) {
        logInternal("WARNING: " + message);
        if (jankModInstance != null && !jankModInstance.getModSettings().shouldHideAllWarningLogs()) {
            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
                Helper.HELPER.logDirect("[WARN] " + message);
            }
        }
    }

    public static void logWarning(String format, Object... args) {
        logWarning(String.format(format, args));
    }

    public static void logError(String message) {
        String stacktrace = getStack(2);
        System.err.println(message);
        System.err.println("at:");
        System.err.println(stacktrace);
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
            Helper.HELPER.logDirect("[ERROR] " + message);
        }
    }

    public static void logError(String format, Object... args) {
        logError(String.format(format, args));
    }

    public static void logStack() {
        logInternal("STACKTRACE: \n" + getStack(2));
    }

    private static String getStack(int toSkip) {
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (toSkip-- <= 0) {
                stacktrace.append(ste.toString()).append("\n");
            }
        }
        return stacktrace.toString();
    }
}
