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

public class OneHunterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("onehunter")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME && ManhuntMod.checkPermission(source.getPlayer(), "manhunt.onehunter"))
                .then(argument("player", EntityArgumentType.player())
                        .executes(context -> setOneHunter(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneHunter(ServerCommandSource source, ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
            serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
        }

        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.onerunner", Text.literal(player.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("manhunt.hunter").formatted(config.getHuntersColor())), false);

        return Command.SINGLE_SUCCESS;
    }
}
