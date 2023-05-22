package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import baritone.api.command.argument.IArgConsumer;

public class StopPlusCommand extends PlusCommand {

    public StopPlusCommand() {
        super(new String[]{"stop+", "s+"}, "Stop task runner (stops all automation)");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.getUserTaskChain().cancel(mod);
        this.logDirect("plus canceled");
        finish();
    }
}
