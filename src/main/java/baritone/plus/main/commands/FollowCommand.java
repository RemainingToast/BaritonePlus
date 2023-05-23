package baritone.plus.main.commands;

import baritone.plus.main.BaritonePlus;
import baritone.api.command.argument.IArgConsumer;
import baritone.plus.api.command.PlusCommand;

public class FollowCommand extends PlusCommand {
    public FollowCommand() {
        super(new String[]{"follow+", "f+"}, "Follows you or someone else"/*, new Arg(String.class, "username", null, 0)*/);
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) {
//        String username = parser.get(String.class);
//        if (username == null) {
//            if (mod.getButler().hasCurrentUser()) {
//                username = mod.getButler().getCurrentUser();
//            } else {
//                mod.logWarning("No butler user currently present. Running this command with no user argument can ONLY be done via butler.");
//                finish();
//                return;
//            }
//        }
//        mod.runUserTask(new FollowPlayerTask(username));
    }
}