package me.libreh.manhunt;

import eu.pb4.playerdata.api.PlayerDataApi;
import me.libreh.manhunt.command.ManhuntCommand;
import me.libreh.manhunt.command.ResetCommand;
import me.libreh.manhunt.command.StartCommand;
import me.libreh.manhunt.command.game.DurationCommand;
import me.libreh.manhunt.command.game.FeedCommand;
import me.libreh.manhunt.command.game.coords.CoordsCommand;
import me.libreh.manhunt.command.game.coords.ListCoordsCommand;
import me.libreh.manhunt.command.game.pause.PauseCommand;
import me.libreh.manhunt.command.game.pause.UnpauseCommand;
import me.libreh.manhunt.command.gui.ConfigCommand;
import me.libreh.manhunt.command.gui.PreferencesCommand;
import me.libreh.manhunt.command.role.HunterCommand;
import me.libreh.manhunt.command.role.OneHunterCommand;
import me.libreh.manhunt.command.role.OneRunnerCommand;
import me.libreh.manhunt.command.role.RunnerCommand;
import me.libreh.manhunt.config.PreferencesData;
import me.libreh.manhunt.event.PlayerInterfact;
import me.libreh.manhunt.event.PlayerState;
import me.libreh.manhunt.event.ServerStart;
import me.libreh.manhunt.event.ServerTick;
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
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class Manhunt implements ModInitializer {
    public static final String MOD_ID = "manhunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CONFIG.load();
        PlayerDataApi.register(PreferencesData.STORAGE);

        deleteWorld();
        unzip();

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            CoordsCommand.register(dispatcher); ListCoordsCommand.register(dispatcher);
            PauseCommand.register(dispatcher); UnpauseCommand.register(dispatcher);
            DurationCommand.register(dispatcher); FeedCommand.register(dispatcher);

            ConfigCommand.register(dispatcher); PreferencesCommand.register(dispatcher);

            HunterCommand.register(dispatcher);
            OneHunterCommand.register(dispatcher); OneRunnerCommand.register(dispatcher);
            RunnerCommand.register(dispatcher);

            ManhuntCommand.register(dispatcher);
            ResetCommand.register(dispatcher);
            StartCommand.register(dispatcher);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            SERVER = minecraftServer;
            SCOREBOARD = SERVER.getScoreboard();
            ServerStart.serverStart(SERVER);
        });
        ServerTickEvents.START_SERVER_TICK.register(ServerTick::serverTick);

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PlayerState.playerRespawn(oldPlayer));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerState.playerJoin(handler));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerState.playerLeave(handler));

        UseItemCallback.EVENT.register(PlayerInterfact::useItem);
        UseBlockCallback.EVENT.register(PlayerInterfact::useBlock);

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (isPlaying()) {
                if (isPaused) {
                    return false;
                } else {
                    return headStartTicks == 0 || !isHunter(player);
                }
            }
            return true;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isPlaying()) {
                if (isPaused) {
                    return ActionResult.FAIL;
                } else {
                    if (headStartTicks != 0 && isHunter(player)) {
                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}