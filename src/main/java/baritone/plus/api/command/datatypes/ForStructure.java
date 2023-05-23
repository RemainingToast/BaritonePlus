package baritone.plus.api.command.datatypes;

import baritone.api.command.datatypes.IDatatypeContext;
import baritone.api.command.datatypes.IDatatypeFor;
import baritone.api.command.exception.CommandException;
import baritone.api.command.helpers.TabCompleteHelper;

import java.util.Locale;
import java.util.stream.Stream;

public enum ForStructure implements IDatatypeFor<Structure> {
    INSTANCE;

    private ForStructure() {
    }

    public Structure get(IDatatypeContext ctx) throws CommandException {
        return Structure.valueOf(ctx.getConsumer().getString().toUpperCase(Locale.US));
    }

    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        return (new TabCompleteHelper()).append(Stream.of(Structure.values()).map(Structure::name).map(String::toLowerCase)).filterPrefix(ctx.getConsumer().getString()).stream();
    }


}

