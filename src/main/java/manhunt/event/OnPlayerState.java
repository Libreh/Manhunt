package manhunt.event;

import manhunt.command.game.PauseCommand;
import manhunt.command.game.UnpauseCommand;
import manhunt.config.ManhuntConfig;
import manhunt.config.gui.ConfigGui;
import manhunt.config.gui.SettingsGui;
import manhunt.game.GameState;
import manhunt.game.ManhuntGame;
import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.types.JavaTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.GameMode;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static manhunt.ManhuntMod.*;

public class OnPlayerState {
    public static final HashMap<UUID, Integer> PLAYER_FOOD = new HashMap<>();
    public static final HashMap<UUID, Float> PLAYER_SATURATION = new HashMap<>();
    public static final HashMap<UUID, Float> PLAYER_EXHAUSTION = new HashMap<>();
    public static final HashMap<UUID, Integer> PLAYER_AIR = new HashMap<>();

    public static void playerRespawn(ServerPlayerEntity player) {
        OnGameTick.JOIN_LIST.add(player.getUuid());
        if (gameState == GameState.PLAYING) {
            var scoreboard = player.getScoreboard();
            if (player.isTeamPlayer(scoreboard.getTeam("runners"))) {
                if (ManhuntConfig.CONFIG.isHuntOnDeath()) {
                    if (scoreboard.getTeam("runners").getPlayerList().size() != 1) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("hunters"));
                    }
                } else {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }

    public static void playerJoin(ServerPlayNetworkHandler handler) {
        var player = handler.player;
        var server = player.getServer();
        var scoreboard = server.getScoreboard();
        OnGameTick.JOIN_LIST.add(player.getUuid());
        ConfigGui.SLOW_DOWN_MANAGER.putIfAbsent(player.getUuid(), 0);
        if (gameState == GameState.PREGAME) {
            player.setSpawnPoint(LOBBY_REGISTRY_KEY, OnGameTick.LOBBY_SPAWN_INT, 180f, true, false);
        } else if (gameState == GameState.PLAYING) {
            if (!ManhuntGame.PLAY_LIST.contains(player.getUuid())) {
                ManhuntGame.PLAY_LIST.add(player.getUuid());
            }
            if (OnGameTick.paused) {
                if (player.isTeamPlayer(scoreboard.getTeam("runners")) && scoreboard.getTeam("runners").getPlayerList().size() == 1) {
                    UnpauseCommand.unpauseGame(server);
                } else if (!OnGameTick.LEFT_ON_PAUSE.contains(player.getUuid())) {
                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5F);
                    if (!player.getStatusEffects().isEmpty()) {
                        PauseCommand.PLAYER_EFFECTS.put(player.getUuid(), player.getStatusEffects());
                    }
                    PauseCommand.PLAYER_POS.put(player.getUuid(), player.getPos());
                    PauseCommand.PLAYER_YAW.put(player.getUuid(), player.getYaw());
                    PauseCommand.PLAYER_PITCH.put(player.getUuid(), player.getPitch());
                    PLAYER_FOOD.put(player.getUuid(), player.getHungerManager().getFoodLevel());
                    PLAYER_SATURATION.put(player.getUuid(), player.getHungerManager().getSaturationLevel());
                    PLAYER_EXHAUSTION.put(player.getUuid(), player.getHungerManager().getExhaustion());
                    PLAYER_AIR.put(player.getUuid(), player.getAir());
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(0);
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS,
                            StatusEffectInstance.INFINITE, 255, false, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,
                            StatusEffectInstance.INFINITE, 255, false, false, false));
                }
            } else {
                if (OnGameTick.LEFT_ON_PAUSE.contains(player.getUuid())) {
                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                }
            }
        }

        SettingsGui.CUSTOM_TITLES.putIfAbsent(player.getUuid(), ManhuntConfig.CONFIG.isCustomTitlesDefault());
        SettingsGui.CUSTOM_SOUNDS.putIfAbsent(player.getUuid(), ManhuntConfig.CONFIG.isCustomSoundsDefault());
        SettingsGui.CUSTOM_PARTICLES.putIfAbsent(player.getUuid(),
                ManhuntConfig.CONFIG.isCustomParticlesDefault());
        SettingsGui.TRACKER_TYPE.putIfAbsent(player.getUuid(), 4);
        SettingsGui.NIGHT_VISION.putIfAbsent(player.getUuid(), false);
        SettingsGui.FRIENDLY_FIRE.putIfAbsent(player.getUuid(), true);
        SettingsGui.BED_EXPLOSIONS.putIfAbsent(player.getUuid(), ManhuntConfig.CONFIG.isBedExplosionsDefault());
        SettingsGui.LAVA_PVP_IN_NETHER.putIfAbsent(player.getUuid(),
                ManhuntConfig.CONFIG.isLavaPvpInNetherDefault());

        if (!SettingsGui.CUSTOM_TITLES.containsKey(player.getUuid())) {
            Optional<DataContainer> dataContainer = SETTINGS.getContainer("uuid", player.getUuid());
            if (dataContainer.isEmpty()) return;

            SettingsGui.CUSTOM_TITLES.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "custom_titles"));
            SettingsGui.CUSTOM_SOUNDS.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "custom_sounds"));
            SettingsGui.CUSTOM_PARTICLES.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "custom_particles"));
            SettingsGui.TRACKER_TYPE.put(player.getUuid(), dataContainer.get().get(JavaTypes.INT,
                    "tracker_type"));
            SettingsGui.NIGHT_VISION.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "night_vision"));
            SettingsGui.FRIENDLY_FIRE.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "friendly_fire"));
            SettingsGui.BED_EXPLOSIONS.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "bed_explosions"));
            SettingsGui.LAVA_PVP_IN_NETHER.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "lava_pvp_in_nether"));
        }
    }

    public static void playerLeave(ServerPlayNetworkHandler handler) {
        var player = handler.player;
        var scoreboard = player.getScoreboard();
        var runnersTeam = scoreboard.getTeam("runners");
        boolean isRunner = player.isTeamPlayer(runnersTeam);

        if (gameState == GameState.PREGAME) {
            if (isRunner && runnersTeam.getPlayerList().size() == 1) {
                OnGameTick.starting = false;
                OnGameTick.startingTime = 0;
                OnGameTick.startReset = false;
                runnersTeam.getPlayerList().remove(player.getNameForScoreboard());
            }
        } else if (gameState == GameState.PLAYING) {
            if (OnGameTick.paused) {
                OnGameTick.LEFT_ON_PAUSE.add(player.getUuid());
            } else {
                if (isRunner && runnersTeam.getPlayerList().size() == 1) {
                    OnGameTick.LEFT_ON_PAUSE.add(player.getUuid());
                    PauseCommand.pauseGame(player.getServer());
                }
            }
        }

        var dataContainer = SETTINGS.getOrCreateContainer("uuid", player.getUuid());

        dataContainer.transaction().put(JavaTypes.UUID, "uuid", player.getUuid()).put(JavaTypes.BOOL, "custom_titles"
                , SettingsGui.CUSTOM_TITLES.get(player.getUuid())).put(JavaTypes.BOOL, "custom_sounds",
                SettingsGui.CUSTOM_SOUNDS.get(player.getUuid())).put(JavaTypes.BOOL, "custom_particles",
                SettingsGui.CUSTOM_PARTICLES.get(player.getUuid())).put(JavaTypes.INT, "tracker_type",
                SettingsGui.TRACKER_TYPE.get(player.getUuid())).put(JavaTypes.BOOL, "night_vision",
                SettingsGui.NIGHT_VISION.get(player.getUuid())).put(JavaTypes.BOOL, "friendly_fire",
                SettingsGui.FRIENDLY_FIRE.get(player.getUuid())).put(JavaTypes.BOOL, "bed_explosions",
                SettingsGui.BED_EXPLOSIONS.get(player.getUuid())).put(JavaTypes.BOOL, "lava_pvp_in_nether",
                SettingsGui.LAVA_PVP_IN_NETHER.get(player.getUuid())).commit();
    }
}
