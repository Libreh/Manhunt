package manhunt.mixin;

import com.mojang.authlib.GameProfile;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

                if (ManhuntGame.settings.whenRunnersDie) {
                    scoreboard.addPlayerToTeam(this.getName().getString(), scoreboard.getTeam("hunters"));
                } else if (!ManhuntGame.settings.whenRunnersDie) {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        if (player.getName().getString().equals(this.getName().getString())) {
                            ManhuntGame.updateGameMode(player);
                        }
                    }
                }

                if (ManhuntGame.settings.gameTitles && server.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                    ManhuntGame.gameState = ManhuntState.POSTGAME;
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        ManhuntGame.updateGameMode(player);
                        MessageUtil.showTitle(player, "manhunt.title.hunters", "manhunt.title.dead");
                        player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.2f, 1f);
                    }
                }
            }
        }
    }

}
