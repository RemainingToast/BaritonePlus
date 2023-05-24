package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.util.helpers.ConfigHelper;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class CustomCommand extends PlusCommand {
    private static CustomTaskConfig _ctc;

    static {
        ConfigHelper.loadConfig("configs/CustomTasks.json", CustomTaskConfig::new, CustomTaskConfig.class, newConfig -> _ctc = newConfig);
    }


    public CustomCommand() {
        super(new String[]{_ctc.prefix}, "does a custom action"/*, new Arg(String.class, "task name")*/);
    }

    public static CustomTaskConfig getConfig() {
        return _ctc;
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
//        TODO
//        CustomTaskConfig dupliate = _ctc;
//
//        String customCommand = parser.get(String.class);
//
//        StringBuilder commandToExecute = new StringBuilder();
//        int commandIndex = -1;
//        for (int i = 0; i < _ctc.customTasks.length; i++) {
//            if (_ctc.customTasks[i].name.equalsIgnoreCase(customCommand)) {
//                commandIndex = i;
//                break;
//            }
//        }
//        if (commandIndex > -1) {
//            for (int i = 0; i < _ctc.customTasks[commandIndex].tasks.length; i++) {
//                if (i > 0) {
//                    commandToExecute.append(";");
//                }
//                commandToExecute.append(_ctc.customTasks[commandIndex].tasks[i].command).append(" ");
//                if (_ctc.customTasks[commandIndex].tasks[i].command.equals("get") || _ctc.customTasks[commandIndex].tasks[i].command.equals("equip")) {
//                    //parameters have two inside arrays so we need to be careful here
//
//                    commandToExecute.append("[");
//                    for (int j = 0; j < _ctc.customTasks[commandIndex].tasks[i].parameters.length; j++) {
//                        commandToExecute.append(Arrays.toString(_ctc.customTasks[commandIndex].tasks[i].parameters[j]).replaceAll("\\[", "").replaceAll("]", "").replaceAll(",", ""));
//                        if (j < _ctc.customTasks[commandIndex].tasks[i].parameters.length - 1) {
//                            commandToExecute.append("?");
//
//                        }
//                    }
//                    commandToExecute.append("]");
//                } else {
//                    commandToExecute.append(Arrays.toString(_ctc.customTasks[commandIndex].tasks[i].parameters[0]).replaceAll("\\[", "").replaceAll("]", ""));
//                }
//            }
//            AltoClef.getCommandExecutor().execute(mod.getModSettings().getCommandPrefix() + commandToExecute.toString().replaceAll(",", "").replaceAll("\\?", ","));
//        } else {
//
//        }
    }
}
