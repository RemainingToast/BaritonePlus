package baritone.plus.main.tasks.resources;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.ResourceTask;
import baritone.plus.main.tasks.movement.DefaultGoToDimensionTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.Dimension;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.WorldHelper;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.item.Items;

public class CollectMagmaCreamTask extends ResourceTask {
    private final int _count;

    public CollectMagmaCreamTask(int count) {
        super(Items.MAGMA_CREAM, count);
        _count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(BaritonePlus mod) {
        return false;
    }

    @Override
    protected void onResourceStart(BaritonePlus mod) {

    }

    @Override
    protected Task onResourceTick(BaritonePlus mod) {
        /*
         * If in nether:
         *      If magma cube located, hunt magma cube.
         *      If not enough potential blaze powder, hunt blazes.
         * If in overworld:
         *      If not enough potential slimes, hunt slimes
         *      Otherwise, go to nether.
         * If in end:
         *      Go to overworld lol
         */
        int currentCream = mod.getItemStorage().getItemCount(Items.MAGMA_CREAM);
        int neededCream = _count - currentCream;
        switch (WorldHelper.getCurrentDimension()) {
            case NETHER -> {
                if (mod.getEntityTracker().entityFound(MagmaCubeEntity.class)) {
                    setDebugState("Killing Magma cube");
                    return new KillAndLootTask(MagmaCubeEntity.class, new ItemTarget(Items.MAGMA_CREAM));
                }
                int currentBlazePowderPotential = mod.getItemStorage().getItemCount(Items.BLAZE_POWDER) + mod.getItemStorage().getItemCount(Items.BLAZE_ROD);
                if (neededCream > currentBlazePowderPotential) {
                    // Kill blazes as no magma cube was found.
                    setDebugState("Getting blaze powder");
                    return TaskCatalogue.getItemTask(Items.BLAZE_POWDER, neededCream - currentCream);
                }
                setDebugState("Going back to overworld to kill slimes, we have enough blaze powder and no nearby magma cubes.");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
            case OVERWORLD -> {
                int currentSlime = mod.getItemStorage().getItemCount(Items.SLIME_BALL);
                if (neededCream > currentSlime) {
                    setDebugState("Getting slime balls");
                    return TaskCatalogue.getItemTask(Items.SLIME_BALL, neededCream - currentCream);
                }
                setDebugState("Going to nether to get blaze powder and/or kill magma cubes");
                return new DefaultGoToDimensionTask(Dimension.NETHER);
            }
            case END -> {
                setDebugState("Going to overworld, no magma cream materials exist here.");
                return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
            }
        }

        setDebugState("INVALID DIMENSION??: " + WorldHelper.getCurrentDimension());
        return null;
    }

    @Override
    protected void onResourceStop(BaritonePlus mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectMagmaCreamTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " Magma cream.";
    }
}
