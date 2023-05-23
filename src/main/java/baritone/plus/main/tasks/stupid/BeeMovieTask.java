package baritone.plus.main.tasks.stupid;

import baritone.plus.main.BaritonePlus;
import baritone.plus.main.Debug;
import baritone.plus.main.tasks.construction.DestroyBlockTask;
import baritone.plus.main.tasks.construction.PlaceSignTask;
import baritone.plus.main.tasks.construction.PlaceStructureBlockTask;
import baritone.plus.main.tasks.resources.MineAndCollectTask;
import baritone.plus.main.tasks.squashed.CataloguedResourceTask;
import baritone.plus.api.tasks.Task;
import baritone.plus.api.util.ItemTarget;
import baritone.plus.api.util.MiningRequirement;
import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.api.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes a stream input, like from a file, and places signs in a line that say the contents
 * of that stream. Use for absurd bullshit like making a bot that prints out the ENTIRE bee movie script
 * with signs.
 */
public class BeeMovieTask extends Task {

    // How many building materials to collect/buffer up
    private static final int STRUCTURE_MATERIALS_BUFFER = 64;

    private final BlockPos _start;
    private final BlockPos _direction = new BlockPos(0, 0, -1);

    private final StreamedSignStringParser _textParser;

    private final String _uniqueId;
    private final Task _extraSignAcquireTask;
    private final Task _structureMaterialsTask;
    private final List<String> _cachedStrings = new ArrayList<>();
    // Grab extra resources and acquire extra tools for speed
    private final boolean _sharpenTheAxe = true;
    private boolean _finished = false;
    private PlaceSignTask _currentPlace = null;

    public BeeMovieTask(String uniqueId, BlockPos start, InputStreamReader input) {
        _uniqueId = uniqueId;
        _start = start;
        _textParser = new StreamedSignStringParser(input);

        _extraSignAcquireTask = new CataloguedResourceTask(new ItemTarget("sign", 256));//TaskCatalogue.getItemTask("sign", 32);
        _structureMaterialsTask = new MineAndCollectTask(new ItemTarget(new Item[]{Items.DIRT, Items.COBBLESTONE}, STRUCTURE_MATERIALS_BUFFER), new Block[]{Blocks.STONE, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.GRASS, Blocks.GRASS_BLOCK}, MiningRequirement.WOOD);
    }

    private static int sign(int num) {
        return Integer.compare(num, 0);
    }

    private static boolean isSign(Block block) {
        if (block == null) return false;
        Block[] candidates = ItemHelper.WOOD_SIGNS_ALL;
        for (Block candidate : candidates) {
            if (block == candidate) return true;
        }
        return false;
    }

    @Override
    protected void onStart(BaritonePlus mod) {
        mod.getBehaviour().push();
        // Prevent mineshaft garbage
        mod.getBehaviour().setExclusivelyMineLogs(true);

        // Avoid breaking the ground below the signs.
        mod.getBehaviour().avoidBlockBreaking(this::isOnPath);
        // Avoid placing blocks where the signs should be placed.
        mod.getBehaviour().avoidBlockPlacing(block -> isOnPath(block.down()));
    }

    // Whether a block pos is on the path of its signs.
    private boolean isOnPath(BlockPos pos) {
        BlockPos bottomStart = _start.down();
        BlockPos delta = pos.subtract(bottomStart);
        return sign(delta.getX()) == sign(_direction.getX())
                && sign(delta.getY()) == sign(_direction.getY())
                && sign(delta.getZ()) == sign(_direction.getZ());
    }

    @Override
    protected Task onTick(BaritonePlus mod) {

        if (_currentPlace != null && _currentPlace.isActive() && !_currentPlace.isFinished(mod)) {
            setDebugState("Placing...");
            return _currentPlace;
        }

        if (_sharpenTheAxe) {
            if (!mod.getItemStorage().hasItem(Items.DIAMOND_AXE) || !mod.getItemStorage().hasItem(Items.DIAMOND_SHOVEL) || !mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE)) {
                setDebugState("Sharpening the axe: Tools");
                return new CataloguedResourceTask(new ItemTarget(Items.DIAMOND_AXE, 1), new ItemTarget("diamond_shovel", 1), new ItemTarget("diamond_pickaxe", 1));
            }
            if (_extraSignAcquireTask.isActive() && !_extraSignAcquireTask.isFinished(mod)) {
                setDebugState("Sharpening the axe: Signs");
                return _extraSignAcquireTask;
            }
            if (!mod.getItemStorage().hasItem(ItemHelper.WOOD_SIGN)) {
                // Get a bunch of signs in bulk
                return _extraSignAcquireTask;
            }
        }

        // Get building blocks
        int buildCount = mod.getItemStorage().getItemCount(Items.DIRT, Items.COBBLESTONE);
        if (buildCount < STRUCTURE_MATERIALS_BUFFER && (buildCount == 0 || _structureMaterialsTask.isActive())) {
            setDebugState("Collecting structure blocks...");
            return _structureMaterialsTask;
        }

        int signCounter = 0;
        // NOTE: This only checks for the EXISTANCE of signs, NOT that they have the proper text.
        BlockPos currentSignPos = _start;
        while (true) {
            assert Minecraft.getMinecraft().world != null;


            boolean loaded = mod.getChunkTracker().isChunkLoaded(currentSignPos);

            // Clear above
            IBlockState above = Minecraft.getMinecraft().world.getBlockState(currentSignPos.up());
            if (loaded && !above.isAir() && above.getBlock() != Blocks.WATER) {
                setDebugState("Clearing block above to prevent hanging...");
                return new DestroyBlockTask(currentSignPos.up());
            }

            // Fortify below
            //IBlockState below = Minecraft.getMinecraft().world.getBlockState(currentSignPos.down());
            boolean canPlace = WorldHelper.isSolid(mod, currentSignPos.down());//isSideSolidFullSquare(Minecraft.getMinecraft().world, currentSignPos.down(), EnumFacing.UP);
            if (loaded && !canPlace) {
                setDebugState("Placing block below for sign placement...");
                return new PlaceStructureBlockTask(currentSignPos.down());
            }

            // Need a sign at this point.
            while (_cachedStrings.size() <= signCounter) {
                // Load up if we're at a new index.
                if (!_textParser.hasNextSign()) {
                    Debug.logMessage("DONE!!!!");
                    _finished = true;
                    return null;
                }
                String next = _textParser.getNextSignString();
                Debug.logMessage("NEXT SIGN: " + next);
                _cachedStrings.add(next);
            }


            IBlockState blockAt = Minecraft.getMinecraft().world.getBlockState(currentSignPos);


            if (loaded && !isSign(blockAt.getBlock())) {
                // INVALID! place.
                _currentPlace = new PlaceSignTask(currentSignPos, _cachedStrings.get(signCounter));
                return _currentPlace;
            }

            currentSignPos = currentSignPos.add(_direction);
            signCounter++;
        }
    }

    @Override
    protected void onStop(BaritonePlus mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof BeeMovieTask) {
            return ((BeeMovieTask) other)._uniqueId.equals(_uniqueId);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Dead Meme \"" + _uniqueId + "\" at " + _start;
    }

    @Override
    public boolean isFinished(BaritonePlus mod) {
        return _finished;
    }

    static class StreamedSignStringParser {
        private final BufferedReader _reader;

        private boolean _done = false;

        public StreamedSignStringParser(InputStreamReader source) {
            _reader = new BufferedReader(source);
        }

        public void open() {
            try {
                _reader.reset();
                _done = false;
            } catch (IOException e) {
                // Failed.
                e.printStackTrace();
            }
        }

        public boolean hasNextSign() {
            return !_done;
        }

        public String getNextSignString() {

            final double SIGN_TEXT_MAX_WIDTH = 90;
            int lineCount = 0;
            StringBuilder line = new StringBuilder();

            StringBuilder result = new StringBuilder();

            while (true) {
                int in;
                try {
                    _reader.mark(1);
                    in = _reader.read();
                } catch (IOException e) {
                    e.printStackTrace();
                    _done = true;
                    break;
                }
                if (in == -1) {
                    _done = true;
                    break;
                }
                char c = (char) in;
                //Debug.logMessage("Read " + c);

                line.append(c);

                boolean done = c == '\0';

                // Can be a special delimiter for a new sign.

                if (c == '\n' || Minecraft.getMinecraft().textRenderer.getWidth(line.toString()) > SIGN_TEXT_MAX_WIDTH) {
                    line.delete(0, line.length());
                    line.append(c);
                    lineCount++;
                    if (lineCount >= 4) {
                        // We're out of bounds. Put the last character back.
                        try {
                            _reader.reset();
                        } catch (IOException e) {
                            // Not much to do honestly...
                            e.printStackTrace();
                        }

                        done = true;
                    }
                }

                if (done) {
                    break;
                }

                result.append(c);
            }

            return result.toString();
        }
    }

}
