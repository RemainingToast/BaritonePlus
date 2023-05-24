package baritone.plus.main.commands;

import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.construction.SchematicBuildTask;

public class BuildCommand extends PlusCommand {
    public BuildCommand()  {
        super(new String[]{"build+"}, "Build a structure from schematic data");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandException {
        // TODO - Args
        mod.runUserTask(new SchematicBuildTask("house.litematic"));
    }
}
