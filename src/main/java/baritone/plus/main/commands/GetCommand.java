package baritone.plus.main.commands;

import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.api.command.datatypes.ForItemOptionalMeta;
import baritone.plus.api.command.datatypes.ItemById;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;

import java.util.stream.Stream;

public class GetCommand extends PlusCommand {

    public GetCommand() {
        super(new String[]{"get", "collect"}, "Get an item/resource");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        ItemTarget[] items = args.getDatatypeFor(ForItemOptionalMeta.INSTANCE).items;
        Task targetTask;
        if (items == null || items.length == 0) {
            mod.log("You must specify at least one item!");
            finish();
            return;
        }
        if (items.length == 1) {
            targetTask = TaskCatalogue.getItemTask(items[0]);
        } else {
            targetTask = TaskCatalogue.getSquashedItemTask(items);
        }
        if (targetTask != null) {
            mod.runUserTask(targetTask);
        } else {
            finish();
        }
    }

    @Override
    public Stream<String> tabComplete(String s, IArgConsumer args) throws CommandException {
        return args.tabCompleteDatatype(ItemById.INSTANCE);
    }
}