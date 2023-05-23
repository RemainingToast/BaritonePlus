package baritone.plus.api.event.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerCollidedWithEntityEvent {
    public EntityPlayer player;
    public Entity other;

    public PlayerCollidedWithEntityEvent(EntityPlayer player, Entity other) {
        this.player = player;
        this.other = other;
    }
}
