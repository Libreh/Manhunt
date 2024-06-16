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

public class HunterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("hunter")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME)
                .executes(context -> setHunter(context.getSource(), context.getSource().getPlayer()))
                .requires(source -> ManhuntMod.checkPermission(source.getPlayer(), "manhunt.hunter"))
                .then(argument("targets", EntityArgumentType.players())
                        .executes(context -> setHunters(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                )
        );
    }

    private static int setHunter(ServerCommandSource source, ServerPlayerEntity player) {
        if (!player.getScoreboard().getTeam("hunters").getPlayerList().contains(player.getNameForScoreboard())) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.joined_team", Text.literal(player.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("manhunt.role.hunters").formatted(config.getHuntersColor())), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.already_team", Text.translatable("manhunt.role.hunter").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setHunters(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

            player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.set_role", Text.literal(player.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("manhunt.role.hunter").formatted(config.getHuntersColor())), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
