package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static manhunt.ManhuntMod.config;
import static manhunt.ManhuntMod.getGameState;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OneRunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("onerunner")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME && ManhuntMod.checkPermission(source.getPlayer(), "manhunt.onerunner"))
                .then(argument("player", EntityArgumentType.player())
                        .executes(context -> setOneRunner(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneRunner(ServerCommandSource source, ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
        }

        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.one_role", Text.literal(player.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("manhunt.role.runner").formatted(config.getRunnersColor())), false);

        return Command.SINGLE_SUCCESS;
    }
}
