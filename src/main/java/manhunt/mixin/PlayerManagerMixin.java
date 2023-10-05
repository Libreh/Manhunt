package manhunt.mixin;

import manhunt.config.ManhuntConfig;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import nota.model.Playlist;
import nota.model.Song;
import nota.player.RadioSongPlayer;
import nota.utils.NBSDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

import static manhunt.Manhunt.*;
import static manhunt.config.ManhuntConfig.latePlayersJoinHunters;
import static manhunt.config.ManhuntConfig.musicDirectory;
import static manhunt.game.ManhuntState.PREGAME;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo info) {
        if (ManhuntGame.state == PREGAME) {
            ManhuntConfig.load();

            player.getScoreboard().getPlayerScore(player.getName().getString(), getTimeObjective(player.getServer())).setScore(0);
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

            var world = player.getWorld();

            if (world.getBlockState(new BlockPos(2, 60, 24)).getBlock().equals(Blocks.AIR)) {
                world.getServer().getCommandManager().executeWithPrefix(world.getServer().getCommandSource().withSilent(), "setblock 2 60 24 ice");
                world.getServer().getCommandManager().executeWithPrefix(world.getServer().getCommandSource().withSilent(), "summon glow_squid 2 60 27 {NoGravity:1b,Silent:1b,Invulnerable:1b,NoAI:1b}");
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

        if (getPlayerData(player).getString("pingSound") == null) {
            getPlayerData(player).put("muteMusic", false);
            getPlayerData(player).put("lobbyMusic", true);
            getPlayerData(player).put("doNotDisturb", false);
            getPlayerData(player).put("pingSound", "");
            getPlayerData(player).put("currentRole", "hunter");
            playLobbyMusic(player);
        } else if (getPlayerData(player).getString("pingSound") != null) {
            if (!getPlayerData(player).getBool("muteMusic") && getPlayerData(player).getBool("lobbyMusic")) {
                playLobbyMusic(player);
            }
        }
    }

    private void playLobbyMusic(ServerPlayerEntity player) {
        Song elevatorMusic = NBSDecoder.parse(new File(musicDirectory + "/" + "elevatorMusic.nbs"));
        Song localForecast = NBSDecoder.parse(new File(musicDirectory + "/" + "localForecast.nbs"));
        Song soChill = NBSDecoder.parse(new File(musicDirectory + "/" + "soChill.nbs"));
        Playlist lobbyMusic = new Playlist(soChill, localForecast, elevatorMusic);
        RadioSongPlayer rsp = new RadioSongPlayer(lobbyMusic);
        rsp.addPlayer(player);
        rsp.setPlaying(true);
        player.sendMessage(Text.translatable("manhunt.jukebox.playing", Text.translatable(rsp.getSong().getPath().getAbsoluteFile().getName())));
        player.sendMessage(Text.translatable("manhunt.jukebox.cancel"));
        player.sendMessage(Text.translatable("manhunt.jukebox.permanent"));
        player.sendMessage(Text.translatable("manhunt.lobbymusic.disable"));
        player.sendMessage(Text.translatable("manhunt.jukebox.volume"));
    }
}