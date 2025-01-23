package me.libreh.manhunt.world;

import me.libreh.manhunt.Manhunt;
import me.libreh.manhunt.mixin.world.LevelPropertiesAccessor;
import me.libreh.manhunt.mixin.world.ServerChunkLoadingManagerAccessor;
import me.libreh.manhunt.mixin.world.ServerChunkManagerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.*;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.io.File;
import java.io.IOException;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static me.libreh.manhunt.Manhunt.MOD_ID;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.unzipLobbyWorld;

public class ServerWorldController {
    public static Executor taskExecutor;

    private static final ChunkTicketType<Unit> TICKET_ASYNC = ChunkTicketType.create(MOD_ID + "-async", (a, b) -> 0);

    private static void tickKeepAlive(MinecraftServer server) {
        if (server.getNetworkIo() != null) {
            server.getNetworkIo().tick();
        }
    }

    public static void resetWorlds(long seed) {
        tickKeepAlive(server);

        server.saving = true;
        try {
            server.getPlayerManager().saveAllPlayerData();
            for (ServerWorld world : server.getWorlds()) {
                world.getPersistentStateManager().save();
            }

            tickKeepAlive(server);
            server.cancelTasks();
            shouldCancelSaving = true;

            for (World world : server.getWorlds()) {
                world.close();
                tickKeepAlive(server);
                String[] directories = {"region", "poi", "entities"};
                for (String dir : directories) {
                    File file = server.session.getWorldDirectory(world.getRegistryKey()).resolve(dir).toFile();
                    if (file.exists()) {
                        deleteRecursively(file);
                    }
                }
                tickKeepAlive(server);
            }

            LevelPropertiesAccessor levelPropertiesAccessor = (LevelPropertiesAccessor) server.getSaveProperties();
            levelPropertiesAccessor.setGeneratorOptions(levelPropertiesAccessor.getGeneratorOptions().withSeed(OptionalLong.of(seed)));
            server.loadWorld();
            tickKeepAlive(server);
        } catch (IOException e) {
            Manhunt.LOGGER.info("Failed to reset", e);
        } finally {
            server.saving = false;
            shouldCancelSaving = false;
        }

        unzipLobbyWorld();

        overworld = server.getWorld(World.OVERWORLD);
        theNether = server.getWorld(World.NETHER);
        theEnd = server.getWorld(World.END);
    }

    public static CompletableFuture<Chunk> getChunkAsync(ServerWorld world, Pair<Integer, Integer> chunk) {
        if (!server.isOnThread()) {
            return CompletableFuture.supplyAsync(() -> getChunkAsync(world, chunk), server).thenCompose(chunkFuture -> chunkFuture);
        }

        ServerChunkManager chunkManager = world.getChunkManager();
        chunkManager.addTicket(TICKET_ASYNC, new ChunkPos(chunk.getLeft(), chunk.getRight()), 0, Unit.INSTANCE);

        ((ServerChunkManagerAccessor) chunkManager).invokeUpdateChunks();

        ServerChunkLoadingManager chunkLoadingManager = chunkManager.chunkLoadingManager;
        ChunkHolder chunkHolder = ((ServerChunkLoadingManagerAccessor) chunkLoadingManager).invokeGetChunkHolder(new ChunkPos(chunk.getLeft(), chunk.getRight()).toLong());

        CompletableFuture<Chunk> chunkFuture = (chunkHolder != null
                ? chunkHolder.load(ChunkStatus.FULL, chunkLoadingManager)
                .thenApply(optionalChunk -> optionalChunk.orElse(null))
                : CompletableFuture.completedFuture(null));

        chunkFuture.whenCompleteAsync((c, e) ->
                chunkManager.removeTicket(
                        TICKET_ASYNC,
                        new ChunkPos(chunk.getLeft(), chunk.getRight()),
                        0,
                        Unit.INSTANCE
                ), taskExecutor
        );

        return chunkFuture;
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteRecursively(subFile);
            }
        }

        file.delete();
    }
}
