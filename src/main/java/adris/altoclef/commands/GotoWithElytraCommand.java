package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.GetToXZWithElytraTask;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.RelativeGoalXZ;
import baritone.api.command.exception.CommandInvalidTypeException;
import baritone.api.command.exception.CommandNotEnoughArgumentsException;
import baritone.api.pathing.goals.GoalXZ;

public class GotoWithElytraCommand extends PlusCommand {
    public GotoWithElytraCommand() {
        super(new String[]{"elytra", "e+"}, "Tell bot to travel to a set of coordinates using Elytra"/*,
                new Arg(Integer.class, "x"),
                new Arg(Integer.class, "z")*/
        );
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) throws CommandInvalidTypeException, CommandNotEnoughArgumentsException {
        GoalXZ goal = args.getDatatypePost(RelativeGoalXZ.INSTANCE, this.baritone.getPlayerContext().playerFeet());
        mod.runUserTask(new GetToXZWithElytraTask(goal.getX(), goal.getZ()));
    }
}