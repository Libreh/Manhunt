package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.MessageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TogglePauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("togglepause")
                .executes(context -> togglePause(context.getSource()))
        );
    }

    private static int togglePause(ServerCommandSource source) {
        if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
            if (ManhuntGame.gameState == ManhuntState.PLAYING) {
                MinecraftServer server = source.getServer();

                if (ManhuntGame.isPaused()) {
                    for (ServerPlayerEntity gamePlayer : server.getPlayerManager().getPlayerList()) {
                        gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                        gamePlayer.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);
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
                } else {
                    for (ServerPlayerEntity gamePlayer : server.getPlayerManager().getPlayerList()) {
                        gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                        gamePlayer.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, StatusEffectInstance.INFINITE, 255, false, true));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, StatusEffectInstance.INFINITE, 248, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, StatusEffectInstance.INFINITE, 255, false, false));
                        MessageUtil.showTitle(gamePlayer, "manhunt.title.paused", "manhunt.title.holdup");
                    }
                    MessageUtil.sendBroadcast("manhunt.chat.paused");
                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                    scheduledExecutorService.schedule(() -> ManhuntGame.setPaused(true), 1, TimeUnit.SECONDS);
                }
            } else {
                source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.pregame"), false);
            }
        } else {
            source.sendFeedback(() -> MessageUtil.ofVomponent(source.getPlayer(), "manhunt.chat.leader"), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
