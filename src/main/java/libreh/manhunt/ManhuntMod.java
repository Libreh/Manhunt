package libreh.manhunt;

import libreh.manhunt.command.game.*;
import libreh.manhunt.command.lobby.*;
import libreh.manhunt.command.role.*;
import libreh.manhunt.config.ManhuntConfig;
import libreh.manhunt.event.OnGameTick;
import libreh.manhunt.event.OnPlayerInteract;
import libreh.manhunt.event.OnPlayerState;
import libreh.manhunt.event.OnServerStart;
import libreh.manhunt.game.GameState;
import libreh.manhunt.game.ManhuntGame;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ManhuntMod implements ModInitializer {
    public static final String MOD_ID = "manhunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
    public static final Database DATABASE = SQLib.getDatabase();
    public static final DataStore SETTINGS = DATABASE.dataStore(MOD_ID, "settings");
    public static GameState gameState;
    public static StructurePlacementCalculator structurePlacementCalculator;
    public static final RegistryKey<World> LOBBY_REGISTRY_KEY = RegistryKey.of(RegistryKeys.WORLD,
            Identifier.of(MOD_ID, "lobby"));
    private static RuntimeWorldHandle overworldWorldHandle;
    private static RuntimeWorldHandle theNetherWorldHandle;
    private static RuntimeWorldHandle theEndWorldHandle;
    private static ServerWorld overworld;
    private static ServerWorld theNether;
    private static ServerWorld theEnd;

    public static ServerWorld getOverworld() {
        return overworld;
    }

    private static void setOverworld(ServerWorld overworld) {
        ManhuntMod.overworld = overworld;
    }

    public static ServerWorld getTheNether() {
        return theNether;
    }

    private static void setTheNether(ServerWorld theNether) {
        ManhuntMod.theNether = theNether;
    }

    public static ServerWorld getTheEnd() {
        return theEnd;
    }

    private static void setTheEnd(ServerWorld theEnd) {
        ManhuntMod.theEnd = theEnd;
    }

    @Override
    public void onInitialize() {
        ManhuntConfig.CONFIG.load();
        ManhuntGame.chunkyLoaded = (FabricLoader.getInstance().isModLoaded("chunky"));

        try {
            FileUtils.deleteDirectory(GAME_DIR.resolve("world").toFile());
        } catch (IOException e) {
            ManhuntMod.LOGGER.error("Failed to delete world files");
        }

        try {
            unzip("/manhunt/lobby_world.zip", GAME_DIR.resolve("world").toString());
        } catch (IOException e) {
            ManhuntMod.LOGGER.error("Failed to copy world files");
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
            SettingsGuiCommand.register(dispatcher);
            ResetCommand.register(dispatcher);
            RunnerCommand.register(dispatcher);
            ConfigGuiCommand.register(dispatcher);
            SpectatorCommand.register(dispatcher);
            StartCommand.register(dispatcher);
            TrackCommand.register(dispatcher);
            UnpauseCommand.register(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            loadManhuntWorlds(server, RandomSeed.getSeed());
            OnServerStart.onServerStart(server);
        });
        ServerTickEvents.START_SERVER_TICK.register(OnGameTick::onGameTick);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> OnPlayerState.playerRespawn(newPlayer));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> OnPlayerState.playerJoin(handler));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> OnPlayerState.playerLeave(handler));
        UseItemCallback.EVENT.register(OnPlayerInteract::useItem);
        UseBlockCallback.EVENT.register(OnPlayerInteract::useBlock);
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (ManhuntMod.gameState == GameState.PLAYING) {
                if (OnGameTick.paused) {
                    return false;
                } else {
                    if (OnGameTick.waitForRunner && !OnGameTick.runnerHasStarted) {
                        return false;
                    } else
                        return !OnGameTick.headStart || !player.isTeamPlayer(player.getScoreboard().getTeam("hunters"));
                }
            }

            return true;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (ManhuntMod.gameState == GameState.PLAYING) {
                if (OnGameTick.paused) {
                    return ActionResult.FAIL;
                } else {
                    if (OnGameTick.waitForRunner && !OnGameTick.runnerHasStarted) {
                        return ActionResult.FAIL;
                    } else if (OnGameTick.headStart && player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
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
                server.getOverworld().getChunkManager().getChunkGenerator().createStructurePlacementCalculator(server.getOverworld().getRegistryManager().getOrThrow(RegistryKeys.STRUCTURE_SET), server.getOverworld().getChunkManager().getNoiseConfig(), seed);

        overworldWorldHandle = fantasy.openTemporaryWorld(overworldConfig);
        theNetherWorldHandle = fantasy.openTemporaryWorld(theNetherConfig);
        theEndWorldHandle = fantasy.openTemporaryWorld(theEndConfig);

        setOverworld(overworldWorldHandle.asWorld());
        setTheNether(theNetherWorldHandle.asWorld());
        setTheEnd(theEndWorldHandle.asWorld());

        getTheEnd().setEnderDragonFight(new EnderDragonFight(getTheEnd(), getTheEnd().getSeed(),
                EnderDragonFight.Data.DEFAULT));

        if (ManhuntGame.chunkyLoaded && ManhuntConfig.CONFIG.isChunky()) {
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
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky cancel");
        server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky confirm");

        try {
            FileUtils.deleteDirectory(GAME_DIR.resolve("config/chunky/tasks").toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to delete Chunky tasks");
        }

        if (ManhuntConfig.CONFIG.getOverworld() != 0) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(),
                    "chunky start " + getOverworld().getRegistryKey().getValue() + " square " + ManhuntGame.worldSpawnPos.getX() + " " + ManhuntGame.worldSpawnPos.getZ() + " " + ManhuntConfig.CONFIG.getOverworld() + " " + ManhuntConfig.CONFIG.getOverworld());
        }
        if (ManhuntConfig.CONFIG.getTheNether() != 0) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(),
                    "chunky start " + getTheNether().getRegistryKey().getValue() + " square " + ManhuntGame.worldSpawnPos.getX() + " " + ManhuntGame.worldSpawnPos.getZ() + " " + ManhuntConfig.CONFIG.getTheNether() + " " + ManhuntConfig.CONFIG.getTheNether());
        }
        if (ManhuntConfig.CONFIG.getTheEnd() != 0) {
            server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(),
                    "chunky start " + getTheEnd().getRegistryKey().getValue() + " square " + ManhuntGame.worldSpawnPos.getX() + " " + ManhuntGame.worldSpawnPos.getZ() + " " + ManhuntConfig.CONFIG.getTheEnd() + " " + ManhuntConfig.CONFIG.getTheEnd());
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!OnGameTick.PLAYER_SPAWN_POS.containsKey(player.getUuid())) {
                ManhuntGame.setPlayerSpawn(getOverworld(), player);
            }
        }
    }

    public static boolean checkLeaderPermission(ServerPlayerEntity player, String key) {
        return Permissions.check(player, key) || Permissions.check(player, "manhunt.leader") || player.hasPermissionLevel(1) || player.hasPermissionLevel(2) || player.hasPermissionLevel(2) || player.hasPermissionLevel(4);
    }

    public static void unzip(String zipFile, String destFolder) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(ManhuntMod.class.getResourceAsStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destFolder + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
        }
    }
}