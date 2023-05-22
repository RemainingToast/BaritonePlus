package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import baritone.api.command.argument.IArgConsumer;

public class GamerCommand extends PlusCommand {
    public GamerCommand() {
        super(new String[]{"oldspeedrun","gamer"}, "Beats the game, old style (Single player only)");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.runUserTask(new BeatMinecraft2Task());
    }
}
