package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.speedrun.MarvionBeatMinecraftTask;
import baritone.api.command.argument.IArgConsumer;

public class SpeedrunCommand extends PlusCommand {
    public SpeedrunCommand() {
        super(new String[]{"speedrun", "marvion"}, "Beats the game (Pretty much single player only).");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.runUserTask(new MarvionBeatMinecraftTask());
    }
}