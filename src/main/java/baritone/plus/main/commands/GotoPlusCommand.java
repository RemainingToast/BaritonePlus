package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.api.command.datatypes.GotoTargetDataType;
import baritone.plus.api.command.datatypes.GotoTarget;
import baritone.plus.main.tasks.movement.DefaultGoToDimensionTask;
import baritone.plus.main.tasks.movement.GetToBlockTask;
import baritone.plus.main.tasks.movement.GetToXZTask;
import baritone.plus.main.tasks.movement.GetToYTask;
import baritone.plus.api.tasks.Task;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;
import baritone.api.command.exception.CommandTooManyArgumentsException;
import net.minecraft.util.math.BlockPos;

/**
 * Out of all the commands, this one probably demonstrates
 * why we need a better arg parsing system. Please.
 */
public class GotoPlusCommand extends PlusCommand {

    public GotoPlusCommand() {
        // x z
        // x y z
        // x y z dimension
        // (dimension)
        // (x z dimension)
        super(new String[]{"goto+", "go+"}, "Tell bot to travel to a set of coordinates."/*, new Arg(GotoTarget.class, "[x y z dimension]/[x z dimension]/[y dimension]/[dimension]/[x y z]/[x z]/[y]")*/);
    }

    public static Task getMovementTaskFor(GotoTarget target) {
        return switch (target.getType()) {
            case XYZ ->
                    new GetToBlockTask(new BlockPos(target.getX(), target.getY(), target.getZ()), target.getDimension());
            case XZ -> new GetToXZTask(target.getX(), target.getZ(), target.getDimension());
            case Y -> new GetToYTask(target.getY(), target.getDimension());
            case NONE -> new DefaultGoToDimensionTask(target.getDimension());
        };
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandTooManyArgumentsException, CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        args.requireMax(4);
        GotoTarget gotoTarget = args.getDatatypeFor(GotoTargetDataType.INSTANCE);
        mod.runUserTask(getMovementTaskFor(gotoTarget));
    }
}
