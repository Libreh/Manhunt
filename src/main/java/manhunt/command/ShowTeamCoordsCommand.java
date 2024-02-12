package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShowTeamCoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("showteamcoords")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> showTeamCoords(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int showTeamCoords(ServerCommandSource source, ServerPlayerEntity player) {
        if (ManhuntGame.gameState == ManhuntState.PLAYING) {
            MinecraftServer server = Manhunt.SERVER;

            if (source.getPlayer().getScoreboardTeam().isEqual(server.getScoreboard().getTeam("hunters"))) {
                if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters"))) {
                    MessageUtil.sendMessage(source.getPlayer(), "manhunt.chat.huntercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                } else {
                    source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.no"), false);
                }
            } else {
                if (player.isTeamPlayer(server.getScoreboard().getTeam("runners"))) {
                    MessageUtil.sendMessage(source.getPlayer(), "manhunt.chat.runnercoords", player.getName().getString(), (int) player.getX(), (int) player.getY(), (int) player.getZ());
                } else {
                    source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.no"), false);
                }
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
