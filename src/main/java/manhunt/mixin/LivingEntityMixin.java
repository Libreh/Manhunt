package manhunt.mixin;

import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    float lastDamageTaken;

    @Inject(method = "damage", at = @At("RETURN"))
    void resetInvulnerabilityTicksWhenNoDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && lastDamageTaken <= 0) {
            timeUntilRegen = 0;
        }
    }
}
