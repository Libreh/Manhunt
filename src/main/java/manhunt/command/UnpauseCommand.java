package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class UnpauseCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("unpause")
                .requires(source -> ManhuntMod.gameState == GameState.PLAYING && GameEvents.paused &&
                        (source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.unpause") || !source.isExecutedByPlayer() ||
                        ManhuntConfig.config.isRunnersCanPause() &&
                                source.getPlayer().isTeamPlayer(source.getPlayer().getScoreboard().getTeam("runners")))
                )
                .executes(context -> unpauseCommand(context.getSource()))
        );
    }

    private static int unpauseCommand(ServerCommandSource source) {
        unpauseGame(source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    public static void unpauseGame(MinecraftServer server) {
        GameEvents.paused = false;

        server.getTickManager().setFrozen(false);

        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
            serverPlayer.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
            serverPlayer.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
            serverPlayer.clearStatusEffects();
            if (PauseCommand.playerEffects.containsKey(serverPlayer.getUuid())) {
                for (StatusEffectInstance statusEffect : PauseCommand.playerEffects.get(serverPlayer.getUuid())) {
                    serverPlayer.addStatusEffect(statusEffect);
                }
            }
            var hungerManager = serverPlayer.getHungerManager();
            hungerManager.setFoodLevel(GameEvents.playerFood.get(serverPlayer.getUuid()));
            hungerManager.setSaturationLevel(GameEvents.playerSaturation.get(serverPlayer.getUuid()));
            hungerManager.setExhaustion(GameEvents.playerExhaustion.get(serverPlayer.getUuid()));
        }
    }
}