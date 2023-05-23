package baritone.plus.main;

import baritone.Baritone;
import baritone.altoclef.AltoClefSettings;
import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.plus.api.butler.Butler;
import baritone.plus.api.control.InputControls;
import baritone.plus.api.control.PlayerExtraController;
import baritone.plus.api.control.SlotHandler;
import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.ClientRenderEvent;
import baritone.plus.api.event.events.ClientTickEvent;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.tasks.TaskRunner;
import baritone.plus.api.trackers.*;
import baritone.plus.api.trackers.storage.ContainerSubTracker;
import baritone.plus.api.trackers.storage.ItemStorageTracker;
import baritone.plus.api.util.helpers.InputHelper;
import baritone.plus.brain.BaritoneBrain;
import baritone.plus.main.chains.*;
import baritone.plus.main.ui.CommandStatusOverlay;
import baritone.plus.main.ui.MessagePriority;
import baritone.plus.main.ui.MessageSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.lwjgl.input.Keyboard;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class BaritonePlus {

    public static BaritonePlus INSTANCE;

    // Static access to altoclef
    private static final Queue<Consumer<BaritonePlus>> _postInitQueue = new ArrayDeque<>();

    // Central Managers
//    private static CommandExecutor _commandExecutor;
    private TaskRunner _taskRunner;
    private TrackerManager _trackerManager;
    private BotBehaviour _botBehaviour;
    private PlayerExtraController _extraController;
    // Task chains
    private UserTaskChain _userTaskChain;
    private FoodChain _foodChain;
    private MobDefenseChain _mobDefenseChain;
    private MLGBucketFallChain _mlgBucketChain;
    // Trackers
    private ItemStorageTracker _storageTracker;
    private ContainerSubTracker _containerSubTracker;
    private EntityTracker _entityTracker;
    private BlockTracker _blockTracker;
    private SimpleChunkTracker _chunkTracker;
    private MiscBlockTracker _miscBlockTracker;
    // Renderers
    private CommandStatusOverlay _commandStatusOverlay;
    // Settings
    private PlusSettings _settings;
    // Misc managers/input
    private MessageSender _messageSender;
    private InputControls _inputControls;
    private SlotHandler _slotHandler;
    // Butler
    private Butler _butler;
    // Baritone Brain
    private BaritoneBrain _baritoneBrain;

    // Are we in game (playing in a server/world)
    public static boolean inGame() {
        return Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().world != null;
    }

    /**
     * Executes commands (ex. `@get`/`@gamer`)
     */
//    public static CommandExecutor getCommandExecutor() {
//        return _commandExecutor;
//    }

    public void onInitializeLoad() {
        // This code should be run after Minecraft loads everything else in.
        // This is the actual start point, controlled by a mixin.
        INSTANCE = this;

        initializeBaritoneSettings();

        // Central Managers
//        _commandExecutor = new CommandExecutor(this);
        _taskRunner = new TaskRunner(this);
        _trackerManager = new TrackerManager(this);
        _botBehaviour = new BotBehaviour(this);
        _extraController = new PlayerExtraController(this);

        // Task chains
        _userTaskChain = new UserTaskChain(_taskRunner);
        _mobDefenseChain = new MobDefenseChain(_taskRunner);
        new DeathMenuChain(_taskRunner);
        new PlayerInteractionFixChain(_taskRunner);
        _mlgBucketChain = new MLGBucketFallChain(_taskRunner);
        new WorldSurvivalChain(_taskRunner);
        _foodChain = new FoodChain(_taskRunner);

        // Trackers
        _storageTracker = new ItemStorageTracker(this, _trackerManager, container -> _containerSubTracker = container);
        _entityTracker = new EntityTracker(_trackerManager);
        _blockTracker = new BlockTracker(this, _trackerManager);
        _chunkTracker = new SimpleChunkTracker(this);
        _miscBlockTracker = new MiscBlockTracker(this);

        // Renderers
        _commandStatusOverlay = new CommandStatusOverlay();

        // Misc managers
        _messageSender = new MessageSender();
        _inputControls = new InputControls();
        _slotHandler = new SlotHandler(this);

        _butler = new Butler(this);
        _baritoneBrain = new BaritoneBrain(this);

        initializeCommands();

        // Load settings
        PlusSettings.load(newSettings -> {
            _settings = newSettings;
            // Baritone's `acceptableThrowawayItems` should match our own.
            List<Item> baritoneCanPlace = Arrays.stream(_settings.getThrowawayItems(this, true))
                    .filter(item -> item != Item.getItemFromBlock(Blocks.SOUL_SAND)
                            && item != Item.getItemFromBlock(Blocks.MAGMA)
                            && item != Item.getItemFromBlock(Blocks.SAND)
                            && item != Item.getItemFromBlock(Blocks.GRAVEL)).toList();
            getClientBaritoneSettings().acceptableThrowawayItems.value.addAll(baritoneCanPlace);
            // If we should run an idle command...
            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask()) && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
//                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }
            // Don't break blocks or place blocks where we are explicitly protected.
            getExtraBaritoneSettings().avoidBlockBreak(blockPos -> _settings.isPositionExplicitlyProtected(blockPos));
            getExtraBaritoneSettings().avoidBlockPlace(blockPos -> _settings.isPositionExplicitlyProtected(blockPos));
        });

        // Receive + cancel chat
//        EventBus.subscribe(SendChatEvent.class, evt -> {
//            String line = evt.message;
//            if (getCommandExecutor().isClientCommand(line)) {
//                evt.cancel();
//                getCommandExecutor().execute(line);
//            }
//        });

        // Debug jank/hookup
        Debug.jankModInstance = this;

        // Tick with the client
        EventBus.subscribe(ClientTickEvent.class, evt -> onClientTick());
        // Render
        EventBus.subscribe(ClientRenderEvent.class, evt -> onClientRenderOverlay(/*evt.stack*/));

        // Playground
        Playground.IDLE_TEST_INIT_FUNCTION(this);

        // External mod initialization
        runEnqueuedPostInits();
    }

    // Client tick
    private void onClientTick() {
        runEnqueuedPostInits();

        _inputControls.onTickPre();

        // Cancel shortcut
        // TODO CONFIG
        if (InputHelper.isKeyPressed(Keyboard.KEY_LCONTROL) && InputHelper.isKeyPressed(Keyboard.KEY_K)) {
            _userTaskChain.cancel(this);
            if (_taskRunner.getCurrentTaskChain() != null) {
                _taskRunner.getCurrentTaskChain().stop(this);
            }
        }

        // TODO: should this go here?
        _storageTracker.setDirty();
        _containerSubTracker.onServerTick();
        _miscBlockTracker.tick();

        _trackerManager.tick();
        _blockTracker.preTickTask();
        _taskRunner.tick();
        _blockTracker.postTickTask();

        _butler.tick();
        _messageSender.tick();

        _inputControls.onTickPost();
    }

    /// GETTERS AND SETTERS

    private void onClientRenderOverlay(/*MatrixStack matrixStack*/) {
        _commandStatusOverlay.render(this/*, matrixStack*/);
    }

    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);
        getClientBaritoneSettings().freeLook.value = false;
        getClientBaritoneSettings().overshootTraverse.value = false;
        getClientBaritoneSettings().allowOvershootDiagonalDescend.value = true;
        getClientBaritoneSettings().allowInventory.value = true;
        getClientBaritoneSettings().allowParkour.value = false;
        getClientBaritoneSettings().allowParkourAscend.value = false;
        getClientBaritoneSettings().allowParkourPlace.value = false;
        getClientBaritoneSettings().allowDiagonalDescend.value = false;
        getClientBaritoneSettings().allowDiagonalAscend.value = false;
        getClientBaritoneSettings().blocksToAvoid.value = List.of(Blocks.FLOWERING_AZALEA, Blocks.AZALEA,
                Blocks.POWDER_SNOW, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.SWEET_BERRY_BUSH,
                Blocks.WARPED_ROOTS, Blocks.VINE, Blocks.GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
                Blocks.AMETHYST_CLUSTER, Blocks.SCULK, Blocks.SCULK_VEIN);
        // Let baritone move items to hotbar to use them
        // Reduces a bit of far rendering to save FPS
        getClientBaritoneSettings().fadePath.value = true;
        // Don't let baritone scan dropped items, we handle that ourselves.
        getClientBaritoneSettings().mineScanDroppedItems.value = false;
        // Don't let baritone wait for drops, we handle that ourselves.
        getClientBaritoneSettings().mineDropLoiterDurationMSThanksLouca.value = 0L;

        // Water bucket placement will be handled by us exclusively
        getExtraBaritoneSettings().configurePlaceBucketButDontFall(true);

        // For render smoothing
        getClientBaritoneSettings().randomLooking.value = 0.0;
        getClientBaritoneSettings().randomLooking113.value = 0.0;

        // Give baritone more time to calculate paths. Sometimes they can be really far away.
        // Was: 2000L
        getClientBaritoneSettings().failureTimeoutMS.reset();
        // Was: 5000L
        getClientBaritoneSettings().planAheadFailureTimeoutMS.reset();
        // Was 100
        getClientBaritoneSettings().movementTimeoutTicks.reset();
    }

    // List all command sources here.
    private void initializeCommands() {
        try {
            // This creates the commands. If you want any more commands feel free to initialize new command lists.
            new PlusCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the highest priority task chain
     * (task chains run the task tree)
     */
    public TaskRunner getTaskRunner() {
        return _taskRunner;
    }

    /**
     * The user task chain (runs your command. Ex. Get Diamonds, Beat the Game)
     */
    public UserTaskChain getUserTaskChain() {
        return _userTaskChain;
    }

    /**
     * Controls bot behaviours, like whether to temporarily "protect" certain blocks or items
     */
    public BotBehaviour getBehaviour() {
        return _botBehaviour;
    }

    /**
     * Tracks items in your inventory and in storage containers.
     */
    public ItemStorageTracker getItemStorage() {
        return _storageTracker;
    }

    /**
     * Tracks loaded entities
     */
    public EntityTracker getEntityTracker() {
        return _entityTracker;
    }

    /**
     * Tracks blocks and their positions
     */
    public BlockTracker getBlockTracker() {
        return _blockTracker;
    }

    /**
     * Tracks of whether a chunk is loaded/visible or not
     */
    public SimpleChunkTracker getChunkTracker() {
        return _chunkTracker;
    }

    /**
     * Tracks random block things, like the last nether portal we used
     */
    public MiscBlockTracker getMiscBlockTracker() {
        return _miscBlockTracker;
    }

    /**
     * Baritone access (could just be static honestly)
     */
    public Baritone getClientBaritone() {
        if (getPlayer() == null) {
            return (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
        }
        return (Baritone) BaritoneAPI.getProvider().getBaritoneForPlayer(getPlayer());
    }

    /**
     * Baritone settings access (could just be static honestly)
     */
    public Settings getClientBaritoneSettings() {
        return Baritone.settings();
    }

    /**
     * Baritone settings special to AltoClef (could just be static honestly)
     */
    public AltoClefSettings getExtraBaritoneSettings() {
        return AltoClefSettings.getInstance();
    }

    /**
     * AltoClef Settings
     */
    public PlusSettings getModSettings() {
        return _settings;
    }

    /**
     * Butler controller. Keeps track of users and lets you receive user messages
     */
    public Butler getButler() {
        return _butler;
    }

    /**
     * Sends chat messages (avoids auto-kicking)
     */
    public MessageSender getMessageSender() {
        return _messageSender;
    }

    /**
     * Does Inventory/container slot actions
     */
    public SlotHandler getSlotHandler() {
        return _slotHandler;
    }

    /**
     * Minecraft player client access (could just be static honestly)
     */
    public EntityPlayerSP getPlayer() {
        return mc().player;
    }

    /**
     * Minecraft world access (could just be static honestly)
     */
    public WorldClient getWorld() {
        return mc().world;
    }

    public Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    /**
     * Minecraft client interaction controller access (could just be static honestly)
     */
    public PlayerControllerMP getController() {
        return Minecraft.getMinecraft().playerController;
    }

    /**
     * Extra controls not present in ClientPlayerInteractionManager. This REALLY should be made static or combined with something else.
     */
    public PlayerExtraController getControllerExtras() {
        return _extraController;
    }

    /**
     * Manual control over input actions (ex. jumping, attacking)
     */
    public InputControls getInputControls() {
        return _inputControls;
    }

    /**
     * Run a user task
     */
    public void runUserTask(Task task) {
        runUserTask(task, () -> {
        });
    }

    /**
     * Run a user task
     */
    public void runUserTask(Task task, Runnable onFinish) {
        _userTaskChain.runTask(this, task, onFinish);
    }

    /**
     * Cancel currently running user task
     */
    public void cancelUserTask() {
        _userTaskChain.cancel(this);
    }

    /**
     * Takes control away to eat food
     */
    public FoodChain getFoodChain() {
        return _foodChain;
    }

    /**
     * Takes control away to defend against mobs
     */
    public MobDefenseChain getMobDefenseChain() {
        return _mobDefenseChain;
    }

    /**
     * Takes control away to perform bucket saves
     */
    public MLGBucketFallChain getMLGBucketChain() {
        return _mlgBucketChain;
    }


    public BaritoneBrain getBaritoneBrain() {
        return _baritoneBrain;
    }

    public void log(String message) {
        log(message, MessagePriority.TIMELY);
    }

    /**
     * Logs to the console and also messages any player using the bot as a butler.
     */
    public void log(String message, MessagePriority priority) {
        Debug.logMessage(message);
        _butler.onLog(message, priority);
    }

    public void logWarning(String message) {
        logWarning(message, MessagePriority.TIMELY);
    }

    /**
     * Logs a warning to the console and also alerts any player using the bot as a butler.
     */
    public void logWarning(String message, MessagePriority priority) {
        Debug.logWarning(message);
        _butler.onLogWarning(message, priority);
    }

    private void runEnqueuedPostInits() {
        synchronized (_postInitQueue) {
            while (!_postInitQueue.isEmpty()) {
                _postInitQueue.poll().accept(this);
            }
        }
    }

}
