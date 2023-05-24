package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.movement.IdleTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class IdleCommand extends PlusCommand {
    public IdleCommand() {
        super(new String[]{"idle"}, "Stand still");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.runUserTask(new IdleTask());
    }
}
