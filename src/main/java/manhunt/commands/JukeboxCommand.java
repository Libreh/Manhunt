package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.NbtCompound;
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
        Song song = NBSDecoder.parse(new File(musicDirectory + "/" + songName));

        RadioSongPlayer rsp = new RadioSongPlayer(song);

        var player = source.getPlayer();

        if (!muteMusic.get(player.getUuid())) {
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

        muteMusic.put(source.getPlayer().getUuid(), true);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.mute"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int unmuteMusic(ServerCommandSource source) {
        muteMusic.put(source.getPlayer().getUuid(), false);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.unmute"), false);

        if (!muteMusic.get(source.getPlayer().getUuid()) && !muteLobbyMusic.get(source.getPlayer().getUuid())) {
            playLobbyMusic(source.getPlayer());
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int playAllSong(ServerCommandSource source, String songName) {
        Song song = NBSDecoder.parse(new File(musicDirectory + "/" + songName + ".nbs"));

        RadioSongPlayer rsp = new RadioSongPlayer(song);

        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            if (!muteMusic.put(source.getPlayer().getUuid(), true)) {
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
        muteLobbyMusic.put(source.getPlayer().getUuid(), true);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.mutelobbymusic"), false);

        Nota.stopPlaying(source.getPlayer());

        return Command.SINGLE_SUCCESS;
    }

    private static int unmuteLobbyMusic(ServerCommandSource source) {
        muteLobbyMusic.put(source.getPlayer().getUuid(), false);

        source.sendFeedback(() -> Text.translatable("manhunt.jukebox.unmutelobbymusic"), false);

        if (!muteMusic.get(source.getPlayer().getUuid()) && !muteLobbyMusic.get(source.getPlayer().getUuid())) {
            playLobbyMusic(source.getPlayer());
        }

        return Command.SINGLE_SUCCESS;
    }
}
