package manhunt;

import manhunt.command.*;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.api.DataStore;
import me.mrnavastar.sqlib.api.database.Database;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import org.apache.commons.io.FileUtils;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManhuntMod implements ModInitializer {
    public static final String MOD_ID = "manhunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
    public static final Database DATABASE = SQLib.getDatabase();
    public static final DataStore DATA_STORE = DATABASE.dataStore(MOD_ID, "playerdata");
    public static GameState gameState;
    public static StructurePlacementCalculator structurePlacementCalculator;
    public static final RegistryKey<World> LOBBY_REGISTRY_KEY = RegistryKey.of(RegistryKeys.WORLD,
            Identifier.of(MOD_ID, "lobby"));
    private static RuntimeWorldHandle overworldWorldHandle;
    private static RuntimeWorldHandle theNetherWorldHandle;
    private static RuntimeWorldHandle theEndWorldHandle;
    public static ServerWorld overworld;
    public static ServerWorld theNether;
    public static ServerWorld theEnd;
    private static boolean preloaded = false;


    @Override
    public void onInitialize() {
        ManhuntConfig.CONFIG.load();

        ManhuntGame.chunkyLoaded = (FabricLoader.getInstance().isModLoaded("chunky"));

        try {
            FileUtils.deleteDirectory(GAME_DIR.resolve("world").toFile());
        } catch (IOException e) {
            ManhuntMod.LOGGER.error("Failed to delete world files");
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CoordsCommand.register(dispatcher);
            DurationCommand.register(dispatcher);
            HunterCommand.register(dispatcher);
            ListCoordsCommand.register(dispatcher);
            MainRunnerCommand.register(dispatcher);
            OneHunterCommand.register(dispatcher);
            OneRunnerCommand.register(dispatcher);
            PauseCommand.register(dispatcher);
            PingCommand.register(dispatcher);
            PreferencesCommand.register(dispatcher);
            ResetCommand.register(dispatcher);
            RunnerCommand.register(dispatcher);
            SettingsComand.register(dispatcher);
            SpectatorCommand.register(dispatcher);
            StartCommand.register(dispatcher);
            TrackCommand.register(dispatcher);
            UnpauseCommand.register(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            loadManhuntWorlds(server, RandomSeed.getSeed());

            GameEvents.serverStart(server);
        });
        ServerTickEvents.START_SERVER_TICK.register(GameEvents::serverTick);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> GameEvents.playerRespawn(newPlayer));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> GameEvents.playerJoin(handler));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> GameEvents.playerLeave(handler));
        UseItemCallback.EVENT.register(GameEvents::useItem);
        UseBlockCallback.EVENT.register(GameEvents::useBlock);
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (ManhuntMod.gameState == GameState.PLAYING) {
                if (GameEvents.paused) {
                    return false;
                } else {
                    if (GameEvents.waitForRunner && !GameEvents.runnerHasStarted) {
                        return false;
                    } else
                        return !GameEvents.headStart || !player.isTeamPlayer(player.getScoreboard().getTeam("hunters"));
                }
            }

            return true;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (ManhuntMod.gameState == GameState.PLAYING) {
                if (GameEvents.paused) {
                    return ActionResult.FAIL;
                } else {
                    if (GameEvents.waitForRunner && !GameEvents.runnerHasStarted) {
                        return ActionResult.FAIL;
                    } else if (GameEvents.headStart && player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public static void loadManhuntWorlds(MinecraftServer server, long seed) {
        if (overworldWorldHandle != null) {
            overworldWorldHandle.delete();
            theNetherWorldHandle.delete();
            theEndWorldHandle.delete();
        }

        ManhuntGame.worldSpawnPos = new BlockPos(0, 0, 0);

        Fantasy fantasy = Fantasy.get(server);

        RuntimeWorldConfig overworldConfig =
                new RuntimeWorldConfig().setDimensionType(DimensionTypes.OVERWORLD).setGenerator(server.getOverworld().getChunkManager().getChunkGenerator()).setMirrorOverworldGameRules(true).setMirrorOverworldDifficulty(true).setShouldTickTime(true).setTimeOfDay(0).setSeed(seed);

        RuntimeWorldConfig theNetherConfig =
                new RuntimeWorldConfig().setDimensionType(DimensionTypes.THE_NETHER).setGenerator(server.getWorld(World.NETHER).getChunkManager().getChunkGenerator()).setMirrorOverworldGameRules(true).setMirrorOverworldDifficulty(true).setShouldTickTime(true).setTimeOfDay(0).setSeed(seed);

        RuntimeWorldConfig theEndConfig =
                new RuntimeWorldConfig().setDimensionType(DimensionTypes.THE_END).setGenerator(server.getWorld(World.END).getChunkManager().getChunkGenerator()).setMirrorOverworldGameRules(true).setMirrorOverworldDifficulty(true).setShouldTickTime(true).setTimeOfDay(0).setSeed(seed);

        structurePlacementCalculator =
                server.getOverworld().getChunkManager().getChunkGenerator().createStructurePlacementCalculator(server.getOverworld().getRegistryManager().getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), server.getOverworld().getChunkManager().getNoiseConfig(), seed);

        overworldWorldHandle = fantasy.openTemporaryWorld(overworldConfig);
        theNetherWorldHandle = fantasy.openTemporaryWorld(theNetherConfig);
        theEndWorldHandle = fantasy.openTemporaryWorld(theEndConfig);

        overworld = overworldWorldHandle.asWorld();
        theNether = theNetherWorldHandle.asWorld();
        theEnd = theEndWorldHandle.asWorld();

        theEnd.setEnderDragonFight(new EnderDragonFight(theEnd, theEnd.getSeed(), EnderDragonFight.Data.DEFAULT));

        if (ManhuntGame.chunkyLoaded && ManhuntConfig.CONFIG.isChunky()) {
            preloaded = true;

            schedulePreload(server);
        }

        for (ServerWorld world : server.getWorlds()) {
            if (world != null && !world.savingDisabled) {
                world.savingDisabled = true;
            }
        }
    }

    public static void schedulePreload(MinecraftServer server) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> startPreload(server), 1, TimeUnit.SECONDS);
    }

    private static void startPreload(MinecraftServer server) {
        ChunkyAPI chunky = ChunkyProvider.get().getApi();

        chunky.cancelTask(String.valueOf(ManhuntMod.overworld.getRegistryKey().getValue()));
        chunky.cancelTask(String.valueOf(ManhuntMod.theNether.getRegistryKey().getValue()));
        chunky.cancelTask(String.valueOf(ManhuntMod.theEnd.getRegistryKey().getValue()));

        try {
            FileUtils.deleteDirectory(GAME_DIR.resolve("config/chunky/tasks").toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to delete Chunky tasks");
        }

        if (ManhuntConfig.CONFIG.getOverworld() != 0) {
            chunky.startTask(String.valueOf(ManhuntMod.overworld.getRegistryKey().getValue()), "square",
                    ManhuntGame.worldSpawnPos.getX(), ManhuntGame.worldSpawnPos.getZ(),
                    ManhuntConfig.CONFIG.getOverworld(), ManhuntConfig.CONFIG.getOverworld(), "concentric");
        }
        if (ManhuntConfig.CONFIG.getTheNether() != 0) {
            chunky.startTask(String.valueOf(ManhuntMod.theNether.getRegistryKey().getValue()), "square",
                    ManhuntGame.worldSpawnPos.getX(), ManhuntGame.worldSpawnPos.getZ(),
                    ManhuntConfig.CONFIG.getTheNether(), ManhuntConfig.CONFIG.getTheNether(), "concentric");
        }
        if (ManhuntConfig.CONFIG.getTheEnd() != 0) {
            chunky.startTask(String.valueOf(ManhuntMod.theEnd.getRegistryKey().getValue()), "square",
                    ManhuntGame.worldSpawnPos.getX(), ManhuntGame.worldSpawnPos.getZ(),
                    ManhuntConfig.CONFIG.getTheEnd(), ManhuntConfig.CONFIG.getTheEnd(), "concentric");
        }

        chunky.onGenerationComplete(event -> {
            if (event.world().equals(String.valueOf(ManhuntMod.overworld.getRegistryKey().getValue())) && !preloaded) {
                preloaded = true;
            }
        });

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!GameEvents.PLAYER_SPAWN_POS.containsKey(player.getUuid())) {
                ManhuntGame.setPlayerSpawn(overworld, player);
            }
        }
    }

    public static boolean checkLeaderPermission(ServerPlayerEntity player, String key) {
        return Permissions.check(player, key) || Permissions.check(player, "manhunt.leader") || player.hasPermissionLevel(1) || player.hasPermissionLevel(2) || player.hasPermissionLevel(2) || player.hasPermissionLevel(4);
    }
}