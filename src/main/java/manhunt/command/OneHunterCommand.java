package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static manhunt.ManhuntMod.config;
import static manhunt.ManhuntMod.getGameState;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OneHunterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("onehunter")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME && (Permissions.check(source.getPlayer(), "manhunt.onehunter") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> setOneHunter(context.getSource(), context.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player())
                        .executes(context -> setOneHunter(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneHunter(ServerCommandSource source, ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            serverPlayer.getScoreboard().clearTeam(serverPlayer.getNameForScoreboard());
            serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getName().getString(), player.getScoreboard().getTeam("runners"));
        }

        player.getScoreboard().clearTeam(player.getNameForScoreboard());
        player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.onerunner", Text.literal(player.getName().getString()).formatted(config.getHuntersColor()), Text.translatable("manhunt.hunter").formatted(config.getHuntersColor())), false);

        return Command.SINGLE_SUCCESS;
    }
}
