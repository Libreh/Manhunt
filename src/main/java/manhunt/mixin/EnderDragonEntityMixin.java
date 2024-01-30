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

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Inject(at = @At(value = "INVOKE"), method = "kill")
    private void runnersWon(CallbackInfo ci) {
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        MinecraftServer server = dragon.getServer();
        if (!ManhuntGame.settings.gameTitles) {
            if (server.getScoreboard().getTeam("runners").getPlayerList().isEmpty() && dragon.deathTime == 1) {
                ManhuntGame.gameState = ManhuntState.POSTGAME;
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    MessageUtil.showTitle(player, "manhunt.title.runners", "manhunt.title.dragon");
                    player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.2f, 2f);
                }
            }
        }
    }
}
