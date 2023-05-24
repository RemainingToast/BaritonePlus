package baritone.plus.main.commands;

import baritone.Baritone;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.RelativeBlockPos;
import baritone.api.command.datatypes.RelativeFile;
import baritone.api.command.exception.CommandException;
import baritone.api.utils.BetterBlockPos;
import baritone.plus.api.command.PlusCommand;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.tasks.construction.SchematicBuildTask;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.stream.Stream;

public class BuildPlusCommand extends PlusCommand {

    private static final File schematicsDir = new File(mc.runDirectory, "schematics");

    public BuildPlusCommand()  {
        super(new String[]{"build+"}, "Build a structure from schematic data");
    }

    @Override
    protected void call(BaritonePlus mod, String label, IArgConsumer args) throws CommandException {
        File file = args.getDatatypePost(RelativeFile.INSTANCE, schematicsDir).getAbsoluteFile();

        if (FilenameUtils.getExtension(file.getAbsolutePath()).isEmpty()) {
            String path = file.getAbsolutePath();
            file = new File(path + "." + Baritone.settings().schematicFallbackExtension.value);
        }

        BetterBlockPos origin = ctx.playerFeet();
        BetterBlockPos buildOrigin;

        if (args.hasAny()) {
            args.requireMax(3);
            buildOrigin = args.getDatatypePost(RelativeBlockPos.INSTANCE, origin);
        } else {
            args.requireMax(0);
            buildOrigin = origin;
        }

        /*boolean success = this.baritone.getBuilderProcess().build(file.getName(), file, buildOrigin);
        if (!success) {
            throw new CommandInvalidStateException("Couldn't load the schematic. Make sure to use the FULL file name, including the extension (e.g. blah.schematic).");
        } else {
            this.logDirect(String.format("Successfully loaded schematic for building\nOrigin: %s", buildOrigin));
        }*/

        mod.runUserTask(new SchematicBuildTask(file.getName(), buildOrigin));
    }

    @Override
    public Stream<String> tabComplete(String s, IArgConsumer args) throws CommandException {
        if (args.hasExactlyOne()) {
            return RelativeFile.tabComplete(args, schematicsDir);
        } else if (args.has(2)) {
            args.get();
            return args.tabCompleteDatatype(RelativeBlockPos.INSTANCE);
        } else {
            return Stream.empty();
        }
    }
}
