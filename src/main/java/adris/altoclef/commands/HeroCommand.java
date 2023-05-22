package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.HeroTask;
import baritone.api.command.argument.IArgConsumer;

public class HeroCommand extends PlusCommand {
    public HeroCommand() {
        super(new String[]{"hero", "defend"}, "Kill all hostile mobs.");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.runUserTask(new HeroTask());
    }
}
