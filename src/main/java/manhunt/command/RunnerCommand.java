package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

import static manhunt.ManhuntMod.config;
import static manhunt.ManhuntMod.getGameState;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("runner")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME && (Permissions.check(source.getPlayer(), "manhunt.runner") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .then(argument("targets", EntityArgumentType.players())
                        .executes(context -> setRunner(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                )
        );
    }

    private static int setRunner(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.getScoreboard().clearTeam(player.getNameForScoreboard());
            player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.role", Text.literal(player.getName().getString()).formatted(config.getRunnersColor()), Text.translatable("manhunt.runner").formatted(config.getRunnersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
