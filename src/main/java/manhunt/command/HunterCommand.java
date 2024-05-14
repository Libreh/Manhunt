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

public class HunterCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("hunter")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PREGAME && (Permissions.check(source.getPlayer(), "manhunt.hunter") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> setHunter(context.getSource(), context.getSource().getPlayer()))
                .then(argument("hunter", EntityArgumentType.player())
                        .executes(context -> setHunter(context.getSource(), EntityArgumentType.getPlayer(context, "hunter")))
                )
        );
    }

    private static int setHunter(ServerCommandSource source, ServerPlayerEntity player) {
        if (player.getScoreboard().getTeam("runners").getPlayerList().contains(player.getName().getString())) {
            player.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));
        }

        player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));

        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.role", Text.literal(player.getName().getString()).formatted(Formatting.RED), Text.translatable("manhunt.hunter").formatted(Formatting.RED)), false);

        return Command.SINGLE_SUCCESS;
    }
}
