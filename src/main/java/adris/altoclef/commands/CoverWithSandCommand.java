package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.CoverWithSandTask;
import baritone.api.command.argument.IArgConsumer;

public class CoverWithSandCommand extends PlusCommand {
    public CoverWithSandCommand() {
        super(new String[]{"coverwithsand", "nethersand"}, "Cover nether lava with sand.");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.runUserTask(new CoverWithSandTask(), this::finish);
    }
}
