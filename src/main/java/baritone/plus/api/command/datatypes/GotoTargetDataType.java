package baritone.plus.api.command.datatypes;

import baritone.api.command.datatypes.IDatatypeContext;
import baritone.api.command.datatypes.IDatatypeFor;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

import java.util.stream.Stream;

public enum GotoTargetDataType implements IDatatypeFor<GotoTarget> {
    INSTANCE;

    private GotoTargetDataType() {
    }

    public GotoTarget get(IDatatypeContext ctx) throws CommandNotEnoughArgumentsException {
        try {
            return GotoTarget.parseRemainder(ctx.getConsumer().getString());
        } catch (CommandException e) {
            throw new CommandNotEnoughArgumentsException(1);
        }
    }

    public Stream<String> tabComplete(IDatatypeContext ctx) {
        return Stream.empty();
    }
}
