package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import manhunt.config.ManhuntConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import nota.Nota;
import nota.model.Song;
import nota.player.RadioSongPlayer;
import nota.utils.NBSDecoder;

import java.io.File;

import static manhunt.Manhunt.*;
import static manhunt.config.ManhuntConfig.musicDirectory;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class JukeboxCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("jukebox")
                .then(literal("play")
                        .then(argument("song", StringArgumentType.word()).suggests(songSuggestions())
                        .executes(context -> playSong(context.getSource(), StringArgumentType.getString(context, "song")))
                ))
                .then(literal("stop")
                        .executes(context -> stopPlaying(context.getSource()))
                )
                .then(literal("mute")
                        .executes(context -> muteMusic(context.getSource()))
                )
                .then(literal("unmute")
                        .executes(context -> unMuteMusic(context.getSource()))
                )
                .then(literal("lobbymusic")
                        .then(literal("status")
                                .executes(context -> lobbyMusicStatus(context.getSource()))
                        )
                        .then(literal("on")
                                .executes(context -> lobbyMusicOn(context.getSource()))
                        )
                        .then(literal("off")
                                .executes(context -> lobbyMusicOff(context.getSource()))
                        )
                )
        );
        dispatcher.register(literal("jukeboxall")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                .then(literal("play")
                        .then(argument("song", StringArgumentType.word()).suggests(songSuggestions())
                                .executes(context -> {
                                    var source = context.getSource();
                                    var songName = StringArgumentType.getString(context, "song");

                                    return playAllSong(source, songName);
                                })
                        ))
                .then(literal("stop")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .executes(context -> stopPlayingAll(context.getSource())
                        )
        ));
    }

    private static SuggestionProvider<ServerCommandSource> songSuggestions() {
        return (context, builder) -> CommandSource.suggestMatching(songs, builder);
    }

    private static int playSong(ServerCommandSource source, String songName) {
        ManhuntConfig.load();

        Song song = NBSDecoder.parse(new File(musicDirectory + "/" + songName + ".nbs"));

        RadioSongPlayer rsp = new RadioSongPlayer(song);

        var player = source.getPlayer();

        if (!getPlayerData(player).getBool("muteMusic")) {
            rsp.addPlayer(player);
            rsp.setPlaying(true);
        } else if (getPlayerData(player).getBool("muteMusic")) {
            player.sendMessage(Text.translatable("manhunt.jukebox.muted"));
        }

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.playing", songName), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopPlaying(ServerCommandSource source) {
        Nota.stopPlaying(source.getPlayer());

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.stopped"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int muteMusic(ServerCommandSource source) {
        Nota.stopPlaying(source.getPlayer());

        getPlayerData(source.getPlayer()).put("muteMusic", true);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.mute"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int unMuteMusic(ServerCommandSource source) {
        getPlayerData(source.getPlayer()).put("muteMusic", false);
        playLobbyMusic(source.getPlayer());

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.unmute"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int playAllSong(ServerCommandSource source, String songName) {
        ManhuntConfig.load();

        Song song = NBSDecoder.parse(new File(musicDirectory + "/" + songName + ".nbs"));

        RadioSongPlayer rsp = new RadioSongPlayer(song);

        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            if (!getPlayerData(player).getBool("muteMusic")) {
                rsp.addPlayer(player);
                rsp.setPlaying(true);
            } else if (getPlayerData(player).getBool("muteMusic")) {
                player.sendMessage(Text.translatable("manhunt.jukebox.muted"));
            }
        }

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.playing", songName), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopPlayingAll(ServerCommandSource source) {
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            Nota.stopPlaying(player);
        }

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.stopped"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int lobbyMusicStatus(ServerCommandSource source) {
        boolean bool = getPlayerData(source.getPlayer()).getBool("lobbyMusic");

        if (bool) {
            source.sendFeedback(() -> Text.translatable("manhunt.get.to", Text.literal("Lobby music"), Text.translatable("on")), false);
        } else if (!bool) {
            source.sendFeedback(() -> Text.translatable("manhunt.get.to", Text.literal("Lobby music"), Text.translatable("off")), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int lobbyMusicOn(ServerCommandSource source) {
        getPlayerData(source.getPlayer()).put("lobbyMusic", true);

        source.sendFeedback(() -> Text.translatable("manhunt.set.to", Text.literal("Lobby music"), Text.translatable("on")), false);
        playLobbyMusic(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }

    private static int lobbyMusicOff(ServerCommandSource source) {
        getPlayerData(source.getPlayer()).put("lobbyMusic", false);

        source.sendFeedback(() -> Text.translatable("manhunt.set.to", Text.literal("Lobby music"), Text.translatable("off")), false);
        Nota.stopPlaying(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }
}
