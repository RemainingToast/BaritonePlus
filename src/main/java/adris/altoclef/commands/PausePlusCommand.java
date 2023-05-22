package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandInvalidStateException;
import baritone.api.command.exception.CommandTooManyArgumentsException;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;

public class PausePlusCommand extends PlusCommand {

    private final boolean[] paused = new boolean[]{false};

    public PausePlusCommand() {
        super(new String[]{"pause+", "p+"}, "Pause task runner (pauses all automation)");
    }

    @Override
    protected void call(AltoClef mod, String label, IArgConsumer args) throws CommandTooManyArgumentsException, CommandInvalidStateException {
        args.requireMax(0);
        if (paused[0]) {
            paused[0] = false;
            this.baritone.getBuilderProcess().resume();
            mod.getUserTaskChain().unpause();
            this.logDirect("Plus Resumed");
        } else {
            paused[0] = true;
//            mod.getClientBaritone().getPathingBehavior().requestPause();
            mod.getUserTaskChain().pause();
            this.logDirect("Plus Paused");
        }

        this.baritone.getPathingControlManager().registerProcess(new IBaritoneProcess() {
            public boolean isActive() {
                return paused[0];
            }

            public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
                baritone.getInputOverrideHandler().clearAllKeys();
                return new PathingCommand((Goal)null, PathingCommandType.REQUEST_PAUSE);
            }

            public boolean isTemporary() {
                return true;
            }

            public void onLostControl() {
            }

            public double priority() {
                return 0.0;
            }

            public String displayName0() {
                return "Plus Pause/Resume Commands";
            }
        });

        finish();
    }
}
