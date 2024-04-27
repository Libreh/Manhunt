package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CoordsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("coords")
                .requires(source -> ManhuntMod.getGameState() == GameState.PLAYING)
                .executes(context -> sendCoords(context.getSource()))
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(context -> sendCoordsMessage(context.getSource(), StringArgumentType.getString(context, "message")))
                )
        );
    }

    private static int sendCoords(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();

        if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
            for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(Text.translatable("manhunt.chat.coords", Text.literal("HUNTERS").formatted(Formatting.RED), player.getName().getString().formatted(Formatting.RED),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ())));
            }
            ManhuntMod.hunterCoords.add(Text.translatable("manhunt.chat.coords", Text.literal("HUNTERS").formatted(Formatting.RED), player.getName().getString().formatted(Formatting.RED),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ())));
        } else {
            for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(Text.translatable("manhunt.chat.coords", Text.literal("RUNNERS").formatted(Formatting.GREEN), player.getName().getString().formatted(Formatting.GREEN),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ())));
            }
            ManhuntMod.runnerCoords.add(Text.translatable("manhunt.chat.coords", Text.literal("RUNNERS").formatted(Formatting.GREEN), player.getName().getString().formatted(Formatting.GREEN),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ())));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int sendCoordsMessage(ServerCommandSource source, String message) {
        ServerPlayerEntity player = source.getPlayer();

        if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
            for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(Text.translatable("manhunt.chat.coords", Text.literal("HUNTERS").formatted(Formatting.RED), player.getName().getString().formatted(Formatting.RED),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ()), message));
            }
            ManhuntMod.hunterCoords.add(Text.translatable("manhunt.chat.coords", Text.literal("HUNTERS").formatted(Formatting.RED), player.getName().getString().formatted(Formatting.RED),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ()), message));
        } else {
            for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                serverPlayer.sendMessage(Text.translatable("manhunt.chat.coords", Text.literal("RUNNERS").formatted(Formatting.GREEN), player.getName().getString().formatted(Formatting.GREEN),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ()), message));
            }
            ManhuntMod.runnerCoords.add(Text.translatable("manhunt.chat.coords", Text.literal("RUNNERS").formatted(Formatting.GREEN), player.getName().getString().formatted(Formatting.GREEN),  Math.floor(player.getPos().getX()),  Math.floor(player.getPos().getY()),  Math.floor(player.getPos().getZ()), message));
        }

        return Command.SINGLE_SUCCESS;
    }
}
