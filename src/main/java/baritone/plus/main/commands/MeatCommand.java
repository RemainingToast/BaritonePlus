package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.api.command.datatypes.NumberDatatype;
import baritone.plus.main.tasks.resources.CollectMeatTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

public class MeatCommand extends PlusCommand {
    public MeatCommand() {
        super(new String[]{"meat"}, "Collects a certain amount of meat"/*, new Arg<>(Integer.class, "count")*/);
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        Number count = args.getDatatypeFor(NumberDatatype.INSTANCE);

        if (count == null) {
            throw new CommandInvalidTypeException(args.get(), "Invalid count argument. Expected a number.");
        }

        mod.runUserTask(new CollectMeatTask(count.intValue()));
    }
}