package me.libreh.manhunt.game;

import me.libreh.manhunt.commands.CoordsCommands;
import me.libreh.manhunt.commands.GeneralCommands;
import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.config.PlayerData;
import me.libreh.manhunt.world.ServerWorldController;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.Arrays;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class ManhuntGame {
    public static void startGame() {
        canStart = false;
        startTicks = 0;

        if (!chunkFutureList.isEmpty()) {
            chunkFutureList = new ArrayList<>();
        }

        resetWorldTime();

        var gameRules = server.getGameRules();
        gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
        gameRules.get(GameRules.DO_FIRE_TICK).set(true, server);
        gameRules.get(GameRules.DO_INSOMNIA).set(true, server);
        gameRules.get(GameRules.DO_MOB_LOOT).set(true, server);
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(true, server);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(true, server);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(3, server);
        gameRules.get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
        gameRules.get(GameRules.FALL_DAMAGE).set(true, server);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(false, server);
        gameRules.get(GameRules.DISABLE_PLAYER_MOVEMENT_CHECK).set(true, server);

        if (Config.getConfig().modIntegrations.vanillaIntegration.enabled) {
            gameRules.get(GameRules.SPAWN_RADIUS).set(Config.getConfig().modIntegrations.vanillaIntegration.spawnRadius, server);
            gameRules.get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(Config.getConfig().modIntegrations.vanillaIntegration.spectatorsGenerateChunks, server);
            server.setDifficulty(Config.getConfig().modIntegrations.vanillaIntegration.difficulty, true);
            var worldBorder = overworld.getWorldBorder();
            worldBorder.setCenter(0, 0);
            worldBorder.setSize(Config.getConfig().modIntegrations.vanillaIntegration.borderSize);
        }

        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        runnersTeam.setShowFriendlyInvisibles(false);
        runnersTeam.getPlayerList().removeIf(playerName -> !Arrays.asList(server.getPlayerNames()).contains(playerName));

        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        huntersTeam.setShowFriendlyInvisibles(false);
        huntersTeam.getPlayerList().removeIf(playerName -> !Arrays.asList(server.getPlayerNames()).contains(playerName));

        if (Config.getConfig().gameOptions.teamColor.enabled) {
            runnersTeam.setColor(Config.getConfig().gameOptions.teamColor.runnersColor);
            huntersTeam.setColor(Config.getConfig().gameOptions.teamColor.huntersColor);
        } else {
            runnersTeam.setColor(Formatting.RESET);
            huntersTeam.setColor(Formatting.RESET);
        }

        if (Config.getConfig().gameOptions.headStart != 0) {
            headStartTicks = (Config.getConfig().gameOptions.headStart + 1) * 20;
        }

        if (Config.getConfig().gameOptions.timeLimit != 0) {
            timeLimitTicks = Config.getConfig().gameOptions.timeLimit * 60 * 20;
        }

        String playerName = runnersTeam.getPlayerList().iterator().next();
        ServerPlayerEntity runner = server.getPlayerManager().getPlayer(playerName);
        if (Config.getConfig().globalPreferences.bedExplosionsPvP.equals(PER_RUNNER)) {
            var bedExplosionsPvP = PlayerData.get(runner).bedExplosionsPvP;
            if (bedExplosionsPvP) {
                Config.getConfig().globalPreferences.bedExplosionsPvP = PER_RUNNER;
            } else {
                Config.getConfig().globalPreferences.bedExplosionsPvP = "off";
            }
        }
        if (Config.getConfig().globalPreferences.bedExplosionsPvP.equals(PER_RUNNER)) {
            var netherLavaPvP = PlayerData.get(runner).netherLavaPvP;
            if (netherLavaPvP) {
                Config.getConfig().globalPreferences.bedExplosionsPvP = "on";
            } else {
                Config.getConfig().globalPreferences.bedExplosionsPvP = "off";
            }
        }

        SAVED_EFFECTS.clear();
        SAVED_POS.clear();
        SAVED_YAW.clear();
        SAVED_PITCH.clear();
        SAVED_AIR.clear();
        SAVED_HUNGER.clear();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            START_LIST.add(player.getUuid());
        }

        READY_LIST.clear();
        PARKOUR_TIMER.clear();
        STARTED_PARKOUR.clear();
        FINISHED_PARKOUR.clear();
    }

    public static void endGame() {
        shouldEnd = false;

        server.getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(true, server);

        GeneralCommands.setDuration();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            var data = PlayerData.get(player);
            if (Config.getConfig().globalPreferences.announceSeed.equals("always") || data.announceSeed) {
                player.sendMessage(Text.translatable("commands.seed.success",
                        Texts.bracketedCopyable(String.valueOf(overworld.getSeed()))), false);
            }
            if (Config.getConfig().globalPreferences.announceDuration.equals("always") || data.announceDuration) {
                player.sendMessage(Text.translatable("chat.manhunt.duration",
                        Texts.bracketedCopyable(GeneralCommands.duration)), false);
            }
        }
    }

    public static void resetGame() {
        ServerWorldController.resetWorlds(GeneralCommands.seed);

        resetWorldTime();

        var gameRules = server.getWorld(LOBBY_REGISTRY_KEY).getGameRules();
        gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        gameRules.get(GameRules.DO_FIRE_TICK).set(false, server);
        gameRules.get(GameRules.DO_INSOMNIA).set(false, server);
        gameRules.get(GameRules.DO_MOB_LOOT).set(false, server);
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, server);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        gameRules.get(GameRules.FALL_DAMAGE).set(false, server);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(0, server);
        gameRules.get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        gameRules.get(GameRules.FALL_DAMAGE).set(false, server);
        gameRules.get(GameRules.SPAWN_RADIUS).set(0, server);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);

        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        huntersTeam.setShowFriendlyInvisibles(true);
        huntersTeam.getPlayerList().removeIf(playerName -> !Arrays.asList(server.getPlayerNames()).contains(playerName));

        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        runnersTeam.getPlayerList().removeIf(playerName -> !Arrays.asList(server.getPlayerNames()).contains(playerName));

        if (Config.getConfig().gameOptions.teamColor.enabled) {
            huntersTeam.setColor(Config.getConfig().gameOptions.teamColor.huntersColor);
            runnersTeam.setColor(Config.getConfig().gameOptions.teamColor.runnersColor);
        } else {
            huntersTeam.setColor(Formatting.RESET);
            runnersTeam.setColor(Formatting.RESET);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            teleportToLobby(player);
            RESET_LIST.add(player.getUuid());
        }

        resetPresetMode();

        CoordsCommands.HUNTER_COORDS.clear();
        CoordsCommands.RUNNER_COORDS.clear();
        GeneralCommands.duration = "";

        PLAY_LIST.clear();
        PAUSE_LEAVE_LIST.clear();
        OVERWORLD_POSITION.clear();
        NETHER_POSITION.clear();
        END_POSITION.clear();
        SPAWN_POS.clear();

        shouldEnd = false;
        isPaused = false;
        headStartTicks = 0;
        timeLimitTicks = 0;
    }
}