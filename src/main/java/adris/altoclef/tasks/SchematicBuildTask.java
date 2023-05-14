package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.CubeBounds;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.schematic.ISchematic;
import baritone.process.BuilderProcess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicBuildTask extends Task {
    private boolean finished;
    private BuilderProcess builder;
    private final String schematicFileName;
    private final BlockPos startPos;
    private final int allowedResourceStackCount;
    private Vec3i schemSize;
    private Map<BlockState, Integer> missing;
    private boolean sourced;
    private boolean addedAvoidance;
    private String name;
    private ISchematic schematic;
    private static final int FOOD_UNITS = 80;
    private static final int MIN_FOOD_UNITS = 10;
    private final TimerGame _clickTimer = new TimerGame(120);
    private final MovementProgressChecker _moveChecker = new MovementProgressChecker(4, 0.1, 4, 0.01);
    private Task walkAroundTask;

    public SchematicBuildTask(final String schematicFileName) {
        this(schematicFileName, MinecraftClient.getInstance().player.getBlockPos());
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos) {
        this(schematicFileName, startPos, 8);
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos, final int allowedResourceStackCount) {
        this.sourced = false;
        this.addedAvoidance = false;
        this.schematicFileName = schematicFileName;
        this.startPos = startPos;
        this.allowedResourceStackCount = allowedResourceStackCount;
    }

    @Override
    protected void onStart(AltoClef mod) {
        this.finished = false;

        mod.getClientBaritoneSettings().buildInLayers.value = true;
        mod.getClientBaritoneSettings().buildIgnoreExisting.value = true;

        mod.getClientBaritoneSettings().buildSubstitutes.value.put(
                Blocks.GRASS_BLOCK,
                List.of(Blocks.DIRT)
        );

        for (Block block : ItemHelper.itemsToBlocks(ItemHelper.BED)) {
            mod.getClientBaritoneSettings().buildSubstitutes.value.put(
                    block,
                    List.of(ItemHelper.itemsToBlocks(ItemHelper.BED))
            );
        }

        if (isNull(builder)) {
            builder = mod.getClientBaritone().getBuilderProcess();
        }

        var file = new File(new File(MinecraftClient.getInstance().runDirectory, "schematics"), schematicFileName);

        if (!file.exists()) {
            Debug.logMessage("Could not locate schematic file. Terminating...");
            this.finished = true;
            return;
        }

        builder.clearState();

        if (this.schematic == null) {
            builder.build(this.schematicFileName, file, startPos, true); //TODO: I think there should be a state queue in baritone
        } else {
            builder.build(this.name, this.schematic, startPos, true);
        }

        if (isNull(schemSize)) {
            this.schemSize = builder.getSchemSize();
        }

        overrideMissing();

        if (!isNull(schemSize) && builder.isFromAltoclef() && !this.addedAvoidance) {
            CubeBounds bounds = new CubeBounds(mod.getPlayer().getBlockPos(), this.schemSize.getX(), this.schemSize.getY(), this.schemSize.getZ());
            this.addedAvoidance = true;
            mod.getBehaviour().avoidBlockBreaking(bounds.getPredicate());
        }

        _moveChecker.reset();
        _clickTimer.reset();

        setDebugState("Building %s".formatted(this.schematicFileName));
    }

    @Override
    protected Task onTick(AltoClef mod) {
        overrideMissing();

        this.sourced = getTodoList(mod, getMissing()).isEmpty();

        if (!isNull(getMissing()) && !getMissing().isEmpty() && (builder.isPaused() || !builder.isFromAltoclef()) || !builder.isActive()) {
            setDebugState("Getting Schematic Materials...");
//            if (!mod.inAvoidance(this.bounds)) {
//                mod.setAvoidanceOf(this.bounds);
//            }
            
            if (StorageHelper.calculateInventoryFoodScore(mod) < MIN_FOOD_UNITS) {
                return new CollectFoodTask(FOOD_UNITS);
            }

            for (final Map.Entry<BlockState, Integer> entry : getTodoList(mod, getMissing()).entrySet()) {
                var _newTarget = new ItemTarget(
                        entry.getKey().getBlock().asItem(),
                        entry.getValue()
                );
//                Debug.logMessage("Added Target: %s %s", _newTarget.toString(), _newTarget.getTargetCount());
                return TaskCatalogue.getItemTask(_newTarget);
            }
        }

//        mod.unsetAvoidanceOf(this.bounds);

        if (this.sourced && !builder.isActive()) {
//            if (mod.inAvoidance(this.bounds)) {
//                mod.unsetAvoidanceOf(this.bounds);
//            }
            Debug.logInternal("Resuming build process...");
            setDebugState("Resuming build process...");
            builder.resume();
        }

        if (_moveChecker.check(mod)) {
            _clickTimer.reset();
        }

        if (_clickTimer.elapsed()) {
            if (isNull(walkAroundTask)) {
                walkAroundTask = new RandomRadiusGoalTask(mod.getPlayer().getBlockPos(), 5d).next(mod.getPlayer().getBlockPos());
            }
            Debug.logMessage("Timer elapsed.");
        }

        if (!isNull(walkAroundTask)) {
            if (!walkAroundTask.isFinished(mod)) {
                return walkAroundTask;
            } else {
                walkAroundTask = null;
                builder.popStack();
                _clickTimer.reset();
                _moveChecker.reset();
            }
        }

        /*
        if (BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().getApproxPlaceable() != null) {
            if (BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().getApproxPlaceable().stream().anyMatch(e ->  e != null && e.getBlock() instanceof BedBlock)) {
                System.out.println("ALT: true");
            }
            BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().getApproxPlaceable().forEach(e -> {
                if (Utils.isSet(e) && e.getBlock().asItem().toString() != "air") {
                    System.out.println(e.getBlock().getName());
                    System.out.println(e.getBlock().asItem().getName());
                    System.out.println(e.getBlock().asItem().toString());
                    System.out.println(e.getBlock().toString());
                    System.out.println("(((((((((");
                    System.out.println(e.getBlock().getDefaultState());
                    System.out.println(")))))))))");
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                }
            });
        }*/
        //mod.getItemStorage().getItemStackInSlot(mod.getItemStorage().getInventorySlotsWithItem(Items.OAK_DOOR).get(0)).getItem().
        /*
        missing.forEach((k,e) -> {
            if (Utils.isSet(k)) {
                System.out.println(k);
            }
        });*/

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        builder = null;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SchematicBuildTask;
    }

    @Override
    protected String toDebugString() {
        return "SchematicBuilderTask";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (!isNull(builder) && builder.isFromAltoclefFinished() || this.finished) {
            return true;
        }
        return false;
    }

    private Map<BlockState, Integer> getTodoList(final AltoClef mod, final Map<BlockState, Integer> missing) {
        final ItemStorageTracker inventory = mod.getItemStorage();
        final Map<BlockState, Integer> todo = new HashMap<>();

        for (final Map.Entry<BlockState, Integer> entry : missing.entrySet()) {
            final Item item = entry.getKey().getBlock().asItem();
            final int currentCount = inventory.getItemCount(item);
            final int neededCount = entry.getValue();
            
            if (currentCount >= neededCount) continue;
            
            todo.put(entry.getKey(), neededCount - currentCount);
        }

        return todo;
    }

    private boolean isNull(Object o) {
        return o == null;
    }

    private void overrideMissing() {
        this.missing = builder.getMissing();
    }

    private Map<BlockState, Integer> getMissing() {
        if (isNull(this.missing)) {
            overrideMissing();
        }
        return this.missing;
    }
}
