package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class StopPlusCommand extends PlusCommand {

    public StopPlusCommand() {
        super(new String[]{"stop+", "s+"}, "Stop task runner (stops all automation)");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.getUserTaskChain().cancel(mod);
        this.logDirect("plus canceled");
        finish();
    }
}
