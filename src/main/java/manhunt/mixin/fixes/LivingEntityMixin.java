package manhunt.mixin.fixes;

import manhunt.game.GameEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected float lastDamageTaken;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void resetInvulnerabilityTicksWhenNoDamage(
            DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ() && lastDamageTaken <= 0) {
            this.timeUntilRegen = 0;
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelHeadStartHunterDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (checkHeadStart()) {
            cir.cancel();
        }
    }

    @Unique
    private boolean checkHeadStart() {
        return GameEvents.headStart && this.isTeamPlayer(this.getServer().getScoreboard().getTeam("hunters"));
    }
}