package manhunt.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Shadow
    float lastDamageTaken;
    private LivingEntity entity = (LivingEntity) (Object) this;

    @Inject(method = "damage", at = @At("RETURN"))
    void manhunt$resetInvulnerabilityTicksWhenNoDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && lastDamageTaken <= 0) {
            entity.timeUntilRegen = 0;
        }
    }
}
