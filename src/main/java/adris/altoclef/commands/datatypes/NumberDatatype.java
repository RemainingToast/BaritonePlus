package adris.altoclef.commands.datatypes;

import baritone.api.command.datatypes.IDatatypeContext;
import baritone.api.command.datatypes.IDatatypeFor;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidTypeException;

import java.util.Random;
import java.util.stream.Stream;

public enum NumberDatatype implements IDatatypeFor<Number> {
    INSTANCE;

    private NumberDatatype() {
    }

    public Number get(IDatatypeContext ctx) throws CommandException {
        String numberString = ctx.getConsumer().getString();
        try {
            if (numberString.contains(".")) {
                return Double.parseDouble(numberString);
            } else {
                return Long.parseLong(numberString);
            }
        } catch (NumberFormatException e) {
            throw new CommandInvalidTypeException(ctx.getConsumer().get(), "Invalid number format: " + numberString);
        }
    }

    public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
        return ctx.getConsumer().hasAtMostOne() ? Stream.of(String.valueOf(new Random().nextInt(10, 64))) : Stream.empty();
    }
}