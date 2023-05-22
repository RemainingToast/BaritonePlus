package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.ui.MessagePriority;
import baritone.api.command.argument.IArgConsumer;

import java.util.Arrays;

public class ListCommand extends PlusCommand {
    public ListCommand() {
        super(new String[]{"list", "obtainable"}, "List all obtainable items");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.log("#### LIST OF ALL OBTAINABLE ITEMS ####", MessagePriority.OPTIONAL);
        mod.log(Arrays.toString(TaskCatalogue.resourceNames().toArray()), MessagePriority.OPTIONAL);
        mod.log("############# END LIST ###############", MessagePriority.OPTIONAL);
    }
}
