package libreh.manhunt.event;

import libreh.manhunt.config.ManhuntConfig;
import libreh.manhunt.game.GameState;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;

import static libreh.manhunt.ManhuntMod.gameState;

public class OnServerStart {
    public static void onServerStart(MinecraftServer server) {
        gameState = GameState.PREGAME;

        if (ManhuntConfig.CONFIG.isSetMotd()) {
            server.setMotd(gameState.getColor() + "[" + gameState.getMotd() + "]§f Minecraft " + "MANHUNT");
        }

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

        Scoreboard scoreboard = server.getScoreboard();

        if (scoreboard.getTeam("hunters") != null) scoreboard.removeTeam(scoreboard.getTeam("hunters"));
        scoreboard.addTeam("hunters");
        Team huntersTeam = scoreboard.getTeam("hunters");
        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        huntersTeam.setShowFriendlyInvisibles(true);

        if (scoreboard.getTeam("runners") != null) scoreboard.removeTeam(scoreboard.getTeam("runners"));
        scoreboard.addTeam("runners");
        Team runnersTeam = scoreboard.getTeam("runners");
        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        runnersTeam.setShowFriendlyInvisibles(true);

        if (ManhuntConfig.CONFIG.isTeamColor()) {
            huntersTeam.setColor(ManhuntConfig.CONFIG.getHuntersColor());
            runnersTeam.setColor(ManhuntConfig.CONFIG.getRunnersColor());
        } else {
            huntersTeam.setColor(Formatting.RESET);
            runnersTeam.setColor(Formatting.RESET);
        }
    }
}
