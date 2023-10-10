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
                        .executes(context -> unmuteMusic(context.getSource()))
                )
                .then(literal("mutelobbymusic")
                        .executes(context -> muteLobbyMusic(context.getSource()))
                )
                .then(literal("unmutelobbymusic")
                        .executes(context -> unmuteLobbyMusic(context.getSource()))
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

        Song song = NBSDecoder.parse(new File(musicDirectory + "/" + songName));

        RadioSongPlayer rsp = new RadioSongPlayer(song);

        var player = source.getPlayer();

        if (getPlayerScore(player, "muteMusic").getScore() == 0) {
            rsp.addPlayer(player);
            rsp.setPlaying(true);
        } else {
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

        getPlayerScore(source.getPlayer(), "muteMusic").setScore(1);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.mute"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int unmuteMusic(ServerCommandSource source) {
        getPlayerScore(source.getPlayer(), "muteMusic").setScore(0);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.unmute"), false);

        if (getPlayerScore(source.getPlayer(), "muteMusic").getScore() == 0 && getPlayerScore(source.getPlayer(), "muteLobbyMusic").getScore() == 0) {
            playLobbyMusic(source.getPlayer());
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int playAllSong(ServerCommandSource source, String songName) {
        ManhuntConfig.load();

        Song song = NBSDecoder.parse(new File(musicDirectory + "/" + songName + ".nbs"));

        RadioSongPlayer rsp = new RadioSongPlayer(song);

        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            if (getPlayerScore(player, "muteMusic").getScore() == 0) {
                rsp.addPlayer(player);
                rsp.setPlaying(true);
            } else {
                player.sendMessage(Text.translatable("manhunt.jukebox.muted"));
            }
        }

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.playing", Text.literal(songName)), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopPlayingAll(ServerCommandSource source) {
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            Nota.stopPlaying(player);
        }

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.stopped"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int muteLobbyMusic(ServerCommandSource source) {
        getPlayerScore(source.getPlayer(), "muteLobbyMusic").setScore(1);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.mutelobbymusic"), false);

        Nota.stopPlaying(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }

    private static int unmuteLobbyMusic(ServerCommandSource source) {
        getPlayerScore(source.getPlayer(), "muteLobbyMusic").setScore(0);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.unmutelobbymusic"), false);

        if (getPlayerScore(source.getPlayer(), "muteMusic").getScore() == 0 && getPlayerScore(source.getPlayer(), "muteLobbyMusic").getScore() == 0) {
            playLobbyMusic(source.getPlayer());
        }

        return Command.SINGLE_SUCCESS;
    }
}
