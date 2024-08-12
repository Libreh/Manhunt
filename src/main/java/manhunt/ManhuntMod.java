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
	public static final Path gameDir = FabricLoader.getInstance().getGameDir();
	public static final Database database = SQLib.getDatabase();
	public static final DataStore datastore = database.dataStore(MOD_ID, "playerdata");
	public static GameState gameState;
	public static StructurePlacementCalculator structurePlacementCalculator;
	public static final RegistryKey<World> lobbyWorldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(MOD_ID, "lobby"));
	private static RuntimeWorldHandle overworldWorldHandle;
	private static RuntimeWorldHandle theNetherWorldHandle;
	private static RuntimeWorldHandle theEndWorldHandle;
	public static ServerWorld overworld;
	public static ServerWorld theNether;
	public static ServerWorld theEnd;
	private static boolean preloaded = false;

    @Override
	public void onInitialize() {
		ManhuntConfig.config.load();

        ManhuntGame.chunkyLoaded = (FabricLoader.getInstance().isModLoaded("chunky"));

		Path worldDirectory = gameDir.resolve("world");

		try {
			FileUtils.deleteDirectory(worldDirectory.resolve("advancements").toFile());
			FileUtils.deleteDirectory(worldDirectory.resolve("data").toFile());
			FileUtils.deleteDirectory(worldDirectory.resolve("DIM1").toFile());
			FileUtils.deleteDirectory(worldDirectory.resolve("DIM-1").toFile());
			FileUtils.deleteDirectory(worldDirectory.resolve("entities").toFile());
			FileUtils.deleteDirectory(worldDirectory.resolve("playerdata").toFile());
			FileUtils.deleteDirectory(worldDirectory.resolve("stats").toFile());
			FileUtils.delete(worldDirectory.resolve("session.lock").toFile());
		} catch (IOException e) {
			LOGGER.error("Failed to delete world files");
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CoordsCommand.register(dispatcher);
			DurationCommand.register(dispatcher);
			HunterCommand.register(dispatcher);
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
				}
				if (GameEvents.headStart && player.getScoreboardTeam() != null) {
					if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
						return false;
					} else {
                        return GameEvents.headStartCountdown;
					}
				}

			}
			return true;
		});
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (ManhuntMod.gameState == GameState.PLAYING) {
				if (GameEvents.paused) {
					return ActionResult.FAIL;
				}
				if (GameEvents.headStart) {
					if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
						return ActionResult.FAIL;
					} else {
						if (!GameEvents.headStartCountdown) {
							return ActionResult.FAIL;
						}
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

		RuntimeWorldConfig overworldConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.OVERWORLD)
				.setGenerator(server.getOverworld().getChunkManager().getChunkGenerator())
				.setMirrorOverworldGameRules(true)
				.setMirrorOverworldDifficulty(true)
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		RuntimeWorldConfig theNetherConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.THE_NETHER)
				.setGenerator(server.getWorld(World.NETHER).getChunkManager().getChunkGenerator())
				.setMirrorOverworldGameRules(true)
				.setMirrorOverworldDifficulty(true)
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		RuntimeWorldConfig theEndConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.THE_END)
				.setGenerator(server.getWorld(World.END).getChunkManager().getChunkGenerator())
				.setMirrorOverworldGameRules(true)
				.setMirrorOverworldDifficulty(true)
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		structurePlacementCalculator = server.getOverworld().getChunkManager().getChunkGenerator().createStructurePlacementCalculator(server.getOverworld().getRegistryManager().getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), server.getOverworld().getChunkManager().getNoiseConfig(), seed);

		overworldWorldHandle = fantasy.openTemporaryWorld(overworldConfig);
		theNetherWorldHandle = fantasy.openTemporaryWorld(theNetherConfig);
		theEndWorldHandle = fantasy.openTemporaryWorld(theEndConfig);

		overworld = overworldWorldHandle.asWorld();
		theNether = theNetherWorldHandle.asWorld();
		theEnd = theEndWorldHandle.asWorld();

		theEnd.setEnderDragonFight(new EnderDragonFight(theEnd, theEnd.getSeed(), EnderDragonFight.Data.DEFAULT));

		if (ManhuntGame.chunkyLoaded && ManhuntConfig.config.isChunky()) {
			preloaded = true;

			schedulePreload(server);
		}
	}

	public static void schedulePreload(MinecraftServer server) {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.schedule(() -> startPreload(server), 1, TimeUnit.SECONDS);
	}

	private static void startPreload(MinecraftServer server) {
		ChunkyAPI chunky = ChunkyProvider.get().getApi();

		chunky.cancelTask("manhunt:overworld");
		chunky.cancelTask("manhunt:the_nether");
		chunky.cancelTask("manhunt:the_end");

		try {
			FileUtils.deleteDirectory(gameDir.resolve("config/chunky/tasks").toFile());
		} catch (IOException e) {
			LOGGER.error("Failed to delete Chunky tasks");
		}

		if (ManhuntConfig.config.getOverworld() != 0) {
			chunky.startTask("manhunt:overworld", "square", 0, 0, ManhuntConfig.config.getOverworld(), ManhuntConfig.config.getOverworld(), "concentric");
		}
		if (ManhuntConfig.config.getTheNether() != 0) {
			chunky.startTask("manhunt:the_nether", "square", 0, 0, ManhuntConfig.config.getTheNether(), ManhuntConfig.config.getTheNether(), "concentric");
		}
		if (ManhuntConfig.config.getTheEnd() != 0) {
			chunky.startTask("manhunt:the_end", "square", 0, 0, ManhuntConfig.config.getTheEnd(), ManhuntConfig.config.getTheEnd(), "concentric");
		}

		chunky.onGenerationComplete(event -> {
			if (event.world().equals("manhunt:overworld") && !preloaded) {
				preloaded = true;
			}
		});

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (!GameEvents.playerSpawnPos.containsKey(player.getUuid())) {
				ManhuntGame.setPlayerSpawn(overworld, player);
			}
		}
	}

	public static boolean checkLeaderPermission(ServerPlayerEntity player, String key) {
		return Permissions.check(player, key) || Permissions.check(player, "manhunt.leader") || player.hasPermissionLevel(1) || player.hasPermissionLevel(2) ||player.hasPermissionLevel(2) ||player.hasPermissionLevel(4);
	}
}