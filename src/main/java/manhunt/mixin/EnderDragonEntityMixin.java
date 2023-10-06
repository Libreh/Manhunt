package manhunt.mixin;

import manhunt.config.ManhuntConfig;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static manhunt.config.ManhuntConfig.revealWinner;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin {

    @Inject(at = @At(value = "INVOKE"), method = "kill")
    private void runnersWon(CallbackInfo ci) {
        ManhuntConfig.load();
        EnderDragonEntity dragon = ((EnderDragonEntity) (Object) this);
        MinecraftServer server = dragon.getServer();
        if (!revealWinner) {
            if (server.getScoreboard().getPlayerTeam("runners").getPlayerList().isEmpty() && dragon.deathTime == 1) {
                ManhuntGame.state = ManhuntState.POSTGAME;
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.runners")));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.dragon")));
                    player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.2f, 2f);
                }
            }
        }
    }
}
