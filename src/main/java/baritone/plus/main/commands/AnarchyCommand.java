package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.anarchy.EscapeSpawnTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class AnarchyCommand extends PlusCommand {

    public AnarchyCommand() {
        super(new String[]{"spawn"}, "Gears up and escapes spawn.");
    }

    @Override
    public void call(BaritonePlus mod, String s, IArgConsumer args) {
        mod.runUserTask(new EscapeSpawnTask(), this::finish);
    }
}