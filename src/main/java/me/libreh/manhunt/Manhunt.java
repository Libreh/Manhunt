package me.libreh.manhunt;

import eu.pb4.playerdata.api.PlayerDataApi;
import me.libreh.manhunt.commands.*;
import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.config.PlayerData;
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

import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class Manhunt implements ModInitializer {
    public static final String MOD_ID = "manhunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Config.loadConfig();
        PlayerDataApi.register(PlayerData.STORAGE);

        deleteWorld();
        unzip();

        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            CoordsCommands.coordsCommand(dispatcher); CoordsCommands.listCoordsCommands(dispatcher);
            PauseCommands.pauseCommand(dispatcher); PauseCommands.unpauseCommand(dispatcher);
            GeneralCommands.durationCommand(dispatcher); GeneralCommands.feedCommand(dispatcher);

            GuiCommands.configCommand(dispatcher);
            GuiCommands.preferencesCommand(dispatcher);

            RoleCommands.hunterCommand(dispatcher);
            RoleCommands.oneHunterCommand(dispatcher);
            RoleCommands.oneRunnerCommand(dispatcher);
            RoleCommands.runnerCommand(dispatcher);

            GeneralCommands.manhuntCommand(dispatcher);
            GeneralCommands.resetCommand(dispatcher);
            GeneralCommands.startCommand(dispatcher);
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
                if (paused) {
                    return false;
                } else {
                    return headStartTicks == 0 || !isHunter(player);
                }
            }
            return true;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (isPlaying()) {
                if (paused) {
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