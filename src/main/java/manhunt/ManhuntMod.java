package manhunt;

import manhunt.command.*;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.Table;
import me.mrnavastar.sqlib.database.Database;
import me.mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ManhuntConfig config = ManhuntConfig.INSTANCE;
	public static final Path gameDir = FabricLoader.getInstance().getGameDir();
	public static final Database database = SQLib.getDatabase();
	public static final Table table = database.createTable(MOD_ID, "playerdata")
			.addColumn("game_titles", SQLDataType.BOOL)
			.addColumn("manhunt_sounds", SQLDataType.BOOL)
			.addColumn("night_vision", SQLDataType.BOOL)
			.addColumn("friendly_fire", SQLDataType.BOOL)
			.addColumn("bed_explosions", SQLDataType.BOOL)
			.addColumn("lava_pvp_in_nether", SQLDataType.BOOL)
			.finish();
	public static GameState state;
	private static final Identifier overworldIdentifier = Identifier.of(MOD_ID, "overworld");
	private static final Identifier netherIdentifier = Identifier.of(MOD_ID, "the_nether");
	private static final Identifier endIdentifier = Identifier.of(MOD_ID, "the_end");
	public static final RegistryKey<World> lobbyRegistry = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(MOD_ID, "lobby"));
	private static RuntimeWorldHandle overworldHandle;
	private static RuntimeWorldHandle netherHandle;
	private static RuntimeWorldHandle endHandle;
	private static ServerWorld overworld;
	private static ServerWorld theNether;
	private static ServerWorld theEnd;
	public static BlockPos worldSpawnPos;
	public static final BlockPos lobbySpawnPos = new BlockPos(0, 64, 0);
	public static final Vec3d lobbySpawn = new Vec3d(0.5, 64, 0.5);
	private static boolean preloaded = false;
	public static boolean chunkyLoaded = false;
	public static boolean duration = false;
	public static boolean paused = false;
	public static boolean headstart = false;
	public static int durationTime = 0;
	public static int pauseTime = 0;
	public static int headstartTime = 0;
	public static int spawnRadius = 0;
	public static List<ServerPlayerEntity> playerList = new ArrayList<>();
	public static List<ServerPlayerEntity> allPlayers;
	public static List<ServerPlayerEntity> allRunners;
	public static final List<MutableText> hunterCoords = new ArrayList<>();
	public static final List<MutableText> runnerCoords = new ArrayList<>();
	public static final List<UUID> allReadyUps = new ArrayList<>();
	public static final List<UUID> newPlayersList = new ArrayList<>();
	public static final List<UUID> leftOnPause = new ArrayList<>();
	public static final HashMap<UUID, Boolean> gameTitles = new HashMap<>();
	public static final HashMap<UUID, Boolean> manhuntSounds = new HashMap<>();
	public static final HashMap<UUID, Boolean> nightVision = new HashMap<>();
	public static final HashMap<UUID, Boolean> friendlyFire = new HashMap<>();
	public static final HashMap<UUID, Boolean> bedExplosions = new HashMap<>();
	public static final HashMap<UUID, Boolean> lavaPvpInNether = new HashMap<>();
	public static final HashMap<UUID, Integer> slowDownManager = new HashMap<>();
	public static final HashMap<UUID, BlockPos> playerSpawnPos = new HashMap<>();
	public static final HashMap<UUID, Collection<StatusEffectInstance>> playerEffects = new HashMap<>();
	public static final HashMap<UUID, Vec3d> playerPos = new HashMap<>();
	public static final HashMap<UUID, Float> playerYaw = new HashMap<>();
	public static final HashMap<UUID, Float> playerPitch = new HashMap<>();
	public static final HashMap<UUID, Integer> playerFood = new HashMap<>();
	public static final HashMap<UUID, Float> playerSaturation = new HashMap<>();
	public static final HashMap<UUID, Float> playerExhuastion = new HashMap<>();
	public static final HashMap<UUID, Integer> parkourTimer = new HashMap<>();
	public static final HashMap<UUID, Boolean> startedParkour = new HashMap<>();
	public static final HashMap<UUID, Boolean> finishedParkour = new HashMap<>();

	public static ServerWorld getOverworld() {
		return overworld;
	}

	public static ServerWorld getTheNether() {
		return theNether;
	}

	public static ServerWorld getTheEnd() {
		return theEnd;
	}

	@Override
	public void onInitialize() {
		config.load();

        chunkyLoaded = (FabricLoader.getInstance().isModLoaded("chunky"));

        try {
            FileUtils.deleteDirectory(gameDir.resolve("world/dimensions/manhunt/lobby").toFile());
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

			GameEvents.serverStart(server);
		});
		ServerTickEvents.START_SERVER_TICK.register(GameEvents::serverTick);
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> GameEvents.playerRespawn(newPlayer));
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> GameEvents.playerJoin(handler));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> GameEvents.playerLeave(handler));
		UseItemCallback.EVENT.register(GameEvents::useItem);
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> GameEvents.useBlock(player, world, hand));
	}

	public static void loadManhuntWorlds(MinecraftServer server, long seed) {
		if (overworldHandle != null) {
			overworldHandle.unload();
			netherHandle.unload();
			endHandle.unload();
		}

		Fantasy fantasy = Fantasy.get(server);

		RuntimeWorldConfig overworldConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.OVERWORLD)
				.setMirrorOverworldDifficulty(true)
				.setMirrorOverworldGameRules(true)
				.setGenerator(server.getOverworld().getChunkManager().getChunkGenerator())
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		RuntimeWorldConfig netherConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.THE_NETHER)
				.setMirrorOverworldDifficulty(true)
				.setMirrorOverworldGameRules(true)
				.setGenerator(server.getWorld(World.NETHER).getChunkManager().getChunkGenerator())
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		RuntimeWorldConfig endConfig = new RuntimeWorldConfig()
				.setDimensionType(DimensionTypes.THE_END)
				.setMirrorOverworldDifficulty(true)
				.setMirrorOverworldGameRules(true)
				.setGenerator(server.getWorld(World.END).getChunkManager().getChunkGenerator())
				.setShouldTickTime(true)
				.setTimeOfDay(0)
				.setSeed(seed);

		overworldHandle = fantasy.openTemporaryWorld(overworldIdentifier, overworldConfig);
		netherHandle = fantasy.openTemporaryWorld(netherIdentifier, netherConfig);
		endHandle = fantasy.openTemporaryWorld(endIdentifier, endConfig);

		overworld = overworldHandle.asWorld();
		theNether = netherHandle.asWorld();
		theEnd = endHandle.asWorld();

		theEnd.setEnderDragonFight(new EnderDragonFight(theEnd, theEnd.getSeed(), EnderDragonFight.Data.DEFAULT));

		worldSpawnPos = new BlockPos(0, 0, 0);

		if (chunkyLoaded && config.isChunky()) {
			preloaded = true;

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
		chunky.cancelTask("manhunt:the_end");

		try {
			FileUtils.deleteDirectory(gameDir.resolve("config/chunky/tasks").toFile());
		} catch (IOException e) {
			LOGGER.error("Failed to delete Chunky tasks");
		}

		if (config.getOverworld() != 0) {
			chunky.startTask("manhunt:overworld", "square", 0, 0, config.getOverworld(), config.getOverworld(), "concentric");
		}
		if (config.getNether() != 0) {
			chunky.startTask("manhunt:the_nether", "square", 0, 0, config.getNether(), config.getNether(), "concentric");
		}
		if (config.getEnd() != 0) {
			chunky.startTask("manhunt:the_end", "square", 0, 0, config.getEnd(), config.getEnd(), "concentric");
		}

		chunky.onGenerationComplete(event -> {
			if (event.world().equals("manhunt:overworld") && !preloaded) {
				preloaded = true;
			}
		});

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (!playerSpawnPos.containsKey(player.getUuid())) {
				ManhuntGame.setPlayerSpawn(overworld, player);
			}
		}
	}

	public static boolean checkPermission(ServerPlayerEntity player, String key) {
		return Permissions.check(player, key) || player.hasPermissionLevel(1) || player.hasPermissionLevel(2) ||player.hasPermissionLevel(2) ||player.hasPermissionLevel(4);
	}
}