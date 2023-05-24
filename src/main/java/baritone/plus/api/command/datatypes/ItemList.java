package baritone.plus.api.command.datatypes;

import baritone.plus.main.TaskCatalogue;
import baritone.plus.api.util.ItemTarget;
import baritone.api.command.exception.CommandException;

import java.util.HashMap;

public class ItemList {
    public ItemTarget[] items;

    public ItemList(ItemTarget[] items) {
        this.items = items;
    }

    // TODO - Quantities don't work?
    public static ItemList parseRemainder(String line) throws CommandException {
        line = line.trim();
        if (line.contains(":")) {
            var split = line.split(":");
            if (split.length > 1) {
                line = split[1];
            }
        }
        if (line.contains(",")/*line.startsWith("[") && line.endsWith("]")*/) {
            line = line.substring(1, line.length() - 1);
            String[] parts = line.split(",");
            HashMap<String, Integer> items = new HashMap<>();
            for (String part : parts) {
                part = part.trim();
                String[] itemQuantityPair = part.split(" ");
                if (itemQuantityPair.length > 2 || itemQuantityPair.length <= 0) {
                    // Must be either "item count" or "item"
                    throw new CommandException("Resource array element must be either \"item count\" or \"item\", but \"" + part + "\"" + " has " + itemQuantityPair.length + " parts.") {};
                }
                String item = itemQuantityPair[0];
                int count = 1;
                if (itemQuantityPair.length > 1) {
                    try {
                        count = Integer.parseInt(itemQuantityPair[1]);
                    } catch (Exception iex) {
                        throw new CommandException("Failed to parse count for array element \"" + part + "\".") {};
                    }
                }
                if (TaskCatalogue.taskExists(item)) {
                    items.put(item, items.getOrDefault(item, 0) + count);
                } else {
                    throw new CommandException("Item not catalogued: " + item) {};
                }
            }
            if (items.size() != 0) {
                return new ItemList(items.entrySet().stream().map(entry -> new ItemTarget(entry.getKey(), entry.getValue())).toArray(ItemTarget[]::new));
            }
        } else {
            // We must be of type "item <?count>"
            String[] items = line.split(" ");
            if (items.length >= 1) {
                String name = items[0];
                if (!TaskCatalogue.taskExists(name)) {
                    throw new CommandException("Item not catalogued: " + name) {};
                }
                int count = 1;
                if (items.length == 2) {
                    try {
                        count = Integer.parseInt(items[1]);
                    } catch (NumberFormatException e) {
                        throw new CommandException("Failed to parse the following argument into type " + Integer.class + ": " + items[1] + ".") {};
                    }
                } else if (items.length > 2) {
                    throw new CommandException("Invalid item argument structure: Must be of form `<item>` or `<item> <count>`") {};
                }
                return new ItemList(new ItemTarget[]{new ItemTarget(name, count)});
            }
        }
        return new ItemList(new ItemTarget[0]);
    }
}
