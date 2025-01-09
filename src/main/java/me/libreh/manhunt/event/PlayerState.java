package me.libreh.manhunt.event;

import me.libreh.manhunt.commands.PauseCommands;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.RUNNERS_TEAM;
import static me.libreh.manhunt.utils.Fields.paused;
import static me.libreh.manhunt.utils.Methods.isPlaying;
import static me.libreh.manhunt.utils.Methods.isRunner;

public class PlayerState {
    public static void playerRespawn(ServerPlayerEntity player) {
        var playerUuid = player.getUuid();

        JOIN_LIST.add(playerUuid);
    }

    public static void playerJoin(ServerPlayNetworkHandler handler) {
        var player = handler.player;
        var playerUuid = player.getUuid();

        JOIN_LIST.add(playerUuid);

        SPAM_PREVENTION.putIfAbsent(playerUuid, 0);
    }

    public static void playerLeave(ServerPlayNetworkHandler handler) {
        var player = handler.player;
        var playerUuid = player.getUuid();
        boolean runner = isRunner(player);

        if (isPlaying()) {
            if (paused) {
                PAUSE_LEAVE_LIST.add(playerUuid);
            } else {
                if (runner && RUNNERS_TEAM.getPlayerList().size() == 1) {
                    PAUSE_LEAVE_LIST.add(playerUuid);
                    PauseCommands.pauseGame(5);
                }
            }
        }
    }

}
