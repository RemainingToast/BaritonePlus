package adris.altoclef.baritone.brain.utils;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WorldState {
    public final float playerHealth;
    public final int playerHunger;
    public final BlockPos playerLocation;
    public final PlayerInventory playerInventory;
    public final Biome currentBiome;
    public final List<Entity> surroundingEntities;

    public WorldState(ClientWorld world, ClientPlayerEntity player) {
        this.playerHealth = player.getHealth();
        this.playerHunger = player.getHungerManager().getFoodLevel();
        this.playerLocation = player.getBlockPos();
        this.playerInventory = player.getInventory();

        this.currentBiome = world.getBiome(this.playerLocation).value();
        this.surroundingEntities = new ArrayList<>();

        world.getEntities().forEach(entity -> {
            if (entity.distanceTo(entity) <= 48) {
                this.surroundingEntities.add(entity);
            }
        });
    }

    @Override
    public String toString() {
        return "WorldState{" +
                "playerHealth=" + playerHealth +
                ", playerHunger=" + playerHunger +
                ", playerLocation=" + playerLocation +
                ", playerInventory=" + playerInventory +
                ", currentBiome=" + currentBiome +
                ", surroundingEntities=" + surroundingEntities +
                '}';
    }
}
