package baritone.plus.main.tasks.slot;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.slots.Slot;
import net.minecraft.screen.slot.ClickType;

public class ClickSlotTask extends Task {

    private final Slot _slot;
    private final int _mouseButton;
    private final ClickType _type;

    private boolean _clicked = false;

    public ClickSlotTask(Slot slot, int mouseButton, ClickType type) {
        _slot = slot;
        _mouseButton = mouseButton;
        _type = type;
    }

    public ClickSlotTask(Slot slot, ClickType type) {
        this(slot, 0, type);
    }

    public ClickSlotTask(Slot slot, int mouseButton) {
        this(slot, mouseButton, ClickType.PICKUP);
    }

    public ClickSlotTask(Slot slot) {
        this(slot, ClickType.PICKUP);
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        _clicked = false;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (mod.getSlotHandler().canDoSlotAction()) {
            mod.getSlotHandler().clickSlot(_slot, _mouseButton, _type);
            mod.getSlotHandler().registerSlotAction();
            _clicked = true;
        }
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof ClickSlotTask task) {
            return task._mouseButton == _mouseButton && task._type == _type && task._slot.equals(_slot);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Clicking " + _slot.toString();
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return _clicked;
    }
}
