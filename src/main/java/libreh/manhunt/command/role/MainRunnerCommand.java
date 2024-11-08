package libreh.manhunt.command.role;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import libreh.manhunt.ManhuntMod;
import libreh.manhunt.config.ManhuntConfig;
import libreh.manhunt.config.gui.GameOptionsGui;
import libreh.manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MainRunnerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mainrunner")
                .requires(source -> ManhuntMod.gameState == GameState.PREGAME && source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "libreh.manhunt.main_runner") || !source.isExecutedByPlayer())
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setMainRunner(EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setMainRunner(ServerPlayerEntity player) {
        var server = player.getServer();
        var scoreboard = server.getScoreboard();
        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            scoreboard.addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), scoreboard.getTeam("hunters"));
        }
        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("runners"));
        GameOptionsGui.mainRunnerUUID = player.getUuid();

        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.one_role",
                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor()),
                Text.translatable("role.manhunt.runner").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);

        return Command.SINGLE_SUCCESS;
    }
}