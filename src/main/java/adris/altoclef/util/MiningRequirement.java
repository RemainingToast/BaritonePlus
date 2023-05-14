package adris.altoclef.util;

import adris.altoclef.Debug;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum MiningRequirement implements Comparable<MiningRequirement> {
    HAND(new Item[]{Items.AIR}, Blocks.DIRT.getDefaultState()),
    WOOD(ItemHelper.WOODEN_TOOLS, Blocks.STONE.getDefaultState()),
    STONE(ItemHelper.STONE_TOOLS, Blocks.IRON_ORE.getDefaultState()),
    IRON(ItemHelper.IRON_TOOLS, Blocks.DIAMOND_ORE.getDefaultState()),
    DIAMOND(ItemHelper.DIAMOND_TOOLS, Blocks.OBSIDIAN.getDefaultState());

    private final Item[] _tools;
    private final BlockState _toMine;

    MiningRequirement(Item[] tools, BlockState toMine) {
        _tools = tools;
        _toMine = toMine;
    }

    public static MiningRequirement getMinimumRequirementForBlock(Block block) {
        if (block.getDefaultState().isToolRequired()) {
            for (MiningRequirement req : MiningRequirement.values()) {
                if (req == MiningRequirement.HAND) continue;
                Item pick = req.getBestTool(block.getDefaultState());
                if (pick != null && pick.isSuitableFor(block.getDefaultState())) {
                    return req;
                }
            }
            Debug.logWarning("Failed to find ANY effective tool against: " + block + ". I assume netherite is not required anywhere, so something else probably went wrong.");
            return MiningRequirement.DIAMOND;
        }
        return MiningRequirement.HAND;
    }

    public Item getBestTool(BlockState block) {
        if (this == HAND) return null;

        // Loop over the tools
        for (Item tool : _tools) {
            // Check if the tool is suitable for the block
            if (tool.isSuitableFor(block)) {
                return tool;
            }
        }

        // If no suitable tool is found, return null
        return null;
    }

    public BlockState getBlockState() {
        return _toMine;
    }

    public MiningRequirement next() {
        // Get the list of enum values
        MiningRequirement[] values = MiningRequirement.values();

        // If this is the last element (DIAMOND), return this
        if (this == DIAMOND) {
            return this;
        }

        // Find the index of the next element
        int nextIndex = (this.ordinal() + 1) % values.length;

        // If the next index is HAND (0), then increment it by 1 to ensure it's always a tool
        if (nextIndex == 0) {
            nextIndex++;
        }

        // Return the next element
        return values[nextIndex];
    }
}
