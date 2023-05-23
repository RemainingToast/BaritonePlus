package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.speedrun.BeatMinecraft2Task;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class GamerCommand extends PlusCommand {
    public GamerCommand() {
        super(new String[]{"oldspeedrun","gamer"}, "Beats the game, old style (Single player only)");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.runUserTask(new BeatMinecraft2Task());
    }
}
