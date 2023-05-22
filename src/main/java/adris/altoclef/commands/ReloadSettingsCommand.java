package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.ConfigHelper;
import baritone.api.command.argument.IArgConsumer;

public class ReloadSettingsCommand extends PlusCommand {
    public ReloadSettingsCommand() {
        super(new String[]{"reload+"}, "Reloads plus settings and butler whitelist/blacklist.");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) {
        ConfigHelper.reloadAllConfigs();
        mod.log("Reload successful!");
//        finish();
    }
}