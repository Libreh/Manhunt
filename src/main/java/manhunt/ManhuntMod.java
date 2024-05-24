package manhunt;

import manhunt.command.*;
import manhunt.config.ManhuntConfig;
import manhunt.game.Events;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.Table;
import me.mrnavastar.sqlib.database.Database;
import me.mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManhuntMod implements ModInitializer {
	public static final String MOD_ID = "manhunt";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static final ManhuntConfig config = ManhuntConfig.INSTANCE;
	private static final Path gameDir = FabricLoader.getInstance().getGameDir();
	private static GameState gameState;
	private static final Database database = SQLib.getDatabase();
	private static Table table = database.createTable(MOD_ID, "playerdata").addColumn("game_titles", SQLDataType.BOOL).addColumn("manhunt_sounds", SQLDataType.BOOL).addColumn("night_vision", SQLDataType.BOOL).addColumn("friendly_fire", SQLDataType.BOOL).finish();
	public static final Identifier lobbyKey = new Identifier(MOD_ID, "lobby");
	public static RuntimeWorldHandle overworldHandle;
	public static RuntimeWorldHandle netherHandle;
	public static RuntimeWorldHandle endHandle;
	public static ServerWorld overworld;
	public static ServerWorld nether;
	public static ServerWorld end;
	public static List<ServerPlayerEntity> allRunners;
	private static BlockPos worldSpawnPos;
	private static BlockPos overworldSpawn;
	private static boolean preloaded = false;
	private static boolean chunkyIntegration = false;
	private static boolean paused = false;
	public static final List<MutableText> hunterCoords = new ArrayList<>();
	public static final List<MutableText> runnerCoords = new ArrayList<>();
	public static final HashMap<UUID, Boolean> hasPlayed = new HashMap<>();
	public static final HashMap<UUID, BlockPos> playerSpawn = new HashMap<>();
	public static final HashMap<UUID, Boolean> gameTitles = new HashMap<>();
	public static final HashMap<UUID, Boolean> manhuntSounds = new HashMap<>();
	public static final HashMap<UUID, Boolean> nightVision = new HashMap<>();
	public static final HashMap<UUID, Boolean> friendlyFire = new HashMap<>();
	public static final HashMap<ServerPlayerEntity, Collection<StatusEffectInstance>> playerEffects = new HashMap<>();
	public static HashMap<UUID, Integer> parkourTimer = new HashMap<>();
	public static HashMap<UUID, Boolean> startedParkour = new HashMap<>();
	public static HashMap<UUID, Boolean> finishedParkour = new HashMap<>();

	public static Path getGameDir() {
		return gameDir;
	}

	public static GameState getGameState() {
		return gameState;
	}

	public static void setGameState(GameState gameState) {
		ManhuntMod.gameState = gameState;
	}

	public static Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		ManhuntMod.table = table;
	}

	public static BlockPos getWorldSpawnPos() {
		return worldSpawnPos;
	}

	public static void setWorldSpawnPos(BlockPos worldSpawnPos) {
		ManhuntMod.worldSpawnPos = worldSpawnPos;
	}

	public static BlockPos getOverworldSpawn() {
		return overworldSpawn;
	}

	public static void setOverworldSpawn(BlockPos overworldSpawn) {
		ManhuntMod.overworldSpawn = overworldSpawn;
	}

	public static boolean isPreloaded() {
		return preloaded;
	}

	public static void setPreloaded(boolean preloaded) {
		ManhuntMod.preloaded = preloaded;
	}

	public static boolean isChunkyIntegration() {
		return chunkyIntegration;
	}

	public static void setChunkyIntegration(boolean chunkyIntegration) {
		ManhuntMod.chunkyIntegration = chunkyIntegration;
	}

	public static boolean isPaused() {
		return paused;
	}

	public static void setPaused(boolean paused) {
		ManhuntMod.paused = paused;
	}

	@Override
	public void onInitialize() {
		config.load();

        setChunkyIntegration(FabricLoader.getInstance().isModLoaded("chunky"));

		setGameState(GameState.PREGAME);

        try {
            FileUtils.deleteDirectory(getGameDir().resolve("world/dimensions/manhunt/lobby").toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to delete lobby dimension");
        }

        Path datapackPath = gameDir.resolve("world/datapacks/manhunt.zip");

		try {
			datapackPath.getParent().toFile().mkdirs();

			Files.deleteIfExists(datapackPath);
			Files.createFile(datapackPath);

			IOUtils.copy(ManhuntMod.class.getResourceAsStream("/manhunt/datapack.zip"), new FileOutputStream(datapackPath.toFile()));
		} catch (IOException e) {
			LOGGER.error("Failed to copy datapack");
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CoordsCommand.register(dispatcher);
			HunterCommand.register(dispatcher);
			OneHunterCommand.register(dispatcher);
			OneRunnerCommand.register(dispatcher);
			PauseCommand.register(dispatcher);
			PingCommand.register(dispatcher);
			ResetCommand.register(dispatcher);
			RunnerCommand.register(dispatcher);
			StartCommand.register(dispatcher);
			TrackCommand.register(dispatcher);
			UnpauseCommand.register(dispatcher);
		});
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			loadManhuntWorlds(server, RandomSeed.getSeed());

			Events.serverStart(server);
		});
		ServerTickEvents.START_SERVER_TICK.register(Events::serverTick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Events.playerJoin(handler, server));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> Events.playerLeave(handler));
		UseItemCallback.EVENT.register((player, world, hand) -> Events.useItem(player, hand));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> Events.playerRespawn(oldPlayer));
	}

	public static void loadManhuntWorlds(MinecraftServer server, long seed) {
		if (overworldHandle != null) {
			overworldHandle.delete();
			netherHandle.delete();
			endHandle.delete();
		}

		Fantasy fantasy = Fantasy.get(server);

		RuntimeWorldConfig overworldConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.OVERWORLD)
				.setDifficulty(config.getGameDifficulty())
				.setGenerator(server.getOverworld().getChunkManager().getChunkGenerator())
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		RuntimeWorldConfig netherConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.THE_NETHER)
				.setDifficulty(config.getGameDifficulty())
				.setGenerator(server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier("minecraft", "the_nether"))).getChunkManager().getChunkGenerator())
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		RuntimeWorldConfig endConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.THE_END)
				.setDifficulty(config.getGameDifficulty())
				.setGenerator(server.getWorld(RegistryKey.of(RegistryKeys.WORLD, new Identifier("minecraft", "the_end"))).getChunkManager().getChunkGenerator())
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		overworldHandle = fantasy.openTemporaryWorld(overworldConfig);
		netherHandle = fantasy.openTemporaryWorld(netherConfig);
		endHandle = fantasy.openTemporaryWorld(endConfig);

		overworld = overworldHandle.asWorld();
		nether = netherHandle.asWorld();
		end = endHandle.asWorld();

		end.setEnderDragonFight(new EnderDragonFight(end, end.getSeed(), EnderDragonFight.Data.DEFAULT));

		setWorldSpawnPos(new BlockPos(0, 0, 0));
		setOverworldSpawn(new BlockPos(0, 0, 0));

		if (isChunkyIntegration() && config.isPreloadChunks()) {
			setPreloaded(false);

			schedulePreload(server);
		}
	}

	public static void schedulePreload(MinecraftServer server) {
		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutorService.schedule(() -> startPreload(server), 2, TimeUnit.SECONDS);
	}

	private static void startPreload(MinecraftServer server) {
		ChunkyAPI chunky = ChunkyProvider.get().getApi();

		chunky.cancelTask("manhunt:overworld");
		chunky.cancelTask("manhunt:the_nether");

		try {
			FileUtils.deleteDirectory(getGameDir().resolve("config/chunky/tasks").toFile());
		} catch (IOException e) {
			LOGGER.error("Failed to delete Chunky tasks");
		}

		chunky.startTask("manhunt:overworld", "square", 0, 0, 8000, 8000, "concentric");
		chunky.startTask("manhunt:the_nether", "square", 0, 0, 1000, 1000, "concentric");

		chunky.onGenerationComplete(event -> {
			if (event.world().equals("manhunt:overworld") && !isPreloaded()) {
				setPreloaded(true);
			}
		});

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			ManhuntGame.setPlayerSpawn(overworld, player);
		}
	}
}