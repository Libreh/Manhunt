package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.game.ManhuntGame.gameState;
import static net.minecraft.server.command.CommandManager.literal;

public class UnpauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unpause")
                .executes(context -> attemptPause(context.getSource()))
        );
    }

    private static int attemptPause(ServerCommandSource source) {
        if (gameState == ManhuntState.PLAYING) {
            var player = source.getPlayer();

            if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
                if (ManhuntGame.isPaused()) {
                    for (ServerPlayerEntity gamePlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
                        gamePlayer.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.2f, 0.5f);
                        gamePlayer.removeStatusEffect(StatusEffects.BLINDNESS);
                        gamePlayer.removeStatusEffect(StatusEffects.JUMP_BOOST);
                        gamePlayer.removeStatusEffect(StatusEffects.MINING_FATIGUE);
                        gamePlayer.removeStatusEffect(StatusEffects.RESISTANCE);
                        gamePlayer.removeStatusEffect(StatusEffects.WEAKNESS);
                        MessageUtil.showTitle(gamePlayer, "manhunt.title.unpaused", "manhunt.title.go");
                    }
                    MessageUtil.sendBroadcast("manhunt.chat.unpaused");
                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                    scheduledExecutorService.schedule(() -> ManhuntGame.setPaused(false), 1, TimeUnit.SECONDS);
                }
            } else {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.lore.game"), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
