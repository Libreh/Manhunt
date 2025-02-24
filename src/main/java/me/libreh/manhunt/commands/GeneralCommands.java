package me.libreh.manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.game.GameState;
import me.libreh.manhunt.world.ServerTaskExecutor;
import me.libreh.manhunt.world.ServerWorldController;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static me.libreh.manhunt.game.GameState.PREGAME;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GeneralCommands {
    public static long seed;
    public static String duration;

    public static void manhuntCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("manhunt")
                .then(literal("reload")
                        .requires(source -> requirePermissionOrOperator(source, "manhunt.reload"))
                        .executes(context -> {
                            Config.loadConfig();

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }

    public static void startCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("start")
                .requires(source -> isPreGame() && requirePermissionOrOperator(source, "manhunt.start"))
                .executes(context -> checkStart(context.getSource()))
        );
    }

    public static void executeStart() {
        ServerWorldController.taskExecutor = new ServerTaskExecutor(server);

        changeState(GameState.PRELOADING);

        int preloadDistance = Config.getConfig().gameOptions.preloadDistance;
        List<Pair<Integer, Integer>> chunkOffsets = new ArrayList<>();
        for (int x = -preloadDistance; x <= preloadDistance; x++) {
            for (int z = -preloadDistance; z <= preloadDistance; z++) {
                if (isWithinDistance(x, z, preloadDistance)) {
                    chunkOffsets.add(new Pair<>(x, z));
                }
            }
        }

        var spawnPos = overworld.getSpawnPos();
        Set<Pair<Integer, Integer>> chunks = new HashSet<>();
        Pair<Integer, Integer> spawnpos = new Pair<>(spawnPos.getX(), spawnPos.getZ());
        for (Pair<Integer, Integer> offset : chunkOffsets) {
            chunks.add(new Pair<>(spawnpos.getLeft() + offset.getLeft(), spawnpos.getRight() + offset.getRight()));
        }

        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (Pair<Integer, Integer> chunk : chunks) {
            CompletableFuture<Chunk> future = ServerWorldController.getChunkAsync(overworld, chunk);
            futures.add(future);
        }

        chunkFutureList = futures;
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .whenCompleteAsync((result, throwable) -> canStart = true,  ServerWorldController.taskExecutor);
    }

    private static boolean isWithinDistance(int dx, int dz, int distance) {
        int deltaX = Math.max(0, Math.abs(dx) - 1);
        int deltaZ = Math.max(0, Math.abs(dz) - 1);
        long max = Math.max((Math.max(deltaX, deltaZ) - 1), 0);
        long min = Math.min(deltaX, deltaZ);
        long distanceSquared = min * min + max * max;
        return distanceSquared < ((long) distance * distance);
    }

    private static int checkStart(ServerCommandSource source) {
        int runners = 0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (isRunner(player)) {
                runners++;
                break;
            }
        }

        if (runners != 0) {
            var saveProperties = server.getSaveProperties();
            var worldProperties = saveProperties.getMainWorldProperties();
            var generatorOptions = saveProperties.getGeneratorOptions();

            MinecraftServer.setupSpawn(overworld, worldProperties, generatorOptions.hasBonusChest(), saveProperties.isDebugWorld());
            overworld.setSpawnPos(overworld.getSpawnPos(), 0.0F);

            if (Config.getConfig().gameOptions.preloadDistance != 0) {
                executeStart();
            } else {
                canStart = true;
            }
        } else {
            source.sendFeedback(() -> Text.translatable("chat.manhunt.minimum",
                    Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void resetCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("reset")
                .requires(
                        source -> !isPreGame() &&
                                (gameState == GameState.POSTGAME && requirePermissionOrOperator(source, "manhunt.reset")) ||
                                isPlaying() && (Permissions.check(source, "manhunt.force_reset") || !source.isExecutedByPlayer())
                )
                .executes(context -> resetCommand(Random.create().nextLong()))
                .then(argument("seed", LongArgumentType.longArg())
                        .executes(context -> resetCommand(LongArgumentType.getLong(context,
                                "seed"))
                        )
                )
        );
    }

    private static int resetCommand(long seed) {
        GeneralCommands.seed = seed;

        changeState(GameState.PREGAME);

        return Command.SINGLE_SUCCESS;
    }

    public static void durationCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("duration")
                .requires(source -> !isPreGame() && Permissions.check(source, "manhunt.duration", true))
                .executes(context -> showDuration(context.getSource())));
    }

    private static int showDuration(ServerCommandSource source) {
        setDuration();

        source.sendFeedback(() -> Text.translatable("chat.manhunt.duration",
                Texts.bracketedCopyable(duration).formatted(Formatting.GREEN)), false);

        return Command.SINGLE_SUCCESS;
    }

    public static void setDuration() {
        if (gameState != PREGAME) {
            String hoursString;
            int hours = (int) Math.floor((double) overworld.getTime() % (20 * 60 * 60 * 24) / (20 * 60 * 60));

            if (hours <= 9) {
                hoursString = "0" + hours;
            } else {
                hoursString = String.valueOf(hours);
            }

            String minutesString;
            int minutes = (int) Math.floor((double) overworld.getTime() % (20 * 60 * 60) / (20 * 60));

            if (minutes <= 9) {
                minutesString = "0" + minutes;
            } else {
                minutesString = String.valueOf(minutes);
            }

            String secondsString;
            int seconds = (int) Math.floor((double) overworld.getTime() % (20 * 60) / (20));

            if (seconds <= 9) {
                secondsString = "0" + seconds;
            } else {
                secondsString = String.valueOf(seconds);
            }

            duration = hoursString + ":" + minutesString + ":" + secondsString;
        }
    }

    public static void feedCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("feed")
                .then(argument("targets", EntityArgumentType.players())
                        .requires(source -> !isPreGame() && requirePermissionOrOperator(source, "manhunt.feed"))
                        .executes(context -> {
                            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
                                player.setHealth(player.getMaxHealth());
                                resetPlayerHealth(player);
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}

