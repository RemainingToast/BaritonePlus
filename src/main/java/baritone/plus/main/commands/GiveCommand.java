package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.api.command.datatypes.ItemById;
import baritone.plus.api.command.datatypes.NumberDatatype;
import baritone.plus.main.tasks.entity.GiveItemToPlayerTask;
import baritone.plus.api.util.ItemTarget;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.NearbyPlayer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GiveCommand extends PlusCommand {
    public GiveCommand() {
        super(new String[]{"give"}, "Collects an item and gives it to you or someone else"/*,
                new Arg(String.class, "username", null, 2),
                new Arg(String.class, "item"),
                new Arg(Integer.class, "count", 1, 1)*/);
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        PlayerEntity player = args.getDatatypeFor(NearbyPlayer.INSTANCE);
        String username;
        if (player == null) {
            if (mod.getButler().hasCurrentUser()) {
                username = mod.getButler().getCurrentUser();
            } else {
                mod.logWarning("No butler user currently present. Running this command with no user argument can ONLY be done via butler.");
                return;
            }
        } else {
            username = player.getGameProfile().getName();
        }
        Item item = args.getDatatypeFor(ItemById.INSTANCE);
        int count = args.getDatatypeFor(NumberDatatype.INSTANCE).intValue();
        ItemTarget target = null;
        if (TaskCatalogue.taskExists(item)) {
            // Registered item with task.
            target = TaskCatalogue.getItemTarget(item.toString(), count);
        } else {
            // Unregistered item, might still be in inventory though.
            for (int i = 0; i < mod.getPlayer().getInventory().size(); ++i) {
                ItemStack stack = mod.getPlayer().getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    target = new ItemTarget(stack.getItem(), count);
                }
            }
        }
        if (target != null) {
            Debug.logMessage("USER: " + username + " : ITEM: " + item + " x " + count);
            mod.runUserTask(new GiveItemToPlayerTask(username, target));
        } else {
            mod.log("Item not found or task does not exist for item: " + item);
//            finish();
        }
    }

}