package baritone.plus.api.command.datatypes;

import baritone.api.command.datatypes.IDatatypeContext;
import baritone.api.command.datatypes.IDatatypeFor;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;

import java.util.stream.Stream;

public enum ForItemOptionalMeta implements IDatatypeFor<ItemList> {
    INSTANCE;

    private ForItemOptionalMeta() {
    }

    public ItemList get(IDatatypeContext ctx) throws CommandNotEnoughArgumentsException {
        try {
            return ItemList.parseRemainder(ctx.getConsumer().getString());
        } catch (CommandException e) {
            throw new CommandNotEnoughArgumentsException(1);
        }
    }

    public Stream<String> tabComplete(IDatatypeContext ctx) {
        return ctx.getConsumer().tabCompleteDatatype(ItemById.INSTANCE);
    }
}
