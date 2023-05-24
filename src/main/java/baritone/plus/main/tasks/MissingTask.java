package baritone.plus.main.tasks;

import baritone.plus.api.tasks.Task;
import baritone.plus.main.BaritonePlus;
import baritone.process.BuilderProcess;
import net.minecraft.block.BlockState;

import java.util.Map;
import java.util.stream.Collectors;

public class MissingTask extends Task {

    private boolean finished = false;
    BuilderProcess builder;

    @Override
    protected void onStart(BaritonePlus mod) {
        builder = mod.getClientBaritone().getBuilderProcess();
        builder.resume();
        builder.build("test8.schem", mod.getPlayer().getBlockPos());
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (builder.isActive()) {
            if (builder.isPaused()) {
                if (builder.getApproxPlaceable().isEmpty()) return null;
                Object o = builder.getApproxPlaceable().get(0);
                Map<BlockState, Integer> missing = (Map<BlockState, Integer>) o;
                System.out.println(missing.entrySet().stream()
                        .map(e -> String.format("%sx %s", e.getValue(), e.getKey()))
                        .collect(Collectors.joining("\n")));
            }
        } else {
            finished = true;
        }
        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {}

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return finished;
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return null;
    }
}
