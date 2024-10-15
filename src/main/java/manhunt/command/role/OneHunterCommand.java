package manhunt.command.role;

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
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME && source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.one_hunter") || !source.isExecutedByPlayer())
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setOneHunter(EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneHunter(ServerPlayerEntity player) {
        var server = player.getServer();
        var scoreboard = server.getScoreboard();
        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            scoreboard.addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), scoreboard.getTeam("runners"));
        }
        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("hunters"));

        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getHuntersColor()),
                Text.translatable("role.manhunt.hunter").formatted(ManhuntConfig.CONFIG.getHuntersColor())), false);

        return Command.SINGLE_SUCCESS;
    }
}
