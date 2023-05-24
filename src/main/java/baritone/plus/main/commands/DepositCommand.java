package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.api.command.datatypes.ForItemOptionalMeta;
import baritone.plus.api.command.datatypes.ItemById;
import baritone.plus.api.command.datatypes.ItemList;
import baritone.plus.main.tasks.container.StoreInAnyContainerTask;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.slots.PlayerSlot;
import baritone.api.command.argument.IArgConsumer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import org.apache.commons.lang3.ArrayUtils;

import java.util.stream.Stream;

public class DepositCommand extends PlusCommand {
    public DepositCommand() {
        super(new String[]{"deposit"}, "Deposit ALL of our items"/*, new Arg(ItemList.class, "items (empty for ALL non gear items)", null, 0, false)*/);
    }

    public static ItemTarget[] getAllNonEquippedOrToolItemsAsTarget(BaritonePlus mod) {
        return StorageHelper.getAllInventoryItemsAsTargets(slot -> {
            // Ignore armor
            if (ArrayUtils.contains(PlayerSlot.ARMOR_SLOTS, slot))
                return false;
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            // Ignore tools
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                return !(item instanceof ToolItem);
            }
            return false;
        });
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        try {
            ItemList itemList = null;
            ItemTarget[] items;

            if (args.hasAny()) {
                itemList = args.getDatatypeFor(ForItemOptionalMeta.INSTANCE);
            }

            if (itemList == null) {
                items = getAllNonEquippedOrToolItemsAsTarget(mod);
            } else {
                items = itemList.items;
            }

            mod.runUserTask(new StoreInAnyContainerTask(false, items));
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    @Override
    public Stream<String> tabComplete(String s, IArgConsumer args) {
        return args.tabCompleteDatatype(ItemById.INSTANCE);
    }
}
