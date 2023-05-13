package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.BeatAnarchyTask;
import adris.altoclef.tasks.speedrun.MarvionBeatMinecraftTask;

public class AnarchyCommand extends Command {
    public AnarchyCommand() {
        super("anarchy", "Gears up and escapes spawn.");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new BeatAnarchyTask(), this::finish);
    }
}