package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.anarchy.EscapeSpawnTask;
import baritone.api.command.argument.IArgConsumer;

public class AnarchyCommand extends PlusCommand {

    public AnarchyCommand() {
        super(new String[]{"spawn"}, "Gears up and escapes spawn.");
    }

    @Override
    public void call(AltoClef mod, String s, IArgConsumer args) {
        mod.runUserTask(new EscapeSpawnTask(), this::finish);
    }
}