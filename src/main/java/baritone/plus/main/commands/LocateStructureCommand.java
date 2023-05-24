package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.api.command.datatypes.ForStructure;
import baritone.plus.api.command.datatypes.Structure;
import baritone.plus.main.tasks.movement.GoToStrongholdPortalTask;
import baritone.plus.main.tasks.movement.LocateDesertTempleTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

import java.util.stream.Stream;

public class LocateStructureCommand extends PlusCommand {

    public LocateStructureCommand() {
        super(new String[]{"structure"}, "Locate a world generated structure."/*, new Arg(Structure.class, "structure")*/);
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
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