package manhunt.world;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import manhunt.mixin.MinecraftServerAccessInterface;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.apache.commons.io.FileUtils;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.api.event.task.GenerationCompleteEvent;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.ManhuntMod.LOGGER;
import static manhunt.config.ManhuntConfig.AUTO_START;
import static manhunt.config.ManhuntConfig.WORLD_SEED;
import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.PREGAME;

// Thanks to https://github.com/sakurawald/fuji-fabric

public class ManhuntWorldModule {
    private static final String DEFAULT_MANHUNT_WORLD_NAMESPACE = "manhunt";
    private final String DEFAULT_OVERWORLD_PATH = "overworld";
    private final String DEFAULT_THE_NETHER_PATH = "the_nether";
    private final String DEFAULT_THE_END_PATH = "the_end";

    public void resetWorlds(MinecraftServer server) {
        server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.begin"), Text.translatable("manhunt.deleting")).formatted(Formatting.RED), false);
        WORLD_SEED.set(RandomSeed.getSeed());
        deleteWorld(server, DEFAULT_OVERWORLD_PATH);
        deleteWorld(server, DEFAULT_THE_NETHER_PATH);
        deleteWorld(server, DEFAULT_THE_END_PATH);
    }

    public void loadWorlds(MinecraftServer server) {
        long seed = Long.parseLong(WORLD_SEED.get());

        createWorld(server, DimensionTypes.OVERWORLD, DEFAULT_OVERWORLD_PATH, seed);
        createWorld(server, DimensionTypes.THE_NETHER, DEFAULT_THE_NETHER_PATH, seed);
        createWorld(server, DimensionTypes.THE_END, DEFAULT_THE_END_PATH, seed);
    }

    @SuppressWarnings("DataFlowIssue")
    private ChunkGenerator getChunkGenerator(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey) {
        if (dimensionTypeRegistryKey == DimensionTypes.OVERWORLD) {
            return server.getWorld(World.OVERWORLD).getChunkManager().getChunkGenerator();
        }
        if (dimensionTypeRegistryKey == DimensionTypes.THE_NETHER) {
            return server.getWorld(World.NETHER).getChunkManager().getChunkGenerator();
        }
        if (dimensionTypeRegistryKey == DimensionTypes.THE_END) {
            return server.getWorld(World.END).getChunkManager().getChunkGenerator();
        }
        return null;
    }

    private DimensionOptions createDimensionOptions(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey) {
        RegistryEntry<DimensionType> dimensionTypeRegistryEntry = getDimensionTypeRegistryEntry(server, dimensionTypeRegistryKey);
        ChunkGenerator chunkGenerator = getChunkGenerator(server, dimensionTypeRegistryKey);
        return new DimensionOptions(dimensionTypeRegistryEntry, chunkGenerator);
    }

    private RegistryEntry<DimensionType> getDimensionTypeRegistryEntry(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey) {
        return server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).getEntry(dimensionTypeRegistryKey).orElse(null);
    }

    private RegistryKey<DimensionType> getDimensionTypeRegistryKeyByPath(String path) {
        if (path.equals(DEFAULT_OVERWORLD_PATH)) return DimensionTypes.OVERWORLD;
        if (path.equals(DEFAULT_THE_NETHER_PATH)) return DimensionTypes.THE_NETHER;
        if (path.equals(DEFAULT_THE_END_PATH)) return DimensionTypes.THE_END;
        return null;
    }


    private SimpleRegistry<DimensionOptions> getDimensionOptionsRegistry(MinecraftServer server) {
        DynamicRegistryManager registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();
        return (SimpleRegistry<DimensionOptions>) registryManager.get(RegistryKeys.DIMENSION);
    }

    @SuppressWarnings("deprecation")
    private void createWorld(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey, String path, long seed) {
        ManhuntWorldProperties manhuntWorldProperties = new ManhuntWorldProperties(server.getSaveProperties(), seed);
        RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(DEFAULT_MANHUNT_WORLD_NAMESPACE, path));
        DimensionOptions dimensionOptions = createDimensionOptions(server, dimensionTypeRegistryKey);
        MinecraftServerAccessInterface serverAccessor = (MinecraftServerAccessInterface) server;
        ServerWorld world = new ManhuntWorld(server,
                Util.getMainWorkerExecutor(),
                serverAccessor.getSession(),
                manhuntWorldProperties,
                worldRegistryKey,
                dimensionOptions,
                VoidWorldGenerationProgressListener.INSTANCE,
                false,
                BiomeAccess.hashSeed(seed),
                ImmutableList.of(),
                true,
                null);

        if (dimensionTypeRegistryKey == DimensionTypes.THE_END) {
            world.setEnderDragonFight(new EnderDragonFight(world, world.getSeed(), EnderDragonFight.Data.DEFAULT));
        }

        ((DimensionOptionsInterface) (Object) dimensionOptions).manhunt$setSaveProperties(false);

        SimpleRegistry<DimensionOptions> dimensionsRegistry = getDimensionOptionsRegistry(server);
        boolean isFrozen = ((SimpleRegistryInterface<?>) dimensionsRegistry).manhunt$isFrozen();
        ((SimpleRegistryInterface<?>) dimensionsRegistry).manhunt$setFrozen(false);
        var dimensionOptionsRegistryKey = RegistryKey.of(RegistryKeys.DIMENSION, worldRegistryKey.getValue());
        if (!dimensionsRegistry.contains(dimensionOptionsRegistryKey)) {
            dimensionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, Lifecycle.stable());
        }
        ((SimpleRegistryInterface<?>) dimensionsRegistry).manhunt$setFrozen(isFrozen);

        serverAccessor.getWorlds().put(world.getRegistryKey(), world);
        ServerWorldEvents.LOAD.invoker().onWorldLoad(server, world);
        world.tick(() -> true);

        if (path.equals(DEFAULT_THE_END_PATH)) {
            server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.finished"), Text.translatable("manhunt.creating")).formatted(Formatting.GREEN), false);

            worldSpawnPos = setupSpawn(world);

            setHasPreloaded(false);

            if (!server.getPlayerManager().getPlayerList().isEmpty()) {
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.schedule(() -> startPreload(server), 2, TimeUnit.SECONDS);
            }
        }
    }

    private ServerWorld getManhuntWorldByPath(MinecraftServer server, String path) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(DEFAULT_MANHUNT_WORLD_NAMESPACE, path));
        return server.getWorld(worldKey);
    }


    private void deleteWorld(MinecraftServer server, String path) {
        ServerWorld world = getManhuntWorldByPath(server, path);
        if (world == null) return;

        ManhuntWorldManager.enqueueWorldDeletion(world);
    }

    public void onWorldUnload(MinecraftServer server, ServerWorld world) {
        if (server.isRunning()) {
            String namespace = world.getRegistryKey().getValue().getNamespace();
            String path = world.getRegistryKey().getValue().getPath();
            if (!namespace.equals(DEFAULT_MANHUNT_WORLD_NAMESPACE)) return;

            LOGGER.info("onWorldUnload() -> Creating world {} ...", path);
            long seed = Long.parseLong(WORLD_SEED.get());
            this.createWorld(server, this.getDimensionTypeRegistryKeyByPath(path), path, seed);
        }
    }

    private void startPreload(MinecraftServer server) {
        server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.begin"), Text.translatable("manhunt.preloading")), false);

        try {
            FileUtils.deleteDirectory(FabricLoader.getInstance().getConfigDir().resolve("chunky/tasks").toFile());
        } catch (IOException ignored) {
        }

        int radius = server.getPlayerManager().getViewDistance() * 8;

        ChunkyAPI chunky = ChunkyProvider.get().getApi();

        if (chunky.version() == 0) {
            chunky.startTask("manhunt:overworld", "square", worldSpawnPos.getX(), worldSpawnPos.getZ(), radius, radius, "concrentric");
            chunky.startTask("manhunt:the_nether", "square", (double) worldSpawnPos.getX() / 8, (double) worldSpawnPos.getZ() / 4, (double) radius / 4, (double) radius / 4, "concrentric");
            chunky.startTask("manhunt:the_end", "square", 0, 0, 64, 64, "concrentric");
            chunky.onGenerationComplete(event -> finishPreload(event, server));
        }
    }

    private void finishPreload(GenerationCompleteEvent event, MinecraftServer server) {
        LOGGER.info("Generation completed for " + event.world());

        if (event.world().equals("manhunt:overworld")) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                setPlayerSpawnXYZ(server.getWorld(overworldRegistryKey), player);
            }

            setHasPreloaded(true);

            server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.finished"), Text.translatable("manhunt.preloading")).formatted(Formatting.GREEN), false);

            if (gameState == PREGAME && Boolean.parseBoolean(AUTO_START.get())) {
                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.schedule(() -> startGame(server), 2, TimeUnit.SECONDS);
            }
        }
    }
}