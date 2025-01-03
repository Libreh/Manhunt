package me.libreh.manhunt.event;

import me.libreh.manhunt.config.ManhuntConfig;
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

        RUNNERS_TEAM = SCOREBOARD.addTeam("runners");
        RUNNERS_TEAM.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        RUNNERS_TEAM.setShowFriendlyInvisibles(true);
        RUNNERS_TEAM.getPlayerList().clear();

        HUNTERS_TEAM = SCOREBOARD.addTeam("hunters");
        HUNTERS_TEAM.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        HUNTERS_TEAM.setShowFriendlyInvisibles(true);
        HUNTERS_TEAM.getPlayerList().clear();

        if (ManhuntConfig.CONFIG.isTeamColor()) {
            RUNNERS_TEAM.setColor(ManhuntConfig.CONFIG.getRunnersColor());
            HUNTERS_TEAM.setColor(ManhuntConfig.CONFIG.getHuntersColor());

        } else {
            RUNNERS_TEAM.setColor(Formatting.RESET);
            HUNTERS_TEAM.setColor(Formatting.RESET);
        }
    }
}
