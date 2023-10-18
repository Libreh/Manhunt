package manhunt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.Manhunt;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import net.minecraft.entity.attribute.EntityAttributes;
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

import static net.minecraft.server.command.CommandManager.literal;

public class UnpauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unpause")
                .executes(context -> attemptPause(context.getSource()))
        );
    }

    private static int attemptPause(ServerCommandSource source) {
        if (ManhuntGame.state == ManhuntState.PLAYING) {
            var player = source.getPlayer();

            if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
                if (Manhunt.isPaused()) {
                    for (ServerPlayerEntity gamePlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
                        gamePlayer.playSound(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.2f, 0.5f);
                        gamePlayer.removeStatusEffect(StatusEffects.BLINDNESS);
                        gamePlayer.removeStatusEffect(StatusEffects.JUMP_BOOST);
                        gamePlayer.removeStatusEffect(StatusEffects.MINING_FATIGUE);
                        gamePlayer.removeStatusEffect(StatusEffects.RESISTANCE);
                        gamePlayer.removeStatusEffect(StatusEffects.WEAKNESS);
                        gamePlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.unpaused")));
                        gamePlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.go")));
                    }
                    player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.unpaused"), false);
                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                    scheduledExecutorService.schedule(() -> Manhunt.setPaused(false), 1, TimeUnit.SECONDS);
                }
            } else {
                source.sendFeedback(() -> Text.translatable("manhunt.lore.game"), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
