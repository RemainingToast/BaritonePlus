package baritone.plus.brain.utils;

import baritone.plus.main.BaritonePlus;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.BlockPos;

@Getter
@Setter
public class WorldState {
    public final float playerHealth;
    public final int playerHunger;
    public final BlockPos playerLocation;
    public final PlayerInventory playerInventory;

    public WorldState(BaritonePlus mod) {
        var player = mod.getPlayer();
        var world = mod.getWorld();

        this.playerHealth = player.getHealth();
        this.playerHunger = player.getHungerManager().getFoodLevel();
        this.playerLocation = player.getBlockPos();
        this.playerInventory = player.getInventory();
    }

    private String inventoryToString(PlayerInventory inventory) {
        var str = new StringBuilder();
        var main = inventory.main;

        main.forEach(itemStack -> {
            if (itemStack == null || itemStack.isEmpty()) {
                return;
            }

            str.append(itemStack.getCount())
                    .append("x ")
                    .append(itemStack.getItem().getName().getString())
                    .append(", ");
        });

        return str.toString();
    }

    @Override
    public String toString() {
        return "{" +
                "health=" + playerHealth +
                ", hunger=" + playerHunger +
                ", location=" + playerLocation +
                ", inventory=" + inventoryToString(playerInventory) +
                "}";
    }
}
