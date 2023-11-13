package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.GameState;
import manhunt.Manhunt;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.Manhunt.gameState;
import static net.minecraft.server.command.CommandManager.literal;

public class PauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pause")
                .executes(context -> attemptPause(context.getSource()))
        );
    }

    private static int attemptPause(ServerCommandSource source) {
        if (gameState == GameState.PLAYING) {
            var player = source.getPlayer();

            if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
                if (!Manhunt.isPaused()) {
                    for (ServerPlayerEntity gamePlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                        gamePlayer.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.2f, 1.5f);
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, StatusEffectInstance.INFINITE, 255, false, true));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, StatusEffectInstance.INFINITE, 248, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, StatusEffectInstance.INFINITE, 255, false, false));
                        gamePlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.paused")));
                        gamePlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.holdup")));
                    }
                    player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.paused"), false);
                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                    scheduledExecutorService.schedule(() -> Manhunt.setPaused(true), 1, TimeUnit.SECONDS);
                }
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.lore.game"), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
