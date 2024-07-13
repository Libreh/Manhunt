package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static manhunt.ManhuntMod.config;

public class HunterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hunter")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.state == GameState.PREGAME && config.getTeamPreset() == 1)
                .executes(context -> setHunter(context.getSource(), context.getSource().getPlayer()))
                .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .requires(source -> ManhuntMod.checkPermission(source.getPlayer(), "manhunt.hunter"))
                        .executes(context -> setHunters(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                )
        );
    }

    private static int setHunter(ServerCommandSource source, ServerPlayerEntity player) {
        if (!player.getScoreboard().getTeam("hunters").getPlayerList().contains(player.getNameForScoreboard())) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("chat.joined_team", Text.literal(player.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("role.manhunt.hunters").formatted(config.getHuntersColor())), false);
        } else {
            source.sendFeedback(() -> Text.translatable("chat.already_team", Text.translatable("role.manhunt.hunter")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setHunters(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("chat.set_role", Text.literal(player.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("role.manhunt.hunter").formatted(config.getHuntersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
