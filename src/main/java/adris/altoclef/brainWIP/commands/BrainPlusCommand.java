package adris.altoclef.brainWIP.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.brainWIP.tasks.BrainTask;
import adris.altoclef.commands.PlusCommand;
import baritone.api.command.argument.IArgConsumer;

public class BrainPlusCommand extends PlusCommand {
    public BrainPlusCommand() {
        super(new String[]{"brain"}, "Activates ChatGPT Brain");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        mod.runUserTask(new BrainTask());
    }

}
