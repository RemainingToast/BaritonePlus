package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.datatypes.ForItemOptionalMeta;
import adris.altoclef.commands.datatypes.ItemList;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.util.ItemTarget;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class EquipCommand extends PlusCommand {
    public EquipCommand() {
        super(new String[]{"equip"}, "Equips armor"/*, new Arg(ItemList.class, "[armors]")*/);
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) throws CommandNotEnoughArgumentsException, CommandInvalidTypeException {
        ItemTarget[] items;
        if (args.hasExactlyOne()) {
            switch (args.peekString().toLowerCase()) {
                case "leather" -> items = new ItemTarget[]{
                        new ItemTarget(Items.LEATHER_HELMET),
                        new ItemTarget(Items.LEATHER_CHESTPLATE),
                        new ItemTarget(Items.LEATHER_LEGGINGS),
                        new ItemTarget(Items.LEATHER_BOOTS)
                };
                case "iron" -> items = new ItemTarget[]{
                        new ItemTarget(Items.IRON_HELMET),
                        new ItemTarget(Items.IRON_CHESTPLATE),
                        new ItemTarget(Items.IRON_LEGGINGS),
                        new ItemTarget(Items.IRON_BOOTS)
                };
                case "gold" -> items = new ItemTarget[]{
                        new ItemTarget(Items.GOLDEN_HELMET),
                        new ItemTarget(Items.GOLDEN_CHESTPLATE),
                        new ItemTarget(Items.GOLDEN_LEGGINGS),
                        new ItemTarget(Items.GOLDEN_BOOTS)
                };
                case "diamond" -> items = new ItemTarget[]{
                        new ItemTarget(Items.DIAMOND_HELMET),
                        new ItemTarget(Items.DIAMOND_CHESTPLATE),
                        new ItemTarget(Items.DIAMOND_LEGGINGS),
                        new ItemTarget(Items.DIAMOND_BOOTS)
                };
                case "netherite" -> items = new ItemTarget[]{
                        new ItemTarget(Items.NETHERITE_HELMET),
                        new ItemTarget(Items.NETHERITE_CHESTPLATE),
                        new ItemTarget(Items.NETHERITE_LEGGINGS),
                        new ItemTarget(Items.NETHERITE_BOOTS)
                };
                default -> {
                    ItemList itemList = args.getDatatypeFor(ForItemOptionalMeta.INSTANCE);
                    items = itemList != null ? itemList.items : null;
                }
            }
        } else {
            ItemList itemList = args.getDatatypeFor(ForItemOptionalMeta.INSTANCE);
            items = itemList != null ? itemList.items : null;
        }

        if (items != null) {
            for (ItemTarget item : items) {
                for (Item i : item.getMatches()) {
                    if (!(i instanceof ArmorItem)) {
                        items = null; // flag items as "bad" if any of the items are not ArmorItems
                        break;
                    }
                }
                if (items == null) {
                    break;
                }
            }

            if (items != null) {
                mod.runUserTask(new EquipArmorTask(items)); // do not run the equip task with non-armor items.
            } else {
                throw new CommandInvalidTypeException(args.get(), "You must provide armor items."); //inform the user that they can only use armor items.
            }
        } else {
            throw new CommandInvalidTypeException(args.get(), "Invalid input. Please provide a valid armor set or a list of armor items.");
        }
        // TODO Possibly add in a variable to tell the user what was wrong. However, this is less helpful if a list of items is wrong.
    }
}
