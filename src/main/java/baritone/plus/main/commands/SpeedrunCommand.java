package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.speedrun.MarvionBeatMinecraftTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class SpeedrunCommand extends PlusCommand {
    public SpeedrunCommand() {
        super(new String[]{"speedrun", "marvion"}, "Beats the game (Pretty much single player only).");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.runUserTask(new MarvionBeatMinecraftTask());
    }
}