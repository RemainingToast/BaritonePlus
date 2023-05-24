package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class StashCommand extends PlusCommand {
    public StashCommand() {
        // stash <stash_x> <stash_y> <stash_z> <stash_radius> [item list]
        super(new String[]{"stash"}, "Store an item in a chest/container stash. Will deposit ALL non-equipped items if item list is empty."/*,
                new Arg(Integer.class, "x_start"),
                new Arg(Integer.class, "y_start"),
                new Arg(Integer.class, "z_start"),
                new Arg(Integer.class, "x_end"),
                new Arg(Integer.class, "y_end"),
                new Arg(Integer.class, "z_end"),
                new Arg(ItemList.class, "items (empty for ALL)", null, 6, false)*/);
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
//        BlockPos start = new BlockPos(
//                parser.get(Integer.class),
//                parser.get(Integer.class),
//                parser.get(Integer.class)
//        );
//
//        BlockPos end = new BlockPos(
//                parser.get(Integer.class),
//                parser.get(Integer.class),
//                parser.get(Integer.class)
//        );
//
//        ItemList itemList = parser.get(ItemList.class);
//        ItemTarget[] items;
//        if (itemList == null) {
//            items = DepositPlusCommand.getAllNonEquippedOrToolItemsAsTarget(mod);
//        } else {
//            items = itemList.items;
//        }
//
//
//        mod.runUserTask(new StoreInStashTask(true, new BlockRange(start, end, WorldHelper.getCurrentDimension()), items));
    }
}
