package baritone.plus.api.util.schematic;

import baritone.plus.main.Debug;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO
public class SchematicReader {

    public static Map<BlockState, Integer> readSchematicBlocks(File file) throws IOException {
        Map<BlockState, Integer> blockCounts = new HashMap<>();

        NbtCompound schematicNBT = loadSchematicNBT(file);
        if (schematicNBT != null) {
            NbtList paletteTag = schematicNBT.getList("Palette", 10);
            byte[] blockData = schematicNBT.getByteArray("BlockData");
            NbtList blockEntities = schematicNBT.getList("BlockEntities", 10);

            for (int i = 0; i < blockData.length; i++) {
                byte data = blockData[i];
                int blockStateId = data & 0xFF;
                BlockState blockState = getBlockStateFromPalette(paletteTag, blockStateId);

                if (blockState != null) {
                    blockCounts.merge(blockState, 1, Integer::sum);

                    // Process block entity if present
                    if (i < blockEntities.size()) {
                        NbtCompound blockEntityTag = blockEntities.getCompound(i);
                        processBlockEntity(blockEntityTag);
                    }
                }
            }
        } else {
            Debug.logInternal("schematicNBT is null!");
        }

        return blockCounts;
    }

    private static void processBlockEntity(NbtCompound blockEntityTag) {
        // Process the block entity here
        BlockPos pos = new BlockPos(blockEntityTag.getInt("x"), blockEntityTag.getInt("y"), blockEntityTag.getInt("z"));
        String blockEntityId = blockEntityTag.getString("Id");
        Debug.logInternal("Block Entity: " + blockEntityId + ", Position: " + pos);
        // Additional processing logic for the block entity
    }

    private static NbtCompound loadSchematicNBT(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            NbtCompound nbt = NbtIo.readCompressed(fis);
            Debug.logInternal(nbt.toString());
            return nbt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BlockState getBlockStateFromPalette(NbtList paletteTag, int index) {
        if (index >= 0 && index < paletteTag.size()) {
            NbtElement tag = paletteTag.get(index);
            if (tag instanceof NbtCompound compoundTag) {
                String nameTag = compoundTag.getString("Name");
                Block block = Registries.BLOCK.get(new Identifier(nameTag));
                return block.getDefaultState();
            }
        }
        return null;
    }

    public static void test(File schematicFile) {
        Debug.logInternal(schematicFile.toString());

        try {
            Map<BlockState, Integer> blockCounts = readSchematicBlocks(schematicFile);
            Debug.logInternal(blockCounts.toString());
            for (Map.Entry<BlockState, Integer> entry : blockCounts.entrySet()) {
                BlockState blockState = entry.getKey();
                int count = entry.getValue();
                String msg = "Block State: " + blockState + ", Count: " + count;
                Debug.logInternal(msg);
                Debug.logMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}