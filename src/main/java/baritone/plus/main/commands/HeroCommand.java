package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.entity.HeroTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class HeroCommand extends PlusCommand {
    public HeroCommand() {
        super(new String[]{"hero", "defend"}, "Kill all hostile mobs.");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.runUserTask(new HeroTask());
    }
}
