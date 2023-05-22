package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import baritone.api.command.Command;
import baritone.api.command.ICommand;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.argument.ICommandArgument;
import baritone.api.command.exception.CommandErrorMessageException;
import baritone.api.command.exception.CommandException;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public abstract class PlusCommand extends Command {

    private final String[] _names;
    private final String _description;
    private final AltoClef _mod;
    @Setter private Runnable _onFinish = null;

    public PlusCommand(String[] names, String description) {
        super(AltoClef.INSTANCE.getClientBaritone(), names);
        _mod = AltoClef.INSTANCE;
        _names = names;
        _description = description;
    }

    protected void finish() {
        if (_onFinish != null)
            _onFinish.run();
    }

    protected abstract void call(AltoClef mod, String label, IArgConsumer args) throws baritone.api.command.exception.CommandException;

    public String getName() {
        return _names[0];
    }

    @Override
    public Stream<String> tabComplete(String s, IArgConsumer args) {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return _description;
    }

    @Override
    public List<String> getLongDesc() {
        return Collections.singletonList(_description);
    }

    @Override
    public void execute(String s, IArgConsumer args) throws baritone.api.command.exception.CommandException {
        call(_mod, s, args);
    }

    public static Object parseEnum(String unit, Class type) throws CommandException {
        unit = unit.toLowerCase().trim();
        StringBuilder res = new StringBuilder();
        for (Object v : type.getEnumConstants()) {
            if (v.toString().toLowerCase().equals(unit)) {
                return v;
            }
            res.append(type);
            res.append("|");
        }
        res.delete(res.length() - 1, res.length()); // Remove the last "|"
        throw new CommandErrorMessageException("Invalid argument found: " + unit + ". Accepted values are: " + res) {
            @Override
            public void handle(ICommand command, List<ICommandArgument> args) {
                super.handle(command, args);
            }
        };
    }
}
