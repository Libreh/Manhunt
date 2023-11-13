package manhunt.mixin;

import com.mojang.authlib.GameProfile;
import manhunt.GameState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static manhunt.Config.showGameTitles;
import static manhunt.Manhunt.gameState;
import static manhunt.Manhunt.updateGameMode;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Final
    @Shadow
    public MinecraftServer server;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource source, CallbackInfo ci) {
        Scoreboard scoreboard = server.getScoreboard();

        if (this.getScoreboardTeam() != null) {
            if (this.getScoreboardTeam().isEqual(scoreboard.getTeam("runners"))) {

                scoreboard.clearPlayerTeam(this.getName().getString());

                if (showGameTitles && server.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                    gameState = GameState.POSTGAME;
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        updateGameMode(player);
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.hunters")));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.dead")));
                        player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.2f, 1f);
                    }
                }
            }
        }
    }

}
