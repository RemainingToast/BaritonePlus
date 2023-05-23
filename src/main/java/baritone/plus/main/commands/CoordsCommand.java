package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.util.helpers.WorldHelper;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class CoordsCommand extends PlusCommand {
    public CoordsCommand() {
        super(new String[]{"coords"}, "Get bot's current coordinates");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.log("CURRENT COORDINATES: " + mod.getPlayer().getBlockPos().toShortString() + " (Current dimension: " + WorldHelper.getCurrentDimension() + ")");
        finish();
    }
}
