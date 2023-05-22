package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.KillPlayerTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.NearbyPlayer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

public class PunkCommand extends PlusCommand {
    public PunkCommand() {
        super(new String[]{"punk"}, "Punk 'em"/*, new Arg(String.class, "playerName")*/);
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        String playerName = args.getDatatypeFor(NearbyPlayer.INSTANCE).getGameProfile().getName();
        mod.runUserTask(new KillPlayerTask(playerName));
    }
}