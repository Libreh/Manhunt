package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.game.GameState;
import net.minecraft.entity.attribute.EntityAttributes;
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

import static manhunt.ManhuntMod.*;
import static net.minecraft.server.command.CommandManager.literal;

public class PauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("pause")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PLAYING && ManhuntMod.checkPermission(source.getPlayer(), "manhunt.pause") || config.isRunnersCanPause() && source.getPlayer().getScoreboard().getTeam("runners").getPlayerList().contains(source.getPlayer().getNameForScoreboard()))
                .executes(context -> pauseCommand(context.getSource()))
        );
    }

    private static int pauseCommand(ServerCommandSource source) {
        if (!isPaused()) {
            pauseGame(source.getServer());
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.game_is_already", Text.translatable("manhunt.pause.paused")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void pauseGame(MinecraftServer server) {
        setPaused(true);

        server.getTickManager().setFrozen(true);

        playerEffects.clear();
        playerPos.clear();
        playerYaw.clear();
        playerPitch.clear();
        playerFood.clear();
        playerSaturation.clear();

        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            serverPlayer.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);
            if (!serverPlayer.getStatusEffects().isEmpty()) {
                playerEffects.put(serverPlayer.getUuid(), serverPlayer.getStatusEffects());
            }
            playerPos.put(serverPlayer.getUuid(), serverPlayer.getPos());
            playerYaw.put(serverPlayer.getUuid(), serverPlayer.getYaw());
            playerPitch.put(serverPlayer.getUuid(), serverPlayer.getPitch());
            playerFood.put(serverPlayer.getUuid(), serverPlayer.getHungerManager().getFoodLevel());
            playerSaturation.put(serverPlayer.getUuid(), serverPlayer.getHungerManager().getSaturationLevel());
            playerExhuastion.put(serverPlayer.getUuid(), serverPlayer.getHungerManager().getExhaustion());
            serverPlayer.getHungerManager().setSaturationLevel(0.0F);
            serverPlayer.getHungerManager().setExhaustion(0.0F);
            serverPlayer.clearStatusEffects();
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0);
            serverPlayer.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(0);
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, StatusEffectInstance.INFINITE, 255, false, false,false));
            serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
            serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getGamePausedTitle()).formatted(Formatting.YELLOW)));
            serverPlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getGamePausedSubtitle()).formatted(Formatting.GOLD)));
        }
    }
}