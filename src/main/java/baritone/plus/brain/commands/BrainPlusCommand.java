package baritone.plus.brain.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.brain.tasks.BrainTask;
import baritone.plus.api.command.PlusCommand;
import baritone.api.command.argument.IArgConsumer;

public class BrainPlusCommand extends PlusCommand {
    public BrainPlusCommand() {
        super(new String[]{"brain"}, "Activates ChatGPT Brain");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        mod.runUserTask(new BrainTask());
    }

}
