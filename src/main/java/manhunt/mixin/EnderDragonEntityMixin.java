package manhunt.mixin;

import manhunt.config.Configs;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Thanks to https://github.com/Ivan-Khar/manhunt-fabricated.

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void runnersWon(CallbackInfo ci) {
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        if (dragon.getHealth() == 1.0F) {
            if (ManhuntGame.settings.gameTitles) {
                Configs.configHandler.model().settings.gameTitles = false;
                Configs.configHandler.saveToDisk();
                ManhuntGame.manhuntState(ManhuntState.POSTGAME, dragon.getServer());
                MinecraftServer server = dragon.getServer();
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    MessageUtil.showTitle(player, "manhunt.title.runners", "manhunt.title.dragon");
                    player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.2f, 2f);
                }
            }
        }
    }

    @Inject(method = "updatePostDeath", at = @At("TAIL"))
    private void setGameTitles(CallbackInfo ci) {
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        if (dragon.ticksSinceDeath == 1) {
            Configs.configHandler.model().settings.gameTitles = true;
            Configs.configHandler.saveToDisk();
        }
    }
}