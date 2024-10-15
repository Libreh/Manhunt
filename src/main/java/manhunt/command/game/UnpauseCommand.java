package manhunt.command.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.event.OnGameTick;
import manhunt.event.OnPlayerState;
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
                .requires(source -> ManhuntMod.gameState == GameState.PLAYING && OnGameTick.paused && (source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt" + ".unpause") || !source.isExecutedByPlayer() || ManhuntConfig.CONFIG.isRunnersCanPause() && source.getPlayer().isTeamPlayer(source.getPlayer().getScoreboard().getTeam("runners"))))
                .executes(context -> unpauseCommand(context.getSource()))
        );
    }

    private static int unpauseCommand(ServerCommandSource source) {
        unpauseGame(source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    public static void unpauseGame(MinecraftServer server) {
        OnGameTick.paused = false;

        server.getTickManager().setFrozen(false);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
            player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
            player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
            player.clearStatusEffects();
            if (PauseCommand.PLAYER_EFFECTS.containsKey(player.getUuid())) {
                for (StatusEffectInstance statusEffect : PauseCommand.PLAYER_EFFECTS.get(player.getUuid())) {
                    player.addStatusEffect(statusEffect);
                }
            }
            var hungerManager = player.getHungerManager();
            hungerManager.setFoodLevel(OnPlayerState.PLAYER_FOOD.get(player.getUuid()));
            hungerManager.setSaturationLevel(OnPlayerState.PLAYER_SATURATION.get(player.getUuid()));
            hungerManager.setExhaustion(OnPlayerState.PLAYER_EXHAUSTION.get(player.getUuid()));
            player.setAir(OnPlayerState.PLAYER_AIR.get(player.getUuid()));
        }
    }
}