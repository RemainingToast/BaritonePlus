package baritone.plus.api.control;

import baritone.api.utils.input.Input;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Sometimes we want to trigger a "press" for one frame, or do other input forcing.
 * <p>
 * Dealing with keeping track of a press and timing each time you do this is annoying.
 * <p>
 * For some reason using baritone's "Forcestate" doesn't always work, perhaps that's my bad.
 * <p>
 * But this will alleviate all confusion.
 */
@SuppressWarnings("UnnecessaryDefault")
public class InputControls {

    private final Queue<Input> _toUnpress = new ArrayDeque<>();
    private final Set<Input> _waitForRelease = new HashSet<>(); // a click requires a release.

    private static KeyBinding inputToKeyBinding(Input input) {
        GameSettings o = Minecraft.getMinecraft().gameSettings;
        return switch (input) {
            case MOVE_FORWARD -> o.keyBindForward;
            case MOVE_BACK -> o.keyBindBack;
            case MOVE_LEFT -> o.keyBindLeft;
            case MOVE_RIGHT -> o.keyBindRight;
            case CLICK_LEFT -> o.keyBindAttack;
            case CLICK_RIGHT -> o.keyBindUseItem;
            case JUMP -> o.keyBindJump;
            case SNEAK -> o.keyBindSneak;
            case SPRINT -> o.keyBindSprint;
            default -> throw new IllegalArgumentException("Invalid key input/not accounted for: " + input);
        };
    }

    public void tryPress(Input input) {
        // We just pressed, so let us release.
        if (_waitForRelease.contains(input)) {
            return;
        }
        KeyBinding.setKeyBindState(inputToKeyBinding(input).getKeyCode(), true);
        _toUnpress.add(input);
        _waitForRelease.add(input);
    }

    public void hold(Input input) {
        if (!Keyboard.isKeyDown(inputToKeyBinding(input).getKeyCode())) {
            KeyBinding.setKeyBindState(inputToKeyBinding(input).getKeyCode(), true);
        }
    }

    public void release(Input input) {
        KeyBinding.setKeyBindState(inputToKeyBinding(input).getKeyCode(), false);
    }

    public boolean isHeldDown(Input input) {
        return Keyboard.isKeyDown(inputToKeyBinding(input).getKeyCode());
    }

    public void forceLook(float yaw, float pitch) {
        Minecraft.getMinecraft().player.rotationYaw = yaw;
        Minecraft.getMinecraft().player.rotationPitch = pitch;
    }

    // Before the user calls input commands for the frame
    public void onTickPre() {
        while (!_toUnpress.isEmpty()) {
            Input input = _toUnpress.remove();
            KeyBinding.setKeyBindState(inputToKeyBinding(input).getKeyCode(), false);
        }
    }

    // After the user calls input commands for the frame
    public void onTickPost() {
        _waitForRelease.clear();
    }
}
