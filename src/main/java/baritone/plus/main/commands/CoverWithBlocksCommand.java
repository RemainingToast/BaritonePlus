package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.construction.CoverWithBlocksTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class CoverWithBlocksCommand extends PlusCommand {
    public CoverWithBlocksCommand() {
        super(new String[]{"coverwithblocks", "netherblocks"}, "Cover nether lava with blocks.");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.runUserTask(new CoverWithBlocksTask(), this::finish);
    }
}
