package baritone.plus.brain.utils;

import baritone.plus.main.BaritonePlus;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

@Getter
@Setter
public class WorldState {
    public final float playerHealth;
    public final int playerHunger;
    public final BlockPos playerLocation;
    public final InventoryPlayer playerInventory;

    public WorldState(BaritonePlus mod) {
        EntityPlayer player = mod.getPlayer();
        this.playerHealth = player.getHealth();
        this.playerHunger = player.getFoodStats().getFoodLevel();
        this.playerLocation = player.getPosition();
        this.playerInventory = player.inventory;
    }

    private String inventoryToString(InventoryPlayer inventory) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                str.append(itemStack.getCount())
                        .append("x ")
                        .append(itemStack.getDisplayName())
                        .append(", ");
            }
        }
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