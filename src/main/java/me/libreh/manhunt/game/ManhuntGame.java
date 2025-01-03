package me.libreh.manhunt.game;

import me.libreh.manhunt.command.ResetCommand;
import me.libreh.manhunt.command.game.DurationCommand;
import me.libreh.manhunt.command.game.coords.CoordsCommand;
import me.libreh.manhunt.config.PreferencesData;
import me.libreh.manhunt.world.ServerWorldController;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.Arrays;

import static me.libreh.manhunt.config.ManhuntConfig.CONFIG;
import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.resetPresetMode;
import static me.libreh.manhunt.utils.Methods.resetWorldTime;

public class ManhuntGame {
    public static void startGame() {
        canStart = false;

        if (!chunkFutureList.isEmpty()) {
            chunkFutureList = new ArrayList<>();
        }

        resetWorldTime();

        var gameRules = SERVER.getGameRules();
        gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, SERVER);
        gameRules.get(GameRules.DO_FIRE_TICK).set(true, SERVER);
        gameRules.get(GameRules.DO_INSOMNIA).set(true, SERVER);
        gameRules.get(GameRules.DO_MOB_LOOT).set(true, SERVER);
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(true, SERVER);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, SERVER);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(true, SERVER);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(3, SERVER);
        gameRules.get(GameRules.SHOW_DEATH_MESSAGES).set(true, SERVER);
        gameRules.get(GameRules.FALL_DAMAGE).set(true, SERVER);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(false, SERVER);
        gameRules.get(GameRules.DISABLE_PLAYER_MOVEMENT_CHECK).set(true, SERVER);

        if (CONFIG.isVanilla()) {
            gameRules.get(GameRules.SPAWN_RADIUS).set(CONFIG.getSpawnRadius(), SERVER);
            gameRules.get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(CONFIG.isSpectatorsGenerateChunks(), SERVER);
            SERVER.setDifficulty(CONFIG.getDifficulty(), true);
            var worldBorder = OVERWORLD.getWorldBorder();
            worldBorder.setCenter(0, 0);
            worldBorder.setSize(CONFIG.getWorldBorder());
        }

        RUNNERS_TEAM.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        RUNNERS_TEAM.setShowFriendlyInvisibles(false);
        RUNNERS_TEAM.getPlayerList().removeIf(playerName -> !Arrays.asList(SERVER.getPlayerNames()).contains(playerName));

        HUNTERS_TEAM.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        HUNTERS_TEAM.setShowFriendlyInvisibles(false);
        HUNTERS_TEAM.getPlayerList().removeIf(playerName -> !Arrays.asList(SERVER.getPlayerNames()).contains(playerName));

        if (CONFIG.isTeamColor()) {
            RUNNERS_TEAM.setColor(CONFIG.getRunnersColor());
            HUNTERS_TEAM.setColor(CONFIG.getHuntersColor());
        } else {
            RUNNERS_TEAM.setColor(Formatting.RESET);
            HUNTERS_TEAM.setColor(Formatting.RESET);
        }

        if (CONFIG.getHeadStartSec() != 0) {
            headStartTicks = (CONFIG.getHeadStartSec() + 1) * 20;
        }

        if (CONFIG.getTimeLimitMin() != 0) {
            timeLimitTicks = CONFIG.getTimeLimitMin() * 60 * 20;
        }

        String playerName = RUNNERS_TEAM.getPlayerList().iterator().next();
        ServerPlayerEntity runner = SERVER.getPlayerManager().getPlayer(playerName);
        if (CONFIG.getBedExplosionsPvP().equals(RUNNERS_PREFERENCE)) {
            var bedExplosionsPvP = PreferencesData.get(runner).bedExplosionsPvP;
            if (bedExplosionsPvP) {
                CONFIG.setBedExplosionsPvP("on");
            } else {
                CONFIG.setBedExplosionsPvP("off");
            }
        }
        if (CONFIG.getNetherLavaPvP().equals(RUNNERS_PREFERENCE)) {
            var netherLavaPvP = PreferencesData.get(runner).netherLavaPvP;
            if (netherLavaPvP) {
                CONFIG.setNetherLavaPvP("on");
            } else {
                CONFIG.setNetherLavaPvP("off");
            }
        }

        SAVED_EFFECTS.clear();
        SAVED_POS.clear();
        SAVED_YAW.clear();
        SAVED_PITCH.clear();
        SAVED_AIR.clear();
        SAVED_HUNGER.clear();

        for (ServerPlayerEntity player : SERVER.getPlayerManager().getPlayerList()) {
            START_LIST.add(player.getUuid());
        }

        READY_LIST.clear();
        PARKOUR_TIMER.clear();
        STARTED_PARKOUR.clear();
        FINISHED_PARKOUR.clear();
    }

    public static void endGame() {
        shouldEnd = false;

        SERVER.getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(true, SERVER);

        DurationCommand.setDuration();

        for (ServerPlayerEntity player : SERVER.getPlayerManager().getPlayerList()) {
            var data = PreferencesData.get(player);
            if (CONFIG.getAnnounceSeed().equals("always") || data.announceSeed) {
                player.sendMessage(Text.translatable("commands.seed.success",
                        Texts.bracketedCopyable(String.valueOf(ResetCommand.seed))), false);
            }
            if (CONFIG.getAnnounceDuration().equals("always") || data.announceDuration) {
                player.sendMessage(Text.translatable("chat.manhunt.duration",
                        Texts.bracketedCopyable(DurationCommand.duration)), false);
            }
        }
    }

    public static void resetGame() {
        ServerWorldController.resetWorlds(ResetCommand.seed);

        resetWorldTime();

        var gameRules = SERVER.getWorld(LOBBY_REGISTRY_KEY).getGameRules();
        gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, SERVER);
        gameRules.get(GameRules.DO_FIRE_TICK).set(false, SERVER);
        gameRules.get(GameRules.DO_INSOMNIA).set(false, SERVER);
        gameRules.get(GameRules.DO_MOB_LOOT).set(false, SERVER);
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, SERVER);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, SERVER);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, SERVER);
        gameRules.get(GameRules.FALL_DAMAGE).set(false, SERVER);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(0, SERVER);
        gameRules.get(GameRules.SHOW_DEATH_MESSAGES).set(false, SERVER);
        gameRules.get(GameRules.FALL_DAMAGE).set(false, SERVER);
        gameRules.get(GameRules.SPAWN_RADIUS).set(0, SERVER);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, SERVER);

        HUNTERS_TEAM.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        HUNTERS_TEAM.setShowFriendlyInvisibles(true);
        HUNTERS_TEAM.getPlayerList().removeIf(playerName -> !Arrays.asList(SERVER.getPlayerNames()).contains(playerName));

        RUNNERS_TEAM.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        RUNNERS_TEAM.getPlayerList().removeIf(playerName -> !Arrays.asList(SERVER.getPlayerNames()).contains(playerName));

        if (CONFIG.isTeamColor()) {
            HUNTERS_TEAM.setColor(CONFIG.getHuntersColor());
            RUNNERS_TEAM.setColor(CONFIG.getRunnersColor());
        } else {
            HUNTERS_TEAM.setColor(Formatting.RESET);
            RUNNERS_TEAM.setColor(Formatting.RESET);
        }

        for (ServerPlayerEntity player : SERVER.getPlayerManager().getPlayerList()) {
            var playerUuid = player.getUuid();
            RESET_LIST.add(playerUuid);
        }

        resetPresetMode();

        CoordsCommand.HUNTER_COORDS.clear();
        CoordsCommand.RUNNER_COORDS.clear();
        DurationCommand.duration = "";

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