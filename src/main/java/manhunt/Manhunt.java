package manhunt;

import manhunt.config.Configs;
import manhunt.game.ManhuntGame;
import manhunt.util.LogUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

import static manhunt.game.ManhuntGame.*;

public class Manhunt implements ModInitializer {
	public static final String MOD_ID = "manhunt";
	public static final Logger LOGGER = LogUtil.createLogger("Manhunt");
	public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID).toAbsolutePath();
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		Configs.configHandler.loadFromDisk();

		LOGGER.info("Manhunt mod initialized");

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> commandRegister(dispatcher));
		ServerLifecycleEvents.SERVER_STARTED.register(ManhuntGame::serverStart);
		ServerTickEvents.START_SERVER_TICK.register(ManhuntGame::serverTick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> playerJoin(handler, server));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playerDisconnect(handler));
		UseItemCallback.EVENT.register((player, world, hand) -> useItem(player, hand));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> playerRespawn(newPlayer));
		ServerWorldEvents.UNLOAD.register(ManhuntGame::unloadWorld);
	}
}