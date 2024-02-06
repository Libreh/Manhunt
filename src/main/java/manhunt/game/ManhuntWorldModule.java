package manhunt.game;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import manhunt.Manhunt;
import manhunt.config.Configs;
import manhunt.mixin.MinecraftServerAccessInterface;
import manhunt.util.MessageUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;

// Thanks to https://github.com/sakurawald/fuji-fabric.

public class ManhuntWorldModule {
    private static final String DEFAULT_MANHUNT_WORLD_NAMESPACE = "manhunt";
    private final String DEFAULT_OVERWORLD_PATH = "overworld";
    private final String DEFAULT_THE_NETHER_PATH = "the_nether";
    private final String DEFAULT_THE_END_PATH = "the_end";

    public void resetWorlds(MinecraftServer server) {
        MessageUtil.sendBroadcast("manhunt.world.reset");
        Configs.configHandler.model().settings.worldSeed = RandomSeed.getSeed();
        Configs.configHandler.saveToDisk();
        deleteWorld(server, DEFAULT_OVERWORLD_PATH);
        deleteWorld(server, DEFAULT_THE_NETHER_PATH);
        deleteWorld(server, DEFAULT_THE_END_PATH);
    }

    public void loadWorlds(MinecraftServer server) {
        long seed = Configs.configHandler.model().settings.worldSeed;

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

        ((DimensionOptionsInterface) (Object) dimensionOptions).manhunt$saveProperties(false);

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
        MessageUtil.sendBroadcast("manhunt.world.created", path);
    }

    private ServerWorld getManhuntWorldByPath(MinecraftServer server, String path) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(DEFAULT_MANHUNT_WORLD_NAMESPACE, path));
        return server.getWorld(worldKey);
    }


    private void deleteWorld(MinecraftServer server, String path) {
        ServerWorld world = getManhuntWorldByPath(server, path);
        if (world == null) return;

        ManhuntWorldManager.enqueueWorldDeletion(world);
        MessageUtil.sendBroadcast("manhunt.world.deleted", path);
    }

    public void onWorldUnload(MinecraftServer server, ServerWorld world) {
        if (server.isRunning()) {
            String namespace = world.getRegistryKey().getValue().getNamespace();
            String path = world.getRegistryKey().getValue().getPath();
            if (!namespace.equals(DEFAULT_MANHUNT_WORLD_NAMESPACE)) return;

            Manhunt.LOGGER.info("onWorldUnload() -> Creating world {} ...", path);
            long seed = Configs.configHandler.model().settings.worldSeed;
            this.createWorld(server, this.getDimensionTypeRegistryKeyByPath(path), path, seed);
        }
    }
}