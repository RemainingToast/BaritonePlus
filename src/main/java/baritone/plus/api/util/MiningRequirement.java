package baritone.plus.api.util;

import baritone.plus.api.util.helpers.ItemHelper;
import baritone.plus.main.Debug;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.Map;

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
//        if (this == HAND) return null;

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

    /**
     * In this code, we want to check if we have to collect a lot of items.
     * If so, we want to increase the mining requirement to make the mining more efficient.
     * To do this, we are counting all the items that we need more than or equal to 64 of.
     *
     * @return _requirement
     */
    public static MiningRequirement optimiseMiningRequirement(ItemTarget[] targets, MiningRequirement _requirement) {
        int _countThreshold = switch (_requirement) {
            case HAND -> 16;
            case WOOD -> 32;
            default -> 64;
        };

        Map<String, Integer> _count = new HashMap<>();

//        Debug.logInternal("[DEBUG] Item Targets: %s", String.join(", ", Arrays.stream(targets)
//                .map(ItemTarget::toString)
//                .toList()
//        ));
//        Debug.logInternal("[DEBUG] Initial requirement: %s", _requirement);
//        Debug.logInternal("[DEBUG] Count threshold: %d", _countThreshold);

        for (ItemTarget target : targets) {
            String itemName = target.toString();
            int targetCount = target.getTargetCount();
//            Debug.logInternal("[DEBUG] Adding item: %s, target count: %d", itemName, targetCount);
            _count.put(itemName, _count.getOrDefault(itemName, 0) + targetCount);
        }

        for (Map.Entry<String, Integer> entry : _count.entrySet()) {
            int count = entry.getValue();
//            Debug.logInternal("[DEBUG] Item: %s, Initial Count: %d", entry.getKey(), count);

            while (count >= _countThreshold) {
                _requirement = _requirement.next();
                count -= _countThreshold;
//                Debug.logInternal("[DEBUG] Updating requirement: %s, Remaining Count: %d", _requirement, count);
            }
        }

//        Debug.logInternal("[DEBUG] Final requirement: %s", _requirement);
//        Debug.logInternal("[DEBUG] Final count: %s", String.join(", ", _count.entrySet().stream().map(entry -> {
//            return String.format("%sx %s", entry.getValue(), entry.getKey());
//        }).toList()));

        return _requirement;
    }
}
