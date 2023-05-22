package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commands.datatypes.ForStructure;
import adris.altoclef.commands.datatypes.Structure;
import adris.altoclef.tasks.movement.GoToStrongholdPortalTask;
import adris.altoclef.tasks.movement.LocateDesertTempleTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

import java.util.stream.Stream;

public class LocateStructureCommand extends PlusCommand {

    public LocateStructureCommand() {
        super(new String[]{"structure"}, "Locate a world generated structure."/*, new Arg(Structure.class, "structure")*/);
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        Structure structure = args.getDatatypeFor(ForStructure.INSTANCE);
        switch (structure) {
            case STRONGHOLD -> mod.runUserTask(new GoToStrongholdPortalTask(1));
            case DESERT_TEMPLE -> mod.runUserTask(new LocateDesertTempleTask());
        }
    }

    @Override
    public Stream<String> tabComplete(String s, IArgConsumer args) {
        return args.tabCompleteDatatype(ForStructure.INSTANCE);
    }
}