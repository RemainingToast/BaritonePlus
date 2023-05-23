package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.ui.MessagePriority;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

import java.util.Arrays;

public class ListCommand extends PlusCommand {
    public ListCommand() {
        super(new String[]{"list", "obtainable"}, "List all obtainable items");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.log("#### LIST OF ALL OBTAINABLE ITEMS ####", MessagePriority.OPTIONAL);
        mod.log(Arrays.toString(TaskCatalogue.resourceNames().toArray()), MessagePriority.OPTIONAL);
        mod.log("############# END LIST ###############", MessagePriority.OPTIONAL);
    }
}
