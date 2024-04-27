package manhunt.world;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import manhunt.ManhuntMod;
import manhunt.mixin.MinecraftServerAccessInterface;
import manhunt.world.interfaces.DimensionOptionsInterface;
import manhunt.world.interfaces.SimpleRegistryInterface;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
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

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.ManhuntMod.*;

public class ManhuntWorldModule {
    public void resetWorlds(MinecraftServer server) {
        server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.start"), Text.translatable("manhunt.deleting")).formatted(Formatting.RED), false);
        config.setWorldSeed(RandomSeed.getSeed());
        config.save();
        deleteWorld(server, OVERWORLD);
        deleteWorld(server, THE_NETHER);
        deleteWorld(server, THE_END);
    }

    public void loadWorlds(MinecraftServer server) {
        long seed = config.getWorldSeed();

        createWorld(server, DimensionTypes.OVERWORLD, OVERWORLD, seed);
        createWorld(server, DimensionTypes.THE_NETHER, THE_NETHER, seed);
        createWorld(server, DimensionTypes.THE_END, THE_END, seed);
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
        return switch (path) {
            case OVERWORLD -> DimensionTypes.OVERWORLD;
            case THE_NETHER -> DimensionTypes.THE_NETHER;
            case THE_END -> DimensionTypes.THE_END;
            default -> null;
        };
    }


    private SimpleRegistry<DimensionOptions> getDimensionOptionsRegistry(MinecraftServer server) {
        DynamicRegistryManager registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();
        return (SimpleRegistry<DimensionOptions>) registryManager.get(RegistryKeys.DIMENSION);
    }

    @SuppressWarnings("deprecation")
    private void createWorld(MinecraftServer server, RegistryKey<DimensionType> dimensionTypeRegistryKey, String path, long seed) {
        ManhuntWorldProperties manhuntWorldProperties = new ManhuntWorldProperties(server.getSaveProperties(), seed);
        RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, path));
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

        if (path.equals(THE_END)) {
            server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.done"), Text.translatable("manhunt.creating")).formatted(Formatting.GOLD), false);
            setWorldSpawnPos(new BlockPos(0, 0, 0));

            if (isChunkyIntegration()) {
                ManhuntMod.setPreloaded(false);

                ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                scheduledExecutorService.schedule(() -> startPreload(server), 500, TimeUnit.MILLISECONDS);
            }
        }
    }

    private ServerWorld getManhuntWorldByPath(MinecraftServer server, String path) {
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, path));
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
            if (!namespace.equals(MOD_ID)) return;

            long seed = config.getWorldSeed();
            this.createWorld(server, this.getDimensionTypeRegistryKeyByPath(path), path, seed);
        }
    }

    private void startPreload(MinecraftServer server) {
        server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.begin"), Text.translatable("manhunt.preloading")).formatted(Formatting.YELLOW), false);

        ChunkyAPI chunky = ChunkyProvider.get().getApi();

        chunky.cancelTask("manhunt:overworld");
        chunky.cancelTask("manhunt:the_nether");

        try {
            FileUtils.deleteDirectory(getGameDir().resolve("config/chunky/tasks").toFile());
        } catch (IOException ignored) {}

        chunky.startTask("manhunt:overworld", "square", 0, 0, 8000, 8000, "concentric");
        chunky.startTask("manhunt:the_nether", "square", 0, 0, 1000, 1000, "concentric");

        chunky.onGenerationComplete(event -> {
            if (event.world().equals("manhunt:overworld") && !isPreloaded()) {
                setPreloaded(true);

                server.getPlayerManager().broadcast(Text.translatable("manhunt.world", Text.translatable("manhunt.finished"), Text.translatable("manhunt.preloading")).formatted(Formatting.GREEN), false);
            }
        });
    }
}