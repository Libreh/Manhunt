package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static manhunt.ManhuntMod.*;

public class OneRunnerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("onerunner")
                .requires(source -> source.isExecutedByPlayer() && state == GameState.PREGAME && checkPermission(source.getPlayer(), "manhunt.onerunner"))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setOneRunner(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneRunner(ServerCommandSource source, ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : source.getServer().getPlayerManager().getPlayerList()) {
            serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
        }

        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("chat.one_role", Text.literal(player.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("role.manhunt.runner").formatted(config.getRunnersColor())), false);

        return Command.SINGLE_SUCCESS;
    }
}
