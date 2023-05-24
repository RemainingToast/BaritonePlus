package baritone.plus.main.tasks.construction;

import baritone.api.schematic.ISchematic;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.trackers.storage.ItemStorageTracker;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.api.util.helpers.StorageHelper;
import baritone.plus.api.util.progresscheck.MovementProgressChecker;
import baritone.plus.api.util.time.TimerGame;
import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.TaskCatalogue;
import baritone.plus.main.tasks.RandomRadiusGoalTask;
import baritone.plus.main.tasks.resources.CollectFoodTask;
import baritone.plus.main.util.CubeBounds;
import baritone.process.BuilderProcess;
import lombok.NonNull;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

// TODO
//  1. Check Difficulty for Gear
//  - Determine Gear to get from Difficulty/what we have
//  - Peaceful - stone sword, pickaxe, shovel, axe (Only need tools for quickness - no defense required - sword for collecting food quicker)
//  - Easy - iron sword, shield, pickaxe, shovel, axe (Minimal defense required)
//  - Normal - iron armour, iron sword, shield, pickaxe, shovel, axe (More defense required)
//  - Hard - diamond armour, diamond sword, shield, pickaxe, shovel, axe (Maximum defense required)
//  -
//  - Determine Tool Resource From Material List Size (i.e small schematic only use stone,
//    but for larger schematics get fastest tool)
//  -
//  ✅ 2. Then Check Food Level
//  3. Determine if Materials can fit in inventory
//   - if true: continue
//   - else:
//       - collect chests required - calculate required space for task
//       - place chests nearby schematic - store in these chests when inventory gets full
//  ✅ 4. Gather Schematic Resources
//  ✅ 5. Build Schematic
//
//  TODO - Notes
//   - Minimum Mine Range (Minimum we need to travel away from Schematic to collect resources (preserves surrounding area)
//   - Litematica Printer Maybe
public class SchematicBuildTask extends Task {

    private static final int FOOD_UNITS = 80;
    private static final int MIN_FOOD_UNITS = 10;

    private BuilderProcess builder;
    private SchematicBaritoneSettings settings;
    private Vec3i schematicSize;
    private Map<BlockState, Integer> missing;
    private CubeBounds bounds;
    private ISchematic schematic; // TODO - Get open schematic /shrug
    private File schematicFile;

    private boolean finished;
    private boolean sourced;
    private boolean addedAvoidance;

    private final String _schematicFileName;
    private final BlockPos _startPos;
    private final TimerGame _clickTimer = new TimerGame(30);
    private final MovementProgressChecker _moveChecker = new MovementProgressChecker(4, 0.1, 4, 0.01);

    private Task _foodTask;
    private Task _walkTask;
    private Task _resourceTask;

    public SchematicBuildTask(final String schematicFileName) {
        this(schematicFileName, Objects.requireNonNull(MinecraftClient.getInstance().player).getBlockPos());
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos) {
        this.sourced = false;
        this.addedAvoidance = false;
        this._schematicFileName = schematicFileName;
        this._startPos = startPos;
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        this.finished = false;

        var dir = new File(MinecraftClient.getInstance().runDirectory, "schematics");
        var file = new File(dir, _schematicFileName);

        if (!file.exists() || _schematicFileName.isBlank()) {
            Debug.logMessage("Could not locate schematic file. Terminating...");
            if (dir.exists()) {
                var _files = Objects.requireNonNullElse(dir.listFiles(), new File[]{});
                var _str = String.join(" ", Stream.of(_files).map(File::getName).toList());
                Debug.logMessage("Available Schematics: %s".formatted(_str));
            }
            this.finished = true;
            return;
        }

//        this.schematicFile = file;

        if (isNull(builder)) {
            builder = mod.getClientBaritone().getBuilderProcess();
        }

        if (isNull(settings)) {
            settings = new SchematicBaritoneSettings(mod);
        }

        builder.clearState();

        settings.onStart();

        buildSchematic(file);

        if (isNull(schematicSize)) {
            this.schematicSize = builder.getSchemSize();
        }

        overrideMissing();

        if (!isNull(schematicSize) && builder.isFromAltoclef() && !this.addedAvoidance) {
            this.bounds = new CubeBounds(mod.getPlayer().getBlockPos(), this.schematicSize.getX(), this.schematicSize.getY(), this.schematicSize.getZ());
            this.addedAvoidance = true;
            mod.getBehaviour().avoidBlockBreaking(bounds.getPredicate());
        }

        _moveChecker.reset();
        _clickTimer.reset();

        setDebugState("Building Schematic [§l%s, x=%s, y=%s, z%s§r]".formatted(
                this._schematicFileName,
                this._startPos.getX(),
                this._startPos.getY(),
                this._startPos.getZ()
        ));
    }

