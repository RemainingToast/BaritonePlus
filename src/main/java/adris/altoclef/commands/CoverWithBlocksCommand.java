package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.CoverWithBlocksTask;
import baritone.api.command.argument.IArgConsumer;

public class CoverWithBlocksCommand extends PlusCommand {
    public CoverWithBlocksCommand() {
        super(new String[]{"coverwithblocks", "netherblocks"}, "Cover nether lava with blocks.");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.runUserTask(new CoverWithBlocksTask(), this::finish);
    }
}
