package me.libreh.manhunt.event;

import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.game.GameState;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import static me.libreh.manhunt.utils.Fields.*;

public class ServerStart {
    public static void serverStart(MinecraftServer server) {
        gameState = GameState.PREGAME;

        server.setMotd(gameState.getColor() + "[" + gameState.getMotd() + "]§f Manhunt");

        GameRules gameRules = server.getGameRules();
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
        gameRules.get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(false, server);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);

        runnersTeam = scoreboard.addTeam("runners");
        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        runnersTeam.setShowFriendlyInvisibles(true);
        runnersTeam.getPlayerList().clear();

        huntersTeam = scoreboard.addTeam("hunters");
        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        huntersTeam.setShowFriendlyInvisibles(true);
        huntersTeam.getPlayerList().clear();

        if (Config.getConfig().gameOptions.teamColor.enabled) {
            runnersTeam.setColor(Config.getConfig().gameOptions.teamColor.runnersColor);
            huntersTeam.setColor(Config.getConfig().gameOptions.teamColor.huntersColor);

        } else {
            runnersTeam.setColor(Formatting.RESET);
            huntersTeam.setColor(Formatting.RESET);
        }
    }
}
