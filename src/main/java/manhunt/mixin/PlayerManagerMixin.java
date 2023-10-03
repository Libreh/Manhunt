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
            var lobbyWorld = player.getServer().getWorld(lobbyRegistryKey);

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
            player.teleport(lobbyWorld, 0.5, 63, 0, 0, 0);
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5f, 0.5f);
            Song bohemianRhapsody = NBSDecoder.parse(new File("/home/libreh/songs/bohemian_rhapsody.nbs"));
            Song callMeMaybe = NBSDecoder.parse(new File("/home/libreh/songs/call_me_maybe.nbs"));
            Song dynamite = NBSDecoder.parse(new File("/home/libreh/songs/dynamite.nbs"));
            Song hesAPirate = NBSDecoder.parse(new File("/home/libreh/songs/hes_a_pirate.nbs"));
            Song heySoulSister = NBSDecoder.parse(new File("/home/libreh/songs/hey_soul_sister.nbs"));
            Song indianaJones = NBSDecoder.parse(new File("/home/libreh/songs/indiana_jones.nbs"));
            Song paradise = NBSDecoder.parse(new File("/home/libreh/songs/paradise.nbs"));
            Song smellsLikeTeenSpirit = NBSDecoder.parse(new File("/home/libreh/songs/smells_like_teen_spirit.nbs"));
            Song somebodyThatIUsedToKnow = NBSDecoder.parse(new File("/home/libreh/songs/somebody_that_i_used_to_know.nbs"));
            Song vivaLaVida = NBSDecoder.parse(new File("/home/libreh/songs/viva_la_vida.nbs"));
            Playlist playlist = new Playlist(bohemianRhapsody, callMeMaybe, dynamite, hesAPirate, heySoulSister, indianaJones, paradise, smellsLikeTeenSpirit, somebodyThatIUsedToKnow, vivaLaVida);
            Playlist tempPlaylist = new Playlist(dynamite, hesAPirate, vivaLaVida);
            PositionSongPlayer psp = new PositionSongPlayer(tempPlaylist, player.getWorld());
            psp.setBlockPos(new BlockPos(0, 63, 0));
            psp.setDistance(64);
            psp.addPlayer(player);
            //psp.setPlaying(true);
            psp.setRepeatMode(RepeatMode.ALL);
            psp.setAutoDestroy(true);
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