package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.plus.api.util.helpers.ConfigHelper;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class ReloadSettingsCommand extends PlusCommand {
    public ReloadSettingsCommand() {
        super(new String[]{"reload+"}, "Reloads plus settings and butler whitelist/blacklist.");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
        ConfigHelper.reloadAllConfigs();
        mod.log("Reload successful!");
//        finish();
    }
}