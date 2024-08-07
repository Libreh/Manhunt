package manhunt.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import manhunt.ManhuntMod;
import manhunt.config.ManhuntConfig;
import manhunt.game.GameEvents;
import manhunt.game.GameState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class PauseCommand {
    public static final HashMap<UUID, Collection<StatusEffectInstance>> playerEffects = new HashMap<>();
    public static final HashMap<UUID, Vec3d> playerPos = new HashMap<>();
    public static final HashMap<UUID, Float> playerYaw = new HashMap<>();
    public static final HashMap<UUID, Float> playerPitch = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("pause")
                .requires(source -> ManhuntMod.gameState == GameState.PLAYING && !GameEvents.paused &&
                        (source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt.pause") || !source.isExecutedByPlayer() ||
                        ManhuntConfig.config.isRunnersCanPause() &&
                                source.getPlayer().isTeamPlayer(source.getPlayer().getScoreboard().getTeam("runners")))
                )
                .executes(context -> pauseCommand(context.getSource()))
        );
    }

    private static int pauseCommand(ServerCommandSource source) {
        pauseGame(source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    public static void pauseGame(MinecraftServer server) {
        GameEvents.paused = true;

        if (ManhuntConfig.config.getLeavePauseTime() != 0) {
            GameEvents.pauseTimeLeft = ManhuntConfig.config.getLeavePauseTime() * 60 * 20;
        }

        server.getTickManager().setFrozen(true);

        playerEffects.clear();
        playerPos.clear();
        playerYaw.clear();
        playerPitch.clear();
        GameEvents.playerFood.clear();
        GameEvents.playerSaturation.clear();

        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            serverPlayer.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);
            if (!serverPlayer.getStatusEffects().isEmpty()) {
                playerEffects.put(serverPlayer.getUuid(), serverPlayer.getStatusEffects());
            }
            playerPos.put(serverPlayer.getUuid(), serverPlayer.getPos());
            playerYaw.put(serverPlayer.getUuid(), serverPlayer.getYaw());
            playerPitch.put(serverPlayer.getUuid(), serverPlayer.getPitch());
            GameEvents.playerFood.put(serverPlayer.getUuid(), serverPlayer.getHungerManager().getFoodLevel());
            GameEvents.playerSaturation.put(serverPlayer.getUuid(), serverPlayer.getHungerManager().getSaturationLevel());
            GameEvents.playerExhaustion.put(serverPlayer.getUuid(), serverPlayer.getHungerManager().getExhaustion());
            serverPlayer.getHungerManager().setSaturationLevel(0.0F);
            serverPlayer.getHungerManager().setExhaustion(0.0F);
            serverPlayer.clearStatusEffects();
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0);
            serverPlayer.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(0);
            serverPlayer.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DARKNESS,
                    StatusEffectInstance.INFINITE,
                    255,
                    false,
                    false,
                    false)
            );
            serverPlayer.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.RESISTANCE,
                        StatusEffectInstance.INFINITE,
                        255,
                    false,
                    false,
                    false)
            );
        }
    }
}