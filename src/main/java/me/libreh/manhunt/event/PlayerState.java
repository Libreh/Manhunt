package me.libreh.manhunt.event;

import me.libreh.manhunt.commands.PauseCommands;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.*;
import static me.libreh.manhunt.utils.Methods.*;

public class PlayerState {
    public static void playerRespawn(ServerPlayerEntity player) {
        if (isPreGame()) {
            var lobbyWorld = server.getWorld(LOBBY_REGISTRY_KEY);
            player.setSpawnPoint(LOBBY_REGISTRY_KEY, new BlockPos((int) LOBBY_SPAWN.x, (int) LOBBY_SPAWN.y, (int) LOBBY_SPAWN.z),
                    0.0F, true, false);
            player.teleport(lobbyWorld, LOBBY_SPAWN.x, LOBBY_SPAWN.y, LOBBY_SPAWN.z,
                    Set.of(), 180.0F, 0.0F, true);
        }
    }

    public static void playerJoin(ServerPlayNetworkHandler handler) {
        var player = handler.player;
        var playerUuid = player.getUuid();

        if (isPreGame()) {
            player.setSpawnPoint(LOBBY_REGISTRY_KEY, new BlockPos((int) LOBBY_SPAWN.x, (int) LOBBY_SPAWN.y, (int) LOBBY_SPAWN.z),
                    0.0F, true, false);
        }

        JOIN_LIST.add(playerUuid);

        SPAM_PREVENTION.putIfAbsent(playerUuid, 0);
    }

    public static void playerLeave(ServerPlayNetworkHandler handler) {
        var player = handler.player;
        var playerUuid = player.getUuid();
        boolean runner = isRunner(player);

        if (isPlaying()) {
            if (isPaused) {
                PAUSE_LEAVE_LIST.add(playerUuid);
            } else {
                if (runner && runnersTeam.getPlayerList().size() == 1) {
                    PAUSE_LEAVE_LIST.add(playerUuid);
                    PauseCommands.pauseGame(5);
                }
            }
        }
    }

}
