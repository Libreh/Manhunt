package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static manhunt.ManhuntMod.*;
import static net.minecraft.server.command.CommandManager.literal;

public class UnpauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unpause")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PLAYING && (Permissions.check(source.getPlayer(), "manhunt.unpause") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> unpauseGame(context.getSource()))
        );
    }

    private static int unpauseGame(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        if (isPaused()) {
            for (ServerPlayerEntity gamePlayer : server.getPlayerManager().getPlayerList()) {
                gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                gamePlayer.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                gamePlayer.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                gamePlayer.clearStatusEffects();
                for (StatusEffectInstance statusEffect : playerEffects.get(gamePlayer)) {
                    gamePlayer.addStatusEffect(statusEffect);
                }
                gamePlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getGameUnpausedTitle()).formatted(Formatting.YELLOW)));
                gamePlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getGameUnpausedSubtitle()).formatted(Formatting.GOLD)));
            }

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(() -> setPaused(false), 500, TimeUnit.MILLISECONDS);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.already", Text.translatable("manhunt.unpaused")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}