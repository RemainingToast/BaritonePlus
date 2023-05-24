package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.api.command.datatypes.ForItemOptionalMeta;
import baritone.plus.api.command.datatypes.ItemList;
import baritone.plus.api.tasks.Task;
import baritone.plus.main.ui.MessagePriority;
import baritone.plus.api.util.ItemTarget;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

public class GetCommand extends PlusCommand {

    public GetCommand() {
        super(new String[]{"get", "collect"}, "Get an item/resource"/*, new Arg(ItemList.class, "items")*/);
    }

    private static void OnResourceDoesNotExist(BaritonePlus mod, String resource) {
        mod.log("\"" + resource + "\" is not a catalogued resource. Can't get it yet, sorry! If it's a generic block try using baritone.", MessagePriority.OPTIONAL);
        mod.log("Use @list to get a list of available resources.", MessagePriority.OPTIONAL);
    }

    private void GetItems(BaritonePlus mod, ItemTarget... items) {
        Task targetTask;
        if (items == null || items.length == 0) {
            mod.log("You must specify at least one item!");
//            finish();
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
//            finish();
        }
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        ItemList items = args.getDatatypeFor(ForItemOptionalMeta.INSTANCE);
        GetItems(mod, items.items);
    }
}