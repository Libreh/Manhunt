package manhunt;

import manhunt.game.ManhuntGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static manhunt.config.ManhuntConfig.setDefaults;
import static manhunt.game.ManhuntGame.*;

public class ManhuntMod implements ModInitializer {
	public static final String MOD_ID = "manhunt";
	public static final Logger LOGGER = LogManager.getLogger("Manhunt");

	@Override
	public void onInitialize() {
		setDefaults();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> commandRegister(dispatcher));
		ServerLifecycleEvents.SERVER_STARTED.register(ManhuntGame::serverStart);
		ServerTickEvents.START_SERVER_TICK.register(ManhuntGame::serverTick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> playerJoin(handler, server));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playerDisconnect(handler));
		UseItemCallback.EVENT.register((player, world, hand) -> useItem(player, hand));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> playerRespawn(newPlayer));
		ServerWorldEvents.UNLOAD.register(ManhuntGame::unloadWorld);
		ServerLifecycleEvents.SERVER_STOPPED.register(ManhuntGame::serverStop);
	}
}