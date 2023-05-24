package baritone.plus.api.command.datatypes;

import baritone.api.command.datatypes.IDatatypeContext;
import baritone.api.command.datatypes.IDatatypeFor;
import baritone.api.command.exception.CommandException;

import java.util.stream.Stream;

public enum ForItemOptionalMeta implements IDatatypeFor<ItemList> {
    INSTANCE;

    private ForItemOptionalMeta() {
    }

    public ItemList get(IDatatypeContext ctx) throws CommandException {
        return ItemList.parseRemainder(ctx.getConsumer().getString());
    }

    public Stream<String> tabComplete(IDatatypeContext ctx) {
        return ctx.getConsumer().tabCompleteDatatype(ItemById.INSTANCE);
    }
}