    private void buildSchematic(File file) {
        if (this.schematic == null) {
            builder.build(this._schematicFileName, file, _startPos, true); //TODO: I think there should be a state queue in baritone
        } else {
            builder.build(this._schematicFileName, this.schematic, _startPos, true);
        }
        this.schematicFile = file;
    }

    @Override
    protected Task onTick(BaritonePlus mod) {
        if (isNull(builder) || isNull(settings) || isNull(schematicSize) || isNull(bounds)) {
            return null;
        }

        overrideMissing();

        this.sourced = false;

        if (!isNull(getMissing()) && !getMissing().isEmpty() && (builder.isPaused() || !builder.isFromAltoclef() || !builder.isActive())) {

            /*if (!mod.getItemStorage().hasEmptyInventorySlot()) {
                setDebugState("Inventory Full, Stashing Items...");
                var _stashStart = startPos.add(100, 0, 100);
                var _stashEnd = _stashStart.add(100, 0, 100);
                return new StoreInStashTask(
                        false,
                        new BlockRange(_stashStart, _stashEnd, WorldHelper.getCurrentDimension()),
                        DepositCommand.getAllNonEquippedOrToolItemsAsTarget(mod)
                );
            }*/

            if (!mod.getBehaviour().inAvoidBlockBreaking(this.bounds.getPredicate())) {
                mod.getBehaviour().avoidBlockBreaking(this.bounds.getPredicate());
            }

            if (StorageHelper.calculateInventoryFoodScore(mod) < MIN_FOOD_UNITS || shouldForce(mod, _foodTask)) {
                setDebugState("Collecting Food First...");
                _foodTask = new CollectFoodTask(FOOD_UNITS);
                return _foodTask;
            }

            // TODO: wait till the resource is done till moving onto the next one
            // Collect Resources in Order - Highest -> Lowest Count
//            if (_resourceTask == null || (!_resourceTask.isActive()
//                    || _resourceTask.isFinished(mod)
//                    || _resourceTask.thisOrChildAreTimedOut()
//                    || shouldForce(mod, _resourceTask)
//            )) {
                var materialList = getTodoList(mod, getMissing()).entrySet()
                        .stream()
                        .sorted(Map.Entry.<BlockState, Integer>comparingByValue().reversed())
                        .toList();

                setDebugState("Sourcing Schematic Materials §a[§l%s§r§a]§r".formatted(String.join(", ",
                        materialList.stream().map(entry -> String.format("x%s %s",
                                entry.getValue(),
                                entry.getKey().getBlock().asItem())
                        ).toList())
                ));

                var _missingTargets = new ArrayList<ItemTarget>();
                for (final Map.Entry<BlockState, Integer> entry : materialList) {
                    var _entryItem = entry.getKey().getBlock().asItem();
                    var _newTarget = new ItemTarget(
                            _entryItem,
                            entry.getValue()
                    );
                    mod.getClientBaritoneSettings().acceptableThrowawayItems.value.remove(_entryItem);
                    _missingTargets.add(_newTarget);
                }
//            }
            _resourceTask = TaskCatalogue.getSquashedItemTask(_missingTargets.toArray(ItemTarget[]::new));
            return _resourceTask;
        } else {
            this.sourced = true;
        }

        mod.getBehaviour().disableAvoidanceOf(this.bounds.getPredicate());

        if (this.sourced && (builder.isPaused() || !builder.isFromAltoclef()) || !builder.isActive()) {
            mod.getClientBaritoneSettings().buildInLayers.value = true;

            if (mod.getBehaviour().inAvoidBlockBreaking(this.bounds.getPredicate())) {
                mod.getBehaviour().disableAvoidanceOf(this.bounds.getPredicate());
            }

            var string = "Resuming Schematic [§l%s, x=%s, y=%s, z%s§r]".formatted(
                    this._schematicFileName,
                    this._startPos.getX(),
                    this._startPos.getY(),
                    this._startPos.getZ()
            );

            Debug.logInternal(string);
            setDebugState(string);

            if (!builder.isActive() && schematicFile != null) {
                buildSchematic(schematicFile);
            } else if (builder.isActive()) {
                builder.resume();
            } else {
                Debug.logMessage("Something went wrong.");
                this.finished = true;
            }
        }

        if (_moveChecker.check(mod)) {
            _clickTimer.reset();
        }

        if (_clickTimer.elapsed()) {
            if (isNull(_walkTask)) {
                _walkTask = new RandomRadiusGoalTask(this._startPos, 5d)
                        .next(mod.getPlayer().getBlockPos());
                Debug.logMessage("Timer elapsed.");
            }
        }

        if (!isNull(_walkTask)) {
            if (!_walkTask.isFinished(mod)) {
                return _walkTask;
            } else {
                _walkTask = null;
                builder.popStack();
                _clickTimer.reset();
                _moveChecker.reset();
            }
        }

        return null;
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        if (isNull(builder) || isNull(settings)) {
            return;
        }

        builder.reset();
        settings.reset();
        _moveChecker.reset();
        _clickTimer.reset();
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
    public boolean isFinished(BaritonePlus mod) {
        return !isNull(builder) && builder.isFromAltoclefFinished() || this.finished;
    }

    private Map<BlockState, Integer> getTodoList(final BaritonePlus mod, final Map<BlockState, Integer> missing) {
        final ItemStorageTracker inventory = mod.getItemStorage();
        final Map<BlockState, Integer> todo = new HashMap<>();

        for (final Map.Entry<BlockState, Integer> entry : missing.entrySet()) {
//            if (entry.getKey().contains(Properties.BED_PART)
//                    && entry.getKey().get(Properties.BED_PART) == BedPart.FOOT
//            ) {
//                // TODO FIX EDGE CASE: Don't add beds twice
//                continue;
//            }

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

    public static void copyAllSchematicsIfNotExists() {
        try {
            var sourcePath = "assets/baritone-plus/schematics";
            var loader = FabricLoader.getInstance();
            var modContainer = loader.getModContainer("baritone-plus")
                    .orElseThrow(() -> new IOException("Mod container not found"));
            var sourceDirectory = modContainer.getPath(sourcePath);

            // The path where you want to copy the schematics to
            var dir = new File(MinecraftClient.getInstance().runDirectory, "schematics");

            // Create the target directory if it does not exist
            if (!dir.exists()) {
                dir.mkdirs();
            }

            Files.walk(sourceDirectory).forEach(path -> {
                var targetFile = new File(dir, sourceDirectory.relativize(path).toString());
                if (!targetFile.exists() && !Files.isDirectory(path)) {
                    try (InputStream in = Files.newInputStream(path)) {
                        FileUtils.copyInputStreamToFile(in, targetFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }

    protected static class SchematicBaritoneSettings {
        @NonNull private final BaritonePlus mod;

        protected SchematicBaritoneSettings(@NonNull BaritonePlus mod) {
            this.mod = mod;
        }

        // TODO - Save Users Values Before Task and Restore Those
        protected void onStart() {
            mod.getClientBaritoneSettings().allowParkour.value = true;
            mod.getClientBaritoneSettings().allowParkourAscend.value = true;
            mod.getClientBaritoneSettings().buildInLayers.value = false;
            mod.getClientBaritoneSettings().buildIgnoreDirection.value = false;
            mod.getClientBaritoneSettings().buildIgnoreExisting.value = true;
            mod.getClientBaritoneSettings().breakFromAbove.value = true;

            // Ignore Blocks Baritone Struggles With - Start
            // Add *ALL* Problematic Blocks
            mod.getClientBaritoneSettings().buildIgnoreProperties.value.addAll(List.of(
                    "facing", "half", "shape", "waterlogged", "open", "powered", "occupied", "part", "type"
            ));

            mod.getClientBaritoneSettings().buildSkipBlocks.value.addAll(collectProblematicBlocks());

            /*mod.getClientBaritoneSettings().buildSkipBlocks.value.add(Blocks.CHEST);
            mod.getClientBaritoneSettings().buildSkipBlocks.value.add(Blocks.CHAIN);
            mod.getClientBaritoneSettings().buildSkipBlocks.value.add(Blocks.LANTERN);
            mod.getClientBaritoneSettings().buildSkipBlocks.value.addAll(List.of(
                    ItemHelper.itemsToBlocks(ItemHelper.WOOD_STAIRS)
            ));

            mod.getClientBaritoneSettings().buildSkipBlocks.value.addAll(List.of(
                    ItemHelper.itemsToBlocks(ItemHelper.WOOD_TRAPDOOR)
            ));

            mod.getClientBaritoneSettings().buildSkipBlocks.value.addAll(List.of(
                    ItemHelper.itemsToBlocks(ItemHelper.WOOD_DOOR)
            ));*/

            // Unobtainables
            var unobtainable = List.of(
                    ItemHelper.itemsToBlocks(
                            ItemHelper.getUnobtainables().toArray(Item[]::new)
                    )
            );

            mod.getClientBaritoneSettings().okIfAir.value.addAll(unobtainable);

            // Block Substitutions
            for (Block block : ItemHelper.itemsToBlocks(ItemHelper.BED)) {
                mod.getClientBaritoneSettings().buildSubstitutes.value.put(block,
                        List.of(ItemHelper.itemsToBlocks(ItemHelper.BED))
                );
            }

            // Ignore Grass
            mod.getClientBaritoneSettings().buildSubstitutes.value.put(Blocks.GRASS_BLOCK, List.of(Blocks.DIRT, Blocks.GRASS_BLOCK));
            mod.getClientBaritoneSettings().buildValidSubstitutes.value.put(Blocks.GRASS_BLOCK, List.of(Blocks.DIRT, Blocks.GRASS_BLOCK));
            mod.getClientBaritoneSettings().buildValidSubstitutes.value.put(Blocks.DIRT, List.of(Blocks.GRASS_BLOCK, Blocks.DIRT));
        }

        protected void reset() {
            mod.getClientBaritoneSettings().allowParkour.value =
                    mod.getClientBaritoneSettings().allowParkour.defaultValue;

            mod.getClientBaritoneSettings().allowParkourAscend.value =
                    mod.getClientBaritoneSettings().allowParkourAscend.defaultValue;

            mod.getClientBaritoneSettings().buildInLayers.value =
                    mod.getClientBaritoneSettings().buildInLayers.defaultValue;

            mod.getClientBaritoneSettings().buildIgnoreDirection.value =
                    mod.getClientBaritoneSettings().buildIgnoreDirection.defaultValue;

            mod.getClientBaritoneSettings().buildIgnoreExisting.value =
                    mod.getClientBaritoneSettings().buildIgnoreExisting.defaultValue;

            mod.getClientBaritoneSettings().buildIgnoreProperties.value =
                    mod.getClientBaritoneSettings().buildIgnoreProperties.defaultValue;

            mod.getClientBaritoneSettings().buildSkipBlocks.value =
                    mod.getClientBaritoneSettings().buildSkipBlocks.defaultValue;

            mod.getClientBaritoneSettings().okIfAir.value =
                    mod.getClientBaritoneSettings().okIfAir.defaultValue;

            mod.getClientBaritoneSettings().buildSubstitutes.value =
                    mod.getClientBaritoneSettings().buildSubstitutes.defaultValue;

            mod.getClientBaritoneSettings().buildValidSubstitutes.value =
                    mod.getClientBaritoneSettings().buildValidSubstitutes.defaultValue;
        }

        public List<Block> collectProblematicBlocks() {
            List<Block> problematicBlocks = new ArrayList<>();

            for (Identifier identifier : Registries.BLOCK.getIds()) {
                Block block = Registries.BLOCK.get(identifier);
                BlockState defaultState = block.getDefaultState();

                // Check for specific conditions that define a block as problematic
                if (hasProperty(defaultState) || isSpecialBlock(block)) {
                    problematicBlocks.add(block);
                }
            }

            return problematicBlocks;
        }

        private boolean isSpecialBlock(Block block) {
            return block instanceof net.minecraft.block.StairsBlock
                    || block instanceof net.minecraft.block.ChestBlock
                    || block instanceof net.minecraft.block.ChainBlock
                    || block instanceof net.minecraft.block.LanternBlock
                    || block instanceof net.minecraft.block.TrapdoorBlock
                    || block instanceof net.minecraft.block.DoorBlock;
        }

        private static boolean hasProperty(BlockState state) {
            return state.getProperties().isEmpty();
        }
    }
}
