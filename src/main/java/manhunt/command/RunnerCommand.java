package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.ManhuntMod.getGameState;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("runner")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME && (Permissions.check(source.getPlayer(), "manhunt.runner") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> setRunner(context.getSource(), context.getSource().getPlayer()))
                .then(argument("runner", EntityArgumentType.player())
                        .executes(context -> setRunner(context.getSource(), EntityArgumentType.getPlayer(context, "runner")))
                )
        );
    }

    private static int setRunner(ServerCommandSource source, ServerPlayerEntity player) {
        if (player.getScoreboard().getTeam("hunters").getPlayerList().contains(player.getName().getString())) {
            player.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));
        }

        player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.role", Text.literal(player.getName().getString()).formatted(Formatting.GREEN), Text.translatable("manhunt.runner").formatted(Formatting.GREEN)), false);

        return Command.SINGLE_SUCCESS;
    }
}
