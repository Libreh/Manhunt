package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.game.GameState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import static manhunt.ManhuntMod.*;

public class UnpauseCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unpause")
                .requires(source -> source.isExecutedByPlayer() && state == GameState.PLAYING && paused && checkPermission(source.getPlayer(), "manhunt.unpause") || config.isRunnersCanPause() && source.getPlayer().getScoreboard().getTeam("runners").getPlayerList().contains(source.getPlayer().getNameForScoreboard()))
                .executes(context -> unpauseCommand(context.getSource()))
        );
    }

    private static int unpauseCommand(ServerCommandSource source) {
        unpauseGame(source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    public static void unpauseGame(MinecraftServer server) {
        paused = false;

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
        }
    }
}