package libreh.manhunt.command.game;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import libreh.manhunt.ManhuntMod;
import libreh.manhunt.config.ManhuntConfig;
import libreh.manhunt.event.OnGameTick;
import libreh.manhunt.event.OnPlayerState;
import libreh.manhunt.game.GameState;
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
    public static final HashMap<UUID, Collection<StatusEffectInstance>> PLAYER_EFFECTS = new HashMap<>();
    public static final HashMap<UUID, Vec3d> PLAYER_POS = new HashMap<>();
    public static final HashMap<UUID, Float> PLAYER_YAW = new HashMap<>();
    public static final HashMap<UUID, Float> PLAYER_PITCH = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("pause")
                .requires(source -> ManhuntMod.gameState == GameState.PLAYING && !OnGameTick.paused && (source.isExecutedByPlayer() && ManhuntMod.checkLeaderPermission(source.getPlayer(), "manhunt" + ".pause") || !source.isExecutedByPlayer() || ManhuntConfig.CONFIG.isRunnersCanPause() && source.getPlayer().isTeamPlayer(source.getPlayer().getScoreboard().getTeam("runners"))))
                .executes(context -> pauseCommand(context.getSource())));
    }

    private static int pauseCommand(ServerCommandSource source) {
        pauseGame(source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    public static void pauseGame(MinecraftServer server) {
        OnGameTick.paused = true;

        if (ManhuntConfig.CONFIG.getLeavePauseTime() != 0) {
            OnGameTick.pauseTimeLeft = ManhuntConfig.CONFIG.getLeavePauseTime() * 60 * 20;
        }

        server.getTickManager().setFrozen(true);

        PLAYER_EFFECTS.clear();
        PLAYER_POS.clear();
        PLAYER_YAW.clear();
        PLAYER_PITCH.clear();
        OnPlayerState.PLAYER_FOOD.clear();
        OnPlayerState.PLAYER_SATURATION.clear();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);
            if (!player.getStatusEffects().isEmpty()) {
                PLAYER_EFFECTS.put(player.getUuid(), player.getStatusEffects());
            }
            PLAYER_POS.put(player.getUuid(), player.getPos());
            PLAYER_YAW.put(player.getUuid(), player.getYaw());
            PLAYER_PITCH.put(player.getUuid(), player.getPitch());
            OnPlayerState.PLAYER_FOOD.put(player.getUuid(), player.getHungerManager().getFoodLevel());
            OnPlayerState.PLAYER_SATURATION.put(player.getUuid(), player.getHungerManager().getSaturationLevel());
            OnPlayerState.PLAYER_AIR.put(player.getUuid(), player.getAir());
            var hungerManager = player.getHungerManager();
            hungerManager.setSaturationLevel(0.0F);
            player.clearStatusEffects();
            player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0);
            player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(0);
            player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED).setBaseValue(0);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, StatusEffectInstance.INFINITE,
                    255, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE,
                    255, false, false));
        }
    }
}