package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commands.datatypes.ItemById;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.helpers.ItemHelper;
import baritone.api.command.argument.IArgConsumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class InventoryCommand extends PlusCommand {
    public InventoryCommand() {
        super(new String[]{"inventory", "inv"}, "Prints the bot's inventory OR returns how many of an item the bot has"/*, new Arg(String.class, "item", null, 1)*/);
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        Item item = args.getDatatypeForOrNull(ItemById.INSTANCE);
        if (item == null) {
            // Print inventory
            // Get item counts
            HashMap<String, Integer> counts = new HashMap<>();
            for (int i = 0; i < mod.getPlayer().getInventory().size(); ++i) {
                ItemStack stack = mod.getPlayer().getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    String name = ItemHelper.stripItemName(stack.getItem());
                    if (!counts.containsKey(name)) counts.put(name, 0);
                    counts.put(name, counts.get(name) + stack.getCount());
                }
            }
            // Print
            mod.log("INVENTORY: ", MessagePriority.OPTIONAL);
            for (String name : counts.keySet()) {
                mod.log(name + " : " + counts.get(name), MessagePriority.OPTIONAL);
            }
            mod.log("(inventory list sent) ", MessagePriority.OPTIONAL);
        } else {
            // Print item quantity
            Item[] matches = TaskCatalogue.getItemMatches(item.toString());
            if (matches == null || matches.length == 0) {
                mod.logWarning("Item \"" + item + "\" is not catalogued/recognized.");
//                finish();
                return;
            }
            int count = mod.getItemStorage().getItemCount(matches);
            if (count == 0) {
                mod.log(item + " COUNT: (none)");
            } else {
                mod.log(item + " COUNT: " + count);
            }
        }
//        finish();
    }
}