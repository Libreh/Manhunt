package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.ManhuntState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.game.ManhuntGame.*;

public class TogglePauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("togglepause")
                .executes(context -> togglePause(context.getSource()))
        );
    }

    private static int togglePause(ServerCommandSource source) {
        if (gameState == ManhuntState.PLAYING) {
            if (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4)) {
                MinecraftServer server = source.getServer();

                if (isPaused()) {
                    for (ServerPlayerEntity gamePlayer : server.getPlayerManager().getPlayerList()) {
                        gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                        gamePlayer.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);
                        gamePlayer.removeStatusEffect(StatusEffects.BLINDNESS);
                        gamePlayer.removeStatusEffect(StatusEffects.JUMP_BOOST);
                        gamePlayer.removeStatusEffect(StatusEffects.MINING_FATIGUE);
                        gamePlayer.removeStatusEffect(StatusEffects.RESISTANCE);
                        gamePlayer.removeStatusEffect(StatusEffects.WEAKNESS);
                        gamePlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.unpaused").formatted(Formatting.YELLOW)));
                        gamePlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.go").formatted(Formatting.GOLD)));
                    }
                    server.getPlayerManager().broadcast(Text.translatable("manhunt.chat.unpaused"), false);

                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                    scheduledExecutorService.schedule(() -> setPaused(false), 1, TimeUnit.SECONDS);
                } else {
                    for (ServerPlayerEntity gamePlayer : server.getPlayerManager().getPlayerList()) {
                        gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                        gamePlayer.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, StatusEffectInstance.INFINITE, 255, false, true));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, StatusEffectInstance.INFINITE, 248, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.paused").formatted(Formatting.YELLOW)));
                        gamePlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.holdup").formatted(Formatting.GOLD)));
                    }
                    server.getPlayerManager().broadcast(Text.translatable("manhunt.chat.paused"), false);

                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                    scheduledExecutorService.schedule(() -> setPaused(true), 1, TimeUnit.SECONDS);
                }
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.chat.onlyleader").formatted(Formatting.RED).formatted(Formatting.RED), false);
            }
        } else if (gameState == ManhuntState.POSTGAME) {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.pregame").formatted(Formatting.RED), false);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.postgame").formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
