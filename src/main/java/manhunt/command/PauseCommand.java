package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
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

import static net.minecraft.server.command.CommandManager.literal;

public class PauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pause")
                .requires(source -> source.isExecutedByPlayer() && ManhuntMod.getGameState() == GameState.PLAYING && (Permissions.check(source.getPlayer(), "manhunt.pause") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4))))
                .executes(context -> pauseGame(context.getSource()))
        );
    }

    private static int pauseGame(ServerCommandSource source) {
        MinecraftServer server = source.getServer();

        if (!ManhuntMod.isPaused()) {
            for (ServerPlayerEntity gamePlayer : server.getPlayerManager().getPlayerList()) {
                gamePlayer.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);
                ManhuntMod.playerEffects.put(gamePlayer, gamePlayer.getStatusEffects());
                gamePlayer.clearStatusEffects();
                gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, StatusEffectInstance.INFINITE, 255, false, true));
                gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, StatusEffectInstance.INFINITE, 255, false, true));
                gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, StatusEffectInstance.INFINITE, 248, false, false));
                gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, StatusEffectInstance.INFINITE, 255, false, false));
                gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false));
                gamePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, StatusEffectInstance.INFINITE, 255, false, false));
                gamePlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.gameis.paused").formatted(Formatting.YELLOW)));
                gamePlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.paused").formatted(Formatting.GOLD)));
            }

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.schedule(() -> ManhuntMod.setPaused(true), 500, TimeUnit.MILLISECONDS);
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.already", Text.translatable("manhunt.paused")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}