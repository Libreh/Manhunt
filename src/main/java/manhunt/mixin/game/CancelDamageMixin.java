package manhunt.mixin.game;

import manhunt.event.OnGameTick;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class CancelDamageMixin extends Entity {
    public CancelDamageMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (shouldCancelDamage()) {
            cir.cancel();
        }
    }

    @Unique
    private boolean shouldCancelDamage() {
        return OnGameTick.waitForRunner || OnGameTick.headStart && this.isTeamPlayer(this.getServer().getScoreboard().getTeam("hunters"));
    }
}