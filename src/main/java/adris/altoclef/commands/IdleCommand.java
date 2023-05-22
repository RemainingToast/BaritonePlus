package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.IdleTask;
import baritone.api.command.argument.IArgConsumer;

public class IdleCommand extends PlusCommand {
    public IdleCommand() {
        super(new String[]{"idle"}, "Stand still");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.runUserTask(new IdleTask());
    }
}
