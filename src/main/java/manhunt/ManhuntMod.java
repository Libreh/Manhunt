package manhunt;

import manhunt.command.*;
import manhunt.config.ManhuntConfig;
import manhunt.game.Events;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import manhunt.world.ManhuntWorldModule;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.Table;
import me.mrnavastar.sqlib.database.Database;
import me.mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ManhuntMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Manhunt");
	public static final String MOD_ID = "manhunt";
	public static final ManhuntConfig config = ManhuntConfig.INSTANCE;
	private static final Path gameDir = FabricLoader.getInstance().getGameDir();
	private static GameState gameState;
	private static final Database database = SQLib.getDatabase();
	private static Table table = database.createTable(MOD_ID, "playerdata").addColumn("game_titles", SQLDataType.BOOL).addColumn("manhunt_sounds", SQLDataType.BOOL).addColumn("night_vision", SQLDataType.BOOL).finish();
	public static final String OVERWORLD = "overworld";
	public static final String THE_NETHER = "the_nether";
	public static final String THE_END = "the_end";
	public static final RegistryKey<World> lobbyKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, "lobby"));
	public static final RegistryKey<World> overworldKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, OVERWORLD));
	public static final RegistryKey<World> theNetherKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, THE_NETHER));
	public static final RegistryKey<World> theEndKey = RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, THE_END));
	private static List<ServerPlayerEntity> allRunners;
	private static BlockPos worldSpawnPos;
	private static boolean preloaded = false;
	private static boolean chunkyIntegration = false;
	private static boolean paused = false;
	public static final List<MutableText> hunterCoords = new ArrayList<>();
	public static final List<MutableText> runnerCoords = new ArrayList<>();
	public static final HashMap<ServerPlayerEntity, Boolean> isRunner = new HashMap<>();
	public static final HashMap<ServerPlayerEntity, BlockPos> playerSpawn = new HashMap<>();
	public static final HashMap<ServerPlayerEntity, Boolean> gameTitles = new HashMap<>();
	public static final HashMap<ServerPlayerEntity, Boolean> manhuntSounds = new HashMap<>();
	public static final HashMap<ServerPlayerEntity, Boolean> nightVision = new HashMap<>();
	public static final HashMap<ServerPlayerEntity, Collection<StatusEffectInstance>> playerEffects = new HashMap<>();

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

	public static List<ServerPlayerEntity> getAllRunners() {
		return allRunners;
	}

	public static void setAllRunners(List<ServerPlayerEntity> allRunners) {
		ManhuntMod.allRunners = allRunners;
	}

	public static BlockPos getWorldSpawnPos() {
		return worldSpawnPos;
	}

	public static void setWorldSpawnPos(BlockPos worldSpawnPos) {
		ManhuntMod.worldSpawnPos = worldSpawnPos;
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
			FileUtils.deleteDirectory(gameDir.resolve("world/dimensions").toFile());
		} catch (IOException ignored) {}

		Path datapackPath = gameDir.resolve("world/datapacks/manhunt.zip");

		try {
			Files.deleteIfExists(datapackPath);
			datapackPath.getParent().toFile().mkdirs();
			IOUtils.copy(ManhuntMod.class.getResourceAsStream("/manhunt/datapack.zip"), new FileOutputStream(datapackPath.toFile()));
		} catch (IOException ignored) {
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CoordsCommand.register(dispatcher);
			HunterCommand.register(dispatcher);
			OneRunnerCommand.register(dispatcher);
			PauseCommand.register(dispatcher);
			PingCommand.register(dispatcher);
			ResetCommand.register(dispatcher);
			RunnerCommand.register(dispatcher);
			StartCommand.register(dispatcher);
			TrackCommand.register(dispatcher);
			UnpauseCommand.register(dispatcher);
		});
		ServerLifecycleEvents.SERVER_STARTED.register(Events::serverStart);
		ServerTickEvents.START_SERVER_TICK.register(Events::serverTick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Events.playerJoin(handler, server));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> Events.playerLeave(handler));
		UseItemCallback.EVENT.register((player, world, hand) -> Events.useItem(player, hand));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> Events.playerRespawn(oldPlayer));
		ServerWorldEvents.UNLOAD.register((server, world) -> new ManhuntWorldModule().onWorldUnload(server, world));
		ServerLifecycleEvents.SERVER_STOPPED.register(ManhuntGame::resetGame);
	}
}