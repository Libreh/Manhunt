package manhunt.mixin;

import manhunt.config.ManhuntConfig;
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
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import nota.model.Song;
import nota.player.RadioSongPlayer;
import nota.utils.NBSDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

import static manhunt.Manhunt.getPlayerData;
import static manhunt.Manhunt.lobbyRegistryKey;
import static manhunt.config.ManhuntConfig.latePlayersJoinHunters;
import static manhunt.config.ManhuntConfig.musicDirectory;
import static manhunt.game.ManhuntState.PREGAME;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo info) {
        if (ManhuntGame.state == PREGAME) {
            ManhuntConfig.load();

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

            if (getPlayerData(player).getString("muteMusic") == null) {
                getPlayerData(player).put("muteMusic", "false");
                if (getPlayerData(player).getString("lobbyMusic") == null) {
                    getPlayerData(player).put("lobbyMusic", "true");
                    lobbyMusic(player);
                } else if (getPlayerData(player).getString("lobbyMusic") != null) {
                    if (getPlayerData(player).getString("lobbyMusic").equals(true)) {
                        lobbyMusic(player);
                    }
                }
            } else if (getPlayerData(player).getString("muteMusic") != null) {
                if (getPlayerData(player).getString("muteMusic").equals(false)) {
                    if (getPlayerData(player).getString("lobbyMusic") == null) {
                        getPlayerData(player).put("lobbyMusic", "true");
                        lobbyMusic(player);
                    } else if (getPlayerData(player).getString("lobbyMusic") != null) {
                        if (getPlayerData(player).getString("lobbyMusic").equals(true)) {
                            lobbyMusic(player);
                        }
                    }
                }
            }
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

    private void lobbyMusic(ServerPlayerEntity player) {
        Song song = NBSDecoder.parse(new File(musicDirectory + "/" + "soChill.nbs"));
        RadioSongPlayer rsp = new RadioSongPlayer(song);
        rsp.addPlayer(player);
        rsp.setPlaying(true);
        player.sendMessage(Text.translatable("manhunt.jukebox.playing", "soChill.nbs"));
        player.sendMessage(Text.translatable("manhunt.jukebox.cancel"));
        player.sendMessage(Text.translatable("manhunt.jukebox.mute"));
    }
}