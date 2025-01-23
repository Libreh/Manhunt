package me.libreh.manhunt.mixin.game;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.libreh.manhunt.utils.Fields.headStartTicks;
import static me.libreh.manhunt.utils.Fields.huntersTeam;

@Mixin(LivingEntity.class)
public abstract class CancelDamageMixin extends Entity {
    public CancelDamageMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void cancelDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (shouldCancelDamage()) {
            cir.cancel();
        }
    }

    @Unique
    private boolean shouldCancelDamage() {
        return headStartTicks != 0 && this.isTeamPlayer(huntersTeam);
    }
}