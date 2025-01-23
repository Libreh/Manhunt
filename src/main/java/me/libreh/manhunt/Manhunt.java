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
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static me.libreh.manhunt.utils.Constants.GAME_DIR;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class Manhunt implements ModInitializer {
    public static final String MOD_ID = "manhunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Config.loadConfig();
        PlayerDataApi.register(PlayerData.STORAGE);
        try {
            FileUtils.deleteDirectory(GAME_DIR.resolve("world/datapacks/manhunt").toFile());
            Path datapackPath = GAME_DIR.resolve("world/datapacks/manhunt.zip");
            datapackPath.getParent().toFile().mkdirs();
            Files.deleteIfExists(datapackPath);
            Files.createFile(datapackPath);
            IOUtils.copy(Manhunt.class.getResourceAsStream("/manhunt/datapack.zip"), new FileOutputStream(datapackPath.toFile()));
        } catch (IOException e) {
            LOGGER.error("Failed to add datapack", e);
        }

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
            unzipLobbyWorld();
            server = minecraftServer;
            scoreboard = server.getScoreboard();
            overworld = minecraftServer.getWorld(World.OVERWORLD);
            theNether = minecraftServer.getWorld(World.NETHER);
            theEnd = minecraftServer.getWorld(World.END);
            ServerStart.serverStart(server);
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