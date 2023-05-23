package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.tasks.Task;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

import java.util.List;

public class StatusCommand extends PlusCommand {
    public StatusCommand() {
        super(new String[]{"status"}, "Get status of currently executing command");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        List<Task> tasks = mod.getUserTaskChain().getTasks();
        if (tasks.size() == 0) {
            mod.log("No tasks currently running.");
        } else {
            mod.log("CURRENT TASK: " + tasks.get(0).toString());
        }
        finish();
    }
}