package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.*;
import adris.altoclef.tasks.construction.SchematicBuildTask;

public class BuildCommand extends Command {
    public BuildCommand() throws CommandException {
        super("build", "Build a structure from schematic data",
                new Arg(String.class, "filename", "", 0)
//                new Arg(GotoTarget.class, "x y z", "", 1)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String name = "";
        try {
            name = parser.get(String.class);
        } catch (CommandException e) {
            Debug.logError("Cannot parse parameter. Input format: '@build house.schem'");
        }

//        GotoTarget target = parser.get(GotoTarget.class);
//
//        if (target.getType() == GotoTarget.GotoTargetCoordType.XYZ) {
//            mod.runUserTask(new SchematicBuildTask(name, new BlockPos(
//                    target.getX(),
//                    target.getY(),
//                    target.getZ()
//            )));
//        } else {
//
//        }

        mod.runUserTask(new SchematicBuildTask(name));
    }
}
