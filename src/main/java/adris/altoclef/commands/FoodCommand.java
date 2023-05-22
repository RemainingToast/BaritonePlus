package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.datatypes.NumberDatatype;
import adris.altoclef.tasks.resources.CollectFoodTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

public class FoodCommand extends PlusCommand {
    public FoodCommand() {
        super(new String[]{"food"}, "Collects a certain amount of food"/*, new Arg(Number.class, "count")*/);
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) throws CommandNotEnoughArgumentsException, CommandInvalidTypeException {
        Number count = args.getDatatypeFor(NumberDatatype.INSTANCE);

        if (count == null) {
            throw new CommandInvalidTypeException(args.get(), "Invalid count argument. Expected a number.");
        }

        double countValue = count.doubleValue(); // Convert to double if needed

        mod.runUserTask(new CollectFoodTask(countValue));
    }
}
