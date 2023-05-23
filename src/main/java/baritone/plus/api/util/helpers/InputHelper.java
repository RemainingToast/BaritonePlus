package baritone.plus.api.util.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputUtil;

public class InputHelper {

    public static boolean isKeyPressed(int code) {
        return InputUtil.isKeyPressed(Minecraft.getMinecraft().getWindow().getHandle(), code);
    }
}
