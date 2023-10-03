package manhunt.mixin;

import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import nota.model.Playlist;
import nota.model.RepeatMode;
import nota.model.Song;
import nota.player.PositionSongPlayer;
import nota.utils.NBSDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Objects;

import static manhunt.Manhunt.getPlayerData;
import static manhunt.Manhunt.lobbyRegistryKey;
import static manhunt.config.ManhuntConfig.latePlayersJoinHunters;
import static manhunt.game.ManhuntState.PREGAME;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo info) {
        if (ManhuntGame.state == PREGAME) {
            player.getScoreboard().getPlayerScore(player.getName().getString(), player.getScoreboard().getNullableObjective("time")).setScore(0);
            player.teleport(player.getServer().getWorld(lobbyRegistryKey), 0.5, 63, 0, 0, 0);
            player.getInventory().clear();

            ManhuntGame.updateGameMode(player);
            player.addStatusEffect(
                    new StatusEffectInstance(
                            StatusEffects.SATURATION,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false,
                            false
                    )
            );
            player.addStatusEffect(
                    new StatusEffectInstance(
                            StatusEffects.RESISTANCE,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false,
                            false
                    )
            );
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5f, 0.5f);
        }

        if (ManhuntGame.state == ManhuntState.PLAYING) {
            getPlayerData(player).put("currentRole", "");

            Team hunters = player.getScoreboard().getTeam("hunters");
            Team runners = player.getScoreboard().getTeam("runners");

            if (!player.isTeamPlayer(hunters) && !player.isTeamPlayer(runners)) {
                if (latePlayersJoinHunters) {
                    player.getScoreboard().addPlayerToTeam(player.getName().getString(), hunters);
                } else {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }
}