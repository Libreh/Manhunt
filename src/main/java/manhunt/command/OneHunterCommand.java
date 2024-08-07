package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OneHunterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("onehunter")
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME && source.isExecutedByPlayer() &&
                        ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.one_hunter") || !source.isExecutedByPlayer())
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setOneHunter(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneHunter(ServerCommandSource source, ServerPlayerEntity player) {
        for (ServerPlayerEntity serverPlayer : source.getServer().getPlayerManager().getPlayerList()) {
            serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), source.getServer().getScoreboard().getTeam("runners"));
        }

        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.config.getHuntersColor()),
                Text.translatable("role.manhunt.hunter").formatted(ManhuntConfig.config.getHuntersColor())),
                false
        );

        return Command.SINGLE_SUCCESS;
    }
}
