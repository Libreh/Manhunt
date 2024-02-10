package manhunt.mixin;

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

    @Inject(method = "updatePostDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private void runnersWon(CallbackInfo ci) {
        try {
            EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
            MinecraftServer server = dragon.getServer();
            if (ManhuntGame.settings.gameTitles) {
                if (!server.getScoreboard().getTeam("runners").getPlayerList().isEmpty() && dragon.deathTime == 1) {
                    ManhuntGame.gameState = ManhuntState.POSTGAME;
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        MessageUtil.showTitle(player, "manhunt.title.runners", "manhunt.title.dragon");
                        player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.2f, 2f);
                    }
                }
            }
        } catch (NullPointerException ignored) {}
    }
}