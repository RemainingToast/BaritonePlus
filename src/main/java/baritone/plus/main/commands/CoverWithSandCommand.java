package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.construction.CoverWithSandTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class CoverWithSandCommand extends PlusCommand {
    public CoverWithSandCommand() {
        super(new String[]{"coverwithsand", "nethersand"}, "Cover nether lava with sand.");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.runUserTask(new CoverWithSandTask(), this::finish);
    }
}
