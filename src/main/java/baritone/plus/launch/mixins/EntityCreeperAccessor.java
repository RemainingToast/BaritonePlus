package baritone.plus.launch.mixins;

import net.minecraft.entity.monster.EntityCreeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityCreeper.class)
public interface EntityCreeperAccessor {
    @Accessor("fuseTime")
    int fuseTime();
}
