package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.playerdata.api.PlayerDataApi;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtByte;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static manhunt.config.ManhuntConfig.ALLOW_BED_EXPLOSIONS;
import static manhunt.config.ManhuntConfig.ALLOW_LAVA_PVP_IN_THE_NETHER;
import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

public class OneRunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("onerunner")
                .executes(context -> setOneRunnerAndAllHunters(context.getSource(), context.getSource().getPlayer()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setOneRunnerAndAllHunters(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneRunnerAndAllHunters(ServerCommandSource source, ServerPlayerEntity player) {
        if (gameState == PREGAME) {
            if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().clearTeam(serverPlayer.getName().getString());
                    serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getName().getString(), player.getScoreboard().getTeam("hunters"));
                }

                player.getScoreboard().clearTeam(player.getName().getString());
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("players"));
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));

                player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.onerunner", Text.literal(player.getName().getString()), Text.literal("Runner").formatted(Formatting.GREEN), Text.literal("Hunter").formatted(Formatting.RED)), false);

                if (PlayerDataApi.getGlobalDataFor(player, allowBedExplosions) == NbtByte.ZERO) {
                    ALLOW_BED_EXPLOSIONS.set(false);
                } else {
                    ALLOW_BED_EXPLOSIONS.set(true);
                }

                if (PlayerDataApi.getGlobalDataFor(player, allowLavaPvpInNether) == NbtByte.ZERO) {
                    ALLOW_LAVA_PVP_IN_THE_NETHER.set(false);
                } else {
                    ALLOW_LAVA_PVP_IN_THE_NETHER.set(true);
                }

                player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.preference.runner", Text.literal(player.getName().getString())).formatted(Formatting.GREEN), false);
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.onlyleader").formatted(Formatting.RED), false);
            }
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.playing").formatted(Formatting.RED), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame").formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
