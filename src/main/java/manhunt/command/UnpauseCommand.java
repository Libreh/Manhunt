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

import static manhunt.ManhuntMod.*;
import static net.minecraft.server.command.CommandManager.literal;

public class UnpauseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unpause")
                .requires(source -> source.isExecutedByPlayer() && getGameState() == GameState.PLAYING && (Permissions.check(source.getPlayer(), "manhunt.unpause") || (source.hasPermissionLevel(1) || source.hasPermissionLevel(2) || source.hasPermissionLevel(3) || source.hasPermissionLevel(4) || config.isRunnerCanPause())))
                .executes(context -> unpauseCommand(context.getSource()))
        );
    }

    private static int unpauseCommand(ServerCommandSource source) {
        if (isPaused()) {
            unpauseGame(source.getServer());
        } else {
            source.sendFeedback(() -> Text.translatable("manhunt.chat.already", Text.translatable("manhunt.unpaused")).formatted(Formatting.RED), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void unpauseGame(MinecraftServer server) {
        setPaused(false);

        server.getTickManager().setFrozen(false);

        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
            serverPlayer.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
            serverPlayer.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
            serverPlayer.clearStatusEffects();
            if (playerEffects.containsKey(serverPlayer.getUuid())) {
                for (StatusEffectInstance statusEffect : playerEffects.get(serverPlayer.getUuid())) {
                    serverPlayer.addStatusEffect(statusEffect);
                }
            }
            serverPlayer.getHungerManager().setFoodLevel(playerFood.get(serverPlayer.getUuid()));
            serverPlayer.getHungerManager().setSaturationLevel(playerSaturation.get(serverPlayer.getUuid()));
            serverPlayer.getHungerManager().setExhaustion(playerExhuastion.get(serverPlayer.getUuid()));
            serverPlayer.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getGameUnpausedTitle()).formatted(Formatting.YELLOW)));
            serverPlayer.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getGameUnpausedSubtitle()).formatted(Formatting.GOLD)));
        }
    }
}