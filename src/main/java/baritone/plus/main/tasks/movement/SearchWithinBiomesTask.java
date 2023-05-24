package baritone.plus.main.tasks.movement;

import baritone.plus.api.tasks.Task;
import baritone.plus.main.BaritonePlus;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.util.Set;

public class SearchWithinBiomesTask extends SearchChunksExploreTask {

    private final Set<RegistryKey<Biome>> _toSearch;

    public SearchWithinBiomesTask(Set<RegistryKey<Biome>> toSearch) {
        _toSearch = toSearch;
    }

    @Override
    protected boolean isChunkWithinSearchSpace(BaritonePlus mod, ChunkPos pos) {
        RegistryEntry<Biome> _biome = mod.getWorld().getBiome(pos.getStartPos().add(1, 1, 1));
        return _toSearch.stream().anyMatch(_biome::matchesKey);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof SearchWithinBiomesTask task) {
            return task._toSearch.equals(_toSearch);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Searching for+within biomes: " + _toSearch;
    }
}
