package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Playground;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;
import baritone.plus.api.command.PlusCommand;

import java.util.Comparator;
import java.util.stream.Stream;

public class TestCommand extends PlusCommand {

    public TestCommand() {
        super(new String[]{"test", "playground"}, "Generic command for testing"/*, new Arg(String.class, "extra", "", 0)*/);
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandNotEnoughArgumentsException {
        Playground.TEMP_TEST_FUNCTION(mod, args.peekString());
        finish();
    }

    @Override
    public Stream<String> tabComplete(String s, IArgConsumer args) {
        return Stream.of(
                "sign", "sign2", "pickup",
                "chunk", "structure", "place",
                "deadmeme", "stacked", "stacked2",
                "ravage", "temples", "outer",
                "smelt", "repair", "iron",
                "avoid", "portal", "kill",
                "craft", "food", "temple",
                "blaze", "flint", "unobtainable",
                "piglin", "stronghold", "terminate",
                "replace", "bed", "dragon",
                "dragon-pearl", "dragon-old",
                "chest", "173", "example",
                "netherite", "whisper"
        ).sorted(Comparator.naturalOrder());
    }
}