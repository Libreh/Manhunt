package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.playerdata.api.PlayerDataApi;
import manhunt.config.Configs;
import manhunt.util.MessageUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtByte;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static manhunt.game.ManhuntGame.*;
import static manhunt.game.ManhuntState.PLAYING;
import static manhunt.game.ManhuntState.PREGAME;

public class RunnerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("runner")
                .executes(context -> setOneselfRunner(context.getSource()))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> setSomeoneRunner(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                )
        );
    }

    private static int setOneselfRunner(ServerCommandSource source) {
        if (gameState == PREGAME) {
            ServerPlayerEntity player = source.getPlayer();

            player.getScoreboard().clearTeam(player.getName().getString());
            player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));

            MessageUtil.sendBroadcast("manhunt.chat.role.runner", player.getName().getString());

            if (PlayerDataApi.getGlobalDataFor(player, bedExplosionDamagePreference).equals(NbtByte.ZERO)) {
                settings.bedExplosionDamage = false;
                Configs.configHandler.saveToDisk();
            } else {
                settings.bedExplosionDamage = true;
                Configs.configHandler.saveToDisk();
            }

            if (PlayerDataApi.getGlobalDataFor(player, lavaPvpInTheNetherPreference).equals(NbtByte.ZERO)) {
                settings.lavaPvpInTheNether = false;
                Configs.configHandler.saveToDisk();
            } else {
                settings.lavaPvpInTheNether = true;
                Configs.configHandler.saveToDisk();
            }

            MessageUtil.sendBroadcast("manhunt.chat.preferences", player.getName().getString());
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setSomeoneRunner(ServerCommandSource source, ServerPlayerEntity player) {
        if (gameState == PREGAME) {
            if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                player.getScoreboard().clearTeam(player.getName().getString());
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));

                MessageUtil.sendBroadcast("manhunt.chat.role.runner", player.getName().getString());

                if (PlayerDataApi.getGlobalDataFor(player, bedExplosionDamagePreference).equals(NbtByte.ZERO)) {
                    settings.bedExplosionDamage = false;
                    Configs.configHandler.saveToDisk();
                } else {
                    settings.bedExplosionDamage = true;
                    Configs.configHandler.saveToDisk();
                }

                if (PlayerDataApi.getGlobalDataFor(player, lavaPvpInTheNetherPreference).equals(NbtByte.ZERO)) {
                    settings.lavaPvpInTheNether = false;
                    Configs.configHandler.saveToDisk();
                } else {
                    settings.lavaPvpInTheNether = true;
                    Configs.configHandler.saveToDisk();
                }

                MessageUtil.sendBroadcast("manhunt.chat.preferences", player.getName().getString());
            } else {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.leader"), false);
            }
        } else if (gameState == PLAYING) {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.playing"), false);
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.postgame"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
