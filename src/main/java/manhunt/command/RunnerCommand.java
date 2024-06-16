package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static manhunt.ManhuntMod.config;
import static manhunt.ManhuntMod.getGameState;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("runner")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME)
                .executes(context -> setRunner(context.getSource(), context.getSource().getPlayer()))
                .requires(source -> ManhuntMod.checkPermission(source.getPlayer(), "manhunt.runner"))
                .then(argument("targets", EntityArgumentType.players())
                        .executes(context -> setRunners(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                )
        );
    }

    private static int setRunner(ServerCommandSource source, ServerPlayerEntity player) {
        if (!player.getScoreboard().getTeam("runners").getPlayerList().contains(player.getNameForScoreboard())) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.joined_team", Text.literal(player.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("manhunt.role.runners").formatted(config.getRunnersColor())), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.already_team", Text.translatable("manhunt.role.runner").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setRunners(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.set_role", Text.literal(player.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("manhunt.role.runner").formatted(config.getRunnersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
