package baritone.plus.launch.mixins;

import baritone.plus.api.event.EventBus;
import baritone.plus.api.event.events.PlayerCollidedWithEntityEvent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class PlayerCollidesWithEntityMixin {

    // Determines a collision between items/EXP orbs/other objects within "pickup" range.
    @Redirect(
            method = "collideWithEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onPlayerCollision(Lnet/minecraft/entity/player/PlayerEntity;)V")
    )
    private void onCollideWithEntity(Entity self, PlayerEntity player) {
        // TODO: Less hard-coded manual means of enforcing client side access
        if (player instanceof ClientPlayerEntity) {
            EventBus.publish(new PlayerCollidedWithEntityEvent(player, self));
        }
        // Perform the default action.
        // TODO: Figure out a cleaner way. First re-read the mixin intro documentation again.
        self.onPlayerCollision(player);
    }
}
