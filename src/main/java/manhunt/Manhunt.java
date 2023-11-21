package manhunt;

import manhunt.config.Configs;
import manhunt.game.ManhuntGame;
import manhunt.util.LogUtil;
import manhunt.util.ResetUtil;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.Table;
import me.mrnavastar.sqlib.database.Database;
import me.mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class Manhunt implements ModInitializer {
	public static final String MOD_ID = "manhunt";
	public static final Logger LOGGER = LogUtil.createLogger("Manhunt");
	public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toAbsolutePath();
	public static MinecraftServer SERVER;
	public static Database database;
	public static Table table;

	@Override
	public void onInitialize() {
		Configs.configHandler.loadFromDisk();

		database = SQLib.getDatabase();

		table = database.createTable(MOD_ID, "playerdata")
				.addColumn("mutelobbymusic", SQLDataType.BOOL)
				.addColumn("donotdisturb", SQLDataType.BOOL)
				.addColumn("pingsound", SQLDataType.STRING)
				.addColumn("gameleader", SQLDataType.BOOL)
				.finish();

		try {
			ResetUtil.invoke();
		} catch (IOException ignored) {
		}

		LOGGER.info("Manhunt mod initialized");

		CommandRegistrationCallback.EVENT.register(ManhuntGame::commandRegister);

		ServerLifecycleEvents.SERVER_STARTED.register(ManhuntGame::serverStart);

		ServerTickEvents.START_SERVER_TICK.register(ManhuntGame::serverTick);

		ServerTickEvents.START_WORLD_TICK.register(ManhuntGame::worldTick);

		UseItemCallback.EVENT.register(ManhuntGame::useItem);

		ServerMessageEvents.CHAT_MESSAGE.register(ManhuntGame::chatMessage);

		ServerPlayConnectionEvents.JOIN.register(ManhuntGame::playerJoin);

		ServerPlayConnectionEvents.DISCONNECT.register(ManhuntGame::playerDisconnect);
	}
}