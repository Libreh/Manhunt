package me.libreh.manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.libreh.manhunt.game.GameState;
import me.libreh.manhunt.world.ServerTaskExecutor;
import me.libreh.manhunt.world.ServerWorldController;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;
import static net.minecraft.server.command.CommandManager.literal;

public class StartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("start")
                .requires(
                        source -> isPreGame() &&
                                source.isExecutedByPlayer() && hasPermission(source.getPlayer(), "manhunt.command.start") ||
                                !source.isExecutedByPlayer())
                .executes(context -> checkStart(context.getSource())));
    }

    public static void executeStart() {
        ServerWorldController.taskExecutor = new ServerTaskExecutor(SERVER);

        changeState(GameState.PRELOADING);

        int preloadDistance = CONFIG.getPreloadDistance();
        List<Pair<Integer, Integer>> chunkOffsets = new ArrayList<>();
        for (int x = -preloadDistance; x <= preloadDistance; x++) {
            for (int z = -preloadDistance; z <= preloadDistance; z++) {
                if (isWithinDistance(x, z, preloadDistance)) {
                    chunkOffsets.add(new Pair<>(x, z));
                }
            }
        }

        var spawnPos = OVERWORLD.getSpawnPos();
        Set<Pair<Integer, Integer>> chunks = new HashSet<>();
        Pair<Integer, Integer> spawnpos = new Pair<>(spawnPos.getX(), spawnPos.getZ());
        for (Pair<Integer, Integer> offset : chunkOffsets) {
            chunks.add(new Pair<>(spawnpos.getLeft() + offset.getLeft(), spawnpos.getRight() + offset.getRight()));
        }

        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (Pair<Integer, Integer> chunk : chunks) {
            CompletableFuture<Chunk> future = ServerWorldController.getChunkAsync(OVERWORLD, chunk);
            futures.add(future);
        }

        chunkFutureList = futures;
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .whenCompleteAsync((result, throwable) -> {
                    canStart = true;
                },  ServerWorldController.taskExecutor);
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

        for (ServerPlayerEntity player : SERVER.getPlayerManager().getPlayerList()) {
            if (isRunner(player)) {
                runners++;
                break;
            }
        }

        if (runners != 0) {
            var saveProperties = SERVER.getSaveProperties();
            var worldProperties = saveProperties.getMainWorldProperties();
            var generatorOptions = saveProperties.getGeneratorOptions();

            MinecraftServer.setupSpawn(OVERWORLD, worldProperties, generatorOptions.hasBonusChest(), saveProperties.isDebugWorld());
            OVERWORLD.setSpawnPos(OVERWORLD.getSpawnPos(), 0.0F);

            if (CONFIG.getPreloadDistance() != 0) {
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
}
