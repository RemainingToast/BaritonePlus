package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.command.argument.IArgConsumer;

public class CoordsCommand extends PlusCommand {
    public CoordsCommand() {
        super(new String[]{"coords"}, "Get bot's current coordinates");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.log("CURRENT COORDINATES: " + mod.getPlayer().getBlockPos().toShortString() + " (Current dimension: " + WorldHelper.getCurrentDimension() + ")");
        finish();
    }
}
