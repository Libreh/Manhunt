package manhunt.mixin.game;

import manhunt.ManhuntMod;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonEntity.class)
public abstract class RunnersWinMixin extends MobEntity {
    protected RunnersWinMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void runnersWon(CallbackInfo ci) {
        if (this.getHealth() == 1f && ManhuntMod.gameState == GameState.PLAYING) {
            this.setHealth(0.0F);

            ManhuntGame.end(this.getServer(), false);
        }
    }
}