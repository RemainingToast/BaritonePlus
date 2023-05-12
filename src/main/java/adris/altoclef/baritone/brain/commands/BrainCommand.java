package adris.altoclef.baritone.brain.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.baritone.brain.tasks.BrainTask;

public class BrainCommand extends Command {
    public BrainCommand() throws CommandException {
        super("brain", "Activates ChatGPT Brain");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        mod.runUserTask(new BrainTask(), this::finish);
    }

}
