package me.libreh.manhunt.world;

import me.libreh.manhunt.Manhunt;
import me.libreh.manhunt.mixin.world.LevelPropertiesAccessor;
import me.libreh.manhunt.mixin.world.ServerChunkLoadingManagerAccessor;
import me.libreh.manhunt.mixin.world.ServerChunkManagerAccessor;
import me.libreh.manhunt.utils.Constants;
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
import static me.libreh.manhunt.utils.Methods.unzip;

public class ServerWorldController {
    public static Executor taskExecutor;

    private static final ChunkTicketType<Unit> TICKET_ASYNC = ChunkTicketType.create(MOD_ID + "-async", (a, b) -> 0);

    public static void resetWorlds(long seed) {
        SERVER.saveAll(false, true, true);

        SERVER.saving = true;
        try {
            SERVER.cancelTasks();
            for (World world : SERVER.getWorlds()) {
                world.close();
                String[] directories = {"region", "poi", "entities"};
                for (String dir : directories) {
                    File file = SERVER.session.getWorldDirectory(world.getRegistryKey()).resolve(dir).toFile();
                    if (file.exists()) {
                        deleteRecursively(file);
                    }
                }
            }

            LevelPropertiesAccessor levelPropertiesAccessor = (LevelPropertiesAccessor) SERVER.getSaveProperties();
            levelPropertiesAccessor.setGeneratorOptions(levelPropertiesAccessor.getGeneratorOptions().withSeed(OptionalLong.of(seed)));
            SERVER.loadWorld();
        } catch (IOException e) {
            Manhunt.LOGGER.info("Failed to reset", e);
        } finally {
            SERVER.saving = false;
        }

        unzip();

        OVERWORLD = SERVER.getWorld(World.OVERWORLD);
        THE_NETHER = SERVER.getWorld(World.NETHER);
        THE_END = SERVER.getWorld(World.END);
        LOBBY = SERVER.getWorld(Constants.LOBBY_REGISTRY_KEY);
    }

    public static CompletableFuture<Chunk> getChunkAsync(ServerWorld world, Pair<Integer, Integer> chunk) {
        if (!SERVER.isOnThread()) {
            return CompletableFuture.supplyAsync(() -> getChunkAsync(world, chunk), SERVER).thenCompose(chunkFuture -> chunkFuture);
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
