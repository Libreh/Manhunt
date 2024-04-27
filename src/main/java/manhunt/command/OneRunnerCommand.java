package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class OneRunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("onerunner")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.getGameState() == GameState.PREGAME && (Permissions.check(source.getPlayer(), "manhunt.onerunner") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> setOneRunner(context.getSource(), context.getSource().getPlayer()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setOneRunner(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneRunner(ServerCommandSource source, ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            serverPlayer.getScoreboard().clearTeam(serverPlayer.getName().getString());
            serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getName().getString(), player.getScoreboard().getTeam("hunters"));
        }

        player.getScoreboard().clearTeam(player.getName().getString());
        player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("players"));
        player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.onerunner", Text.literal(player.getName().getString()).formatted(Formatting.GREEN), Text.translatable("manhunt.runner").formatted(Formatting.GREEN)), false);

        return Command.SINGLE_SUCCESS;
    }
}
