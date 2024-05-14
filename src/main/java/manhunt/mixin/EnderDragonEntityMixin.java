package manhunt.mixin;

import manhunt.game.ManhuntGame;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void runnersWon(CallbackInfo ci) {
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        if (dragon.getHealth() == 1) {
            MinecraftServer server = dragon.getServer();

            dragon.setHealth(0);

            ManhuntGame.endGame(server, false, false);
        }
    }
}