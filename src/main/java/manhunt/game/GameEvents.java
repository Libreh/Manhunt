package manhunt.game;

import manhunt.ManhuntMod;
import manhunt.command.PauseCommand;
import manhunt.command.ResetCommand;
import manhunt.command.UnpauseCommand;
import manhunt.config.ManhuntConfig;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.types.JavaTypes;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Block;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.*;

public class GameEvents {
    public static final HashMap<UUID, Integer> playerFood = new HashMap<>();
    public static final HashMap<UUID, Float> playerSaturation = new HashMap<>();
    public static final HashMap<UUID, Float> playerExhaustion = new HashMap<>();
    public static final HashMap<UUID, Integer> parkourTimer = new HashMap<>();
    public static final HashMap<UUID, Boolean> startedParkour = new HashMap<>();
    public static final HashMap<UUID, Boolean> finishedParkour = new HashMap<>();
    public static final List<UUID> allReadyUps = new ArrayList<>();
    public static final List<UUID> joinList = new ArrayList<>();
    public static final List<UUID> startList = new ArrayList<>();
    public static final List<UUID> resetList = new ArrayList<>();
    public static final List<UUID> leftOnPause = new ArrayList<>();
    public static final HashMap<UUID, BlockPos> playerSpawnPos = new HashMap<>();
    public static final HashMap<UUID, BlockPos> headStartPos = new HashMap<>();
    public static final BlockPos lobbySpawnPos = new BlockPos(0, 64, 0);
    public static final Vec3d lobbySpawn = new Vec3d(0.5, 64, 0.5);
    public static boolean starting = false;
    public static boolean headStart = false;
    public static boolean timeLimit = false;
    public static boolean paused = false;
    public static int startingTime = 0;
    public static int headStartTime = 0;
    public static int timeLimitLeft = 0;
    public static int pauseTimeLeft = 0;
    public static List<ServerPlayerEntity> allRunners;
    public static List<ServerPlayerEntity> allHunters;
    public static boolean headStartCountdown = false;
    public static int count;
    public static boolean startReset = true;

    public static void serverStart(MinecraftServer server) {
        ManhuntMod.gameState = GameState.PREGAME;

        if (ManhuntConfig.config.isSetMotd()) {
            server.setMotd(ManhuntMod.gameState.getColor() + "[" + ManhuntMod.gameState.getMotd() + "]§f Minecraft MANHUNT");
        }

        GameRules gameRules = server.getGameRules();
        gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        gameRules.get(GameRules.DO_FIRE_TICK).set(false, server);
        gameRules.get(GameRules.DO_INSOMNIA).set(false, server);
        gameRules.get(GameRules.DO_MOB_LOOT).set(false, server);
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(false, server);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        gameRules.get(GameRules.FALL_DAMAGE).set(false, server);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(0, server);
        gameRules.get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        gameRules.get(GameRules.FALL_DAMAGE).set(false, server);
        gameRules.get(GameRules.SPAWN_RADIUS).set(0, server);
        gameRules.get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(false, server);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);

        Scoreboard scoreboard = server.getScoreboard();

        if (scoreboard.getTeam("hunters") != null) scoreboard.removeTeam(scoreboard.getTeam("hunters"));
        scoreboard.addTeam("hunters");
        Team huntersTeam = scoreboard.getTeam("hunters");
        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        huntersTeam.setShowFriendlyInvisibles(true);

        if (scoreboard.getTeam("runners") != null) scoreboard.removeTeam(scoreboard.getTeam("runners"));
        scoreboard.addTeam("runners");
        Team runnersTeam = scoreboard.getTeam("runners");
        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        runnersTeam.setShowFriendlyInvisibles(true);

        if (ManhuntConfig.config.isTeamColor()) {
            huntersTeam.setColor(ManhuntConfig.config.getHuntersColor());
            runnersTeam.setColor(ManhuntConfig.config.getRunnersColor());
        } else {
            huntersTeam.setColor(Formatting.RESET);
            runnersTeam.setColor(Formatting.RESET);
        }

        try {
            spawnLobby(server);
        } catch (IOException e) {
            ManhuntMod.LOGGER.error("Failed to spawn Manhunt mod lobby");
        }
    }

    public static void serverTick(MinecraftServer server) {
        var huntersTeam = server.getScoreboard().getTeam("hunters");
        var runnersTeam = server.getScoreboard().getTeam("runners");
        if (ManhuntMod.gameState == GameState.PREGAME) {
            if (!startReset) {
                boolean reset = false;
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (player != null && player.getWorld().getRegistryKey() != ManhuntMod.lobbyWorldRegistryKey) {
                        reset = true;
                        break;
                    }
                }
                if (!reset) {
                    ManhuntMod.loadManhuntWorlds(server, ResetCommand.seed);
                    startReset = true;
                }
            }
            count++;
            if (count == 19) {
                if (starting && !paused) {
                    startingTime = startingTime - 20;
                    if (startingTime >= 20) {
                        int seconds = (int) Math.floor((double) startingTime % (20 * 60) / (20));
                        int color;
                        float pitch = 0.0F;
                        if (seconds > 5) {
                            color = 5766999;
                        } else {
                            pitch = 1.0F;
                            if (seconds == 4) {
                                pitch = 0.9F;
                                color = 9687040;
                            } else if (seconds == 3) {
                                color = 11378688;
                                pitch = 0.8F;
                            } else if (seconds == 2) {
                                color = 11955456;
                                pitch = 0.7F;
                            } else if (seconds == 1) {
                                color = 11547648;
                                pitch = 0.5F;
                            } else {
                                color = 5766999;
                            }
                        }

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
                            player.networkHandler.sendPacket(
                                    new TitleS2CPacket(
                                            Text.translatable("title.manhunt.starting_in",
                                                    Text.literal(seconds + " seconds").styled(style ->
                                                            style.withColor(color))
                                            ).styled(style -> style.withColor(5766999)
                                            )
                                    )
                            );
                            if (pitch != 0.0F) {
                                player.playSoundToPlayer(
                                        SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(),
                                        SoundCategory.MASTER,
                                        0.5F,
                                        pitch
                                );
                                if (pitch == 0.5F) {
                                    player.playSoundToPlayer(
                                            SoundEvents.BLOCK_PORTAL_TRAVEL,
                                            SoundCategory.MASTER,
                                            0.1F,
                                            pitch
                                    );
                                }
                            }

                        }
                    } else {
                        starting = false;
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 20, 5));
                            player.networkHandler.sendPacket(
                                    new TitleS2CPacket(
                                            Text.translatable("title.manhunt.teleporting").styled(style -> style.withColor(11686066))
                                    )
                            );
                        }
                        ManhuntGame.start(server);
                    }
                }
                count = 0;
            }
        } else if (ManhuntMod.gameState == GameState.PLAYING) {
            allRunners = new LinkedList<>();
            allHunters = new LinkedList<>();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != null && player.getScoreboardTeam() != null) {
                    if (player.isTeamPlayer(runnersTeam)) {
                        allRunners.add(player);
                    } else {
                        allHunters.add(player);
                    }
                }
            }

            count++;
            if (count == 19) {
                if (headStart && !paused) {
                    if (!headStartCountdown) {
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.sendMessage(Text.translatable("chat.manhunt.runner_head_start.start").formatted(Formatting.AQUA),
                                    true
                            );
                        }
                    } else {
                        headStartTime = headStartTime - 20;
                        if (headStartTime >= 20) {
                            int seconds = (int) Math.floor((double) headStartTime % (20 * 60) / (20));
                            int color;
                            float pitch = 0.0F;
                            if (seconds >= 10) {
                                color = 5766999;
                            } else {
                                pitch = 1.0F;
                                if (seconds == 9) {
                                    color = 8185892;
                                } else if (seconds == 8) {
                                    color = 9687040;
                                } else if (seconds == 7) {
                                    color = 10729472;
                                } else if (seconds == 6) {
                                    color = 11378688;
                                } else if (seconds == 5) {
                                    color = 11765504;
                                    pitch = 0.9F;
                                } else if (seconds == 4) {
                                    color = 11955456;
                                    pitch = 0.8F;
                                } else if (seconds == 3) {
                                    color = 11883008;
                                    pitch = 0.7F;
                                } else if (seconds == 2) {
                                    color = 11547648;
                                    pitch = 0.6F;
                                } else if (seconds == 1) {
                                    color = 11010048;
                                    pitch = 0.5F;
                                } else {
                                    color = 5766999;
                                }
                            }

                            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                player.sendMessage(Text.translatable("chat.manhunt.runner_head_start.hunter",
                                        Text.literal(seconds + " seconds").styled(style ->
                                                style.withColor(color))).styled(style -> style.withColor(5766999)
                                        ), true
                                );
                                if (pitch != 0.0F) {
                                    player.playSoundToPlayer(
                                            SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
                                            SoundCategory.MASTER,
                                            0.5F,
                                            pitch
                                    );
                                }
                            }
                        }
                    }
                }
                if (timeLimit && !paused) {
                    timeLimitLeft = timeLimitLeft - 20;
                    if (timeLimitLeft % (20 * 60 * 60) / (20 * 60) >= ManhuntConfig.config.getTimeLimit() && ManhuntMod.gameState == GameState.PLAYING) {
                        ManhuntGame.end(server, true);
                        return;
                    } else {
                        String hoursString;
                        int hours = (int) Math.floor((double) timeLimitLeft % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                        if (hours <= 9) {
                            hoursString = "0" + hours;
                        } else {
                            hoursString = String.valueOf(hours);
                        }
                        String minutesString;
                        int minutes = (int) Math.floor((double) timeLimitLeft % (20 * 60 * 60) / (20 * 60));
                        if (minutes <= 9) {
                            minutesString = "0" + minutes;
                        } else {
                            minutesString = String.valueOf(minutes);
                        }
                        String secondsString;
                        int seconds = (int) Math.floor((double) timeLimitLeft % (20 * 60) / (20));
                        if (seconds <= 9) {
                            secondsString = "0" + seconds;
                        } else {
                            secondsString = String.valueOf(seconds);
                        }

                        if (!headStart) {
                            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                player.sendMessage(Text.translatable(
                                                "chat.manhunt.time.triple",
                                                hoursString,
                                                minutesString,
                                                secondsString
                                        ).styled(style -> style.withBold(true)),
                                        true
                                );
                            }
                        }
                    }
                }
                if (paused) {
                    pauseTimeLeft = pauseTimeLeft - 20;

                    String minutesString;
                    int minutes = (int) Math.floor((double) pauseTimeLeft % (20 * 60 * 60) / (20 * 60));

                    if (minutes >= ManhuntConfig.config.getLeavePauseTime()) {
                        pauseTimeLeft = 0;
                        UnpauseCommand.unpauseGame(server);

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new ClearTitleS2CPacket(
                                    false)
                            );
                        }
                    } else {
                        if (minutes <= 9) {
                            minutesString = "0" + minutes;
                        } else {
                            minutesString = String.valueOf(minutes);
                        }
                        String secondsString;
                        int seconds = (int) Math.floor((double) pauseTimeLeft % (20 * 60) / (20));
                        if (seconds <= 9) {
                            secondsString = "0" + seconds;
                        } else {
                            secondsString = String.valueOf(seconds);
                        }

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 20, 5)
                            );
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(
                                    ManhuntConfig.config.getPausedTitle()).formatted(Formatting.YELLOW))
                            );
                            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable(
                                    "chat.manhunt.time.double",
                                    minutesString,
                                    secondsString).formatted(Formatting.GOLD))
                            );
                        }
                    }
                }
                count = 0;
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (joinList.contains(player.getUuid()) && !player.notInAnyWorld) {
                joinList.remove(player.getUuid());
                if (player.interactionManager.getGameMode() != ManhuntGame.getGameMode() ||
                        (player.getWorld().getRegistryKey() == World.OVERWORLD) && ManhuntMod.gameState != GameState.PREGAME ||
                        player.getWorld().getRegistryKey() != ManhuntMod.lobbyWorldRegistryKey && ManhuntMod.gameState == GameState.PREGAME ||
                        !ManhuntGame.playList.contains(player.getUuid())
                ) {
                    player.clearStatusEffects();
                    player.setFireTicks(0);
                    player.setOnFire(false);
                    player.setHealth(player.getMaxHealth());
                    player.setAir(player.getMaxAir());
                    resetHungerManager(player);
                    player.setExperienceLevel(0);
                    player.setExperiencePoints(0);
                    player.setScore(0);

                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

                    player.getInventory().clear();
                    player.changeGameMode(ManhuntGame.getGameMode());

                    for (AdvancementEntry advancement : player.getServer().getAdvancementLoader().getAdvancements()) {
                        AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                        for (String criteria : progress.getObtainedCriteria()) {
                            player.getAdvancementTracker().revokeCriterion(advancement, criteria);
                        }
                    }

                    if (ManhuntSettings.nightVision.get(player.getUuid())) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 255, false, false));
                    }

                    if (ManhuntMod.gameState == GameState.PREGAME) {
                        player.teleport(
                                server.getWorld(ManhuntMod.lobbyWorldRegistryKey),
                                lobbySpawn.x,
                                lobbySpawn.y,
                                lobbySpawn.z,
                                180f,
                                0f
                        );
                        player.setSpawnPoint(ManhuntMod.lobbyWorldRegistryKey, lobbySpawnPos, 180f, true, false);
                    } else {
                        if (!playerSpawnPos.containsKey(player.getUuid())) {
                            ManhuntGame.setPlayerSpawn(ManhuntMod.overworld, player);
                        }
                        int playerX = playerSpawnPos.get(player.getUuid()).getX();
                        int playerY = playerSpawnPos.get(player.getUuid()).getY();
                        int playerZ = playerSpawnPos.get(player.getUuid()).getZ();
                        player.teleport(ManhuntMod.overworld, playerX, playerY, playerZ, 0, 0);
                        player.setSpawnPoint(ManhuntMod.overworld.getRegistryKey(),
                                playerSpawnPos.get(player.getUuid()),
                                0f,
                                true,
                                false
                        );
                    }

                    if (player.getScoreboardTeam() != null) {
                        player.getScoreboard().removeScoreHolderFromTeam(player.getNameForScoreboard(), player.getScoreboardTeam());
                    }
                }
                if (ManhuntMod.gameState == GameState.PREGAME) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
                    if (ManhuntConfig.config.getRolePreset() == 1 || ManhuntConfig.config.getRolePreset() == 5) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                    } else {
                        if (ManhuntConfig.config.getRolePreset() == 2) {
                            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                        } else if (ManhuntConfig.config.getRolePreset() == 3) {
                            if (runnersTeam.getPlayerList().isEmpty()) {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                            } else {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                            }
                        } else {
                            if (huntersTeam.getPlayerList().isEmpty()) {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                            } else {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                            }
                        }
                    }
                }
                if (ManhuntMod.gameState != GameState.POSTGAME) {
                    if (player.getScoreboardTeam() == null) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                    }
                }
            }

            if (ManhuntMod.gameState == GameState.PREGAME) {
                if (resetList.contains(player.getUuid()) && !player.notInAnyWorld && !player.isDead()) {
                    resetList.remove(player.getUuid());
                    player.teleport(
                            server.getWorld(ManhuntMod.lobbyWorldRegistryKey),
                            lobbySpawn.getX(),
                            lobbySpawn.getY(),
                            lobbySpawn.getZ(),
                            PositionFlag.ROT,
                            180.0F,
                            0
                    );
                    player.setSpawnPoint(ManhuntMod.lobbyWorldRegistryKey, lobbySpawnPos, 180f, true, false);
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.SATURATION,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false,
                            false)
                    );
                    player.setOnFire(false);
                    player.setFireTicks(0);
                    player.setHealth(20.0F);
                    resetHungerManager(player);
                    player.setExperienceLevel(0);
                    player.setExperiencePoints(0);
                    player.setScore(0);
                    player.getEnderChestInventory().clear();
                    player.getInventory().clear();
                    player.changeGameMode(ManhuntGame.getGameMode());

                    for (AdvancementEntry advancement : server.getAdvancementLoader().getAdvancements()) {
                        AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                        for (String criteria : progress.getObtainedCriteria()) {
                            player.getAdvancementTracker().revokeCriterion(advancement, criteria);
                        }
                    }

                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

                    if (player.getScoreboardTeam() == null) player.getScoreboard().addScoreHolderToTeam(
                            player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters")
                    );
                }
                if (!hasItem(player, Items.PLAYER_HEAD)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.preferences").styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(0, stack);
                } else {
                    clearWrongSlots(player, Items.PLAYER_HEAD, 0);
                }
                if (ManhuntConfig.config.getRolePreset() == 1) {
                    if (!hasItem(player, Items.RECOVERY_COMPASS)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.RECOVERY_COMPASS);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.join_hunters").styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(2, stack);
                    } else {
                        clearWrongSlots(player, Items.RECOVERY_COMPASS, 2);
                    }

                    if (!hasItem(player, Items.CLOCK)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.CLOCK);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.join_runners").styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(6, stack);
                    } else {
                        clearWrongSlots(player, Items.CLOCK, 6);
                    }
                    if (!hasItem(player, Items.RED_CONCRETE) && !hasItem(player, Items.LIME_CONCRETE)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.RED_CONCRETE);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.not_ready").styled(style -> style.withColor(Formatting.RED).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(4, stack);
                    } else {
                        if (hasItem(player, Items.RED_CONCRETE)) {
                            clearWrongSlots(player, Items.RED_CONCRETE, 4);
                        }
                        if (hasItem(player, Items.LIME_CONCRETE)) {
                            clearWrongSlots(player, Items.LIME_CONCRETE, 4);
                        }
                    }
                } else {
                    removeItem(player, Items.RECOVERY_COMPASS);
                    removeItem(player, Items.RED_CONCRETE);
                    removeItem(player, Items.LIME_CONCRETE);
                    removeItem(player, Items.CLOCK);
                }
                if (!hasItem(player, Items.COMMAND_BLOCK)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.COMMAND_BLOCK);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.settings").styled(style ->
                            style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(8, stack);
                } else {
                    clearWrongSlots(player, Items.COMMAND_BLOCK, 8);
                }

                if (!Permissions.check(player, "manhunt.parkour") && player.getZ() < -2 && player.getWorld().getRegistryKey() == ManhuntMod.lobbyWorldRegistryKey) {
                    parkourTimer.putIfAbsent(player.getUuid(), 0);
                    startedParkour.putIfAbsent(player.getUuid(), false);
                    finishedParkour.putIfAbsent(player.getUuid(), false);

                    int ticks = parkourTimer.get(player.getUuid());
                    int sec = (int) Math.floor(((double) (ticks % (20 * 60)) / 20));
                    int ms = (int) Math.floor(((double) (ticks * 5) % 100));
                    String secSeconds;
                    String msSeconds;

                    if (sec < 10) {
                        secSeconds = "0" + sec;
                    } else {
                        secSeconds = String.valueOf(sec);
                    }

                    if (ms < 10) {
                        msSeconds = "0" + ms;
                    } else if (ms > 99) {
                        msSeconds = "00";
                    } else {
                        msSeconds = String.valueOf(ms);
                    }

                    if (!finishedParkour.get(player.getUuid())) {
                        if (!startedParkour.get(player.getUuid()) && player.getZ() < -3 && !(player.getZ() < -6)) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER,1.0F,1.0F);
                            startedParkour.put(player.getUuid(), true);
                        }

                        if (startedParkour.get(player.getUuid())) {
                            if (player.getZ() < -3) {
                                player.sendMessage(Text.translatable("chat.manhunt.time.double", secSeconds, msSeconds), true);
                                parkourTimer.put(player.getUuid(), parkourTimer.get(player.getUuid()) + 1);
                                player.addStatusEffect(new StatusEffectInstance(
                                        StatusEffects.INVISIBILITY,
                                        StatusEffectInstance.INFINITE,
                                        255,
                                        false,
                                        false,
                                        false)
                                );

                                if (player.getZ() < -24 && player.getZ() > -27 && player.getX() < -6 && player.getY() >= 70 && player.getY() < 72) {
                                    player.sendMessage(Text.translatable("chat.manhunt.time.double",
                                            secSeconds,
                                            msSeconds)
                                            .formatted(Formatting.GREEN),
                                            true
                                    );
                                    player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER,1.0F, 2.0F);
                                    finishedParkour.put(player.getUuid(), true);
                                }
                            }
                        }
                    }
                    if (startedParkour.get(player.getUuid())) {
                        if (player.getZ() > -3 || player.getY() < 61 || (player.getZ() < -27 && player.getY() < 68)) {
                            if (!finishedParkour.get(player.getUuid())) {
                                player.sendMessage(Text.translatable("chat.manhunt.time.double",
                                                        secSeconds,
                                                        msSeconds)
                                                .formatted(Formatting.RED),
                                        true
                                );
                            }
                            resetLobbyPlayer(player);
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER,1.0F, 0.5F);
                        }
                    }
                } else {
                    if (player.getY() < 58) {
                        resetLobbyPlayer(player);
                    }
                }
            }

            if (ManhuntMod.gameState == GameState.PLAYING) {
                if (startList.contains(player.getUuid()) && !player.notInAnyWorld) {
                    startList.remove(player.getUuid());
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.RESISTANCE,
                            2,
                            255,
                            false,
                            false,
                            false)
                    );
                    if (!playerSpawnPos.containsKey(player.getUuid())) {
                        ManhuntGame.setPlayerSpawn(ManhuntMod.overworld, player);
                    }
                    double playerX = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getX()));
                    double playerY = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getY()));
                    double playerZ = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getZ()));
                    player.teleport(
                            ManhuntMod.overworld,
                            playerX,
                            playerY,
                            playerZ,
                            0,
                            0
                    );
                    player.setSpawnPoint(
                            ManhuntMod.overworld.getRegistryKey(),
                            playerSpawnPos.get(player.getUuid()),
                            0f,
                            true,
                            false
                    );
                    player.setFireTicks(0);
                    player.setOnFire(false);
                    player.setHealth(player.getMaxHealth());
                    player.setAir(player.getMaxAir());
                    resetHungerManager(player);

                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

                    player.getInventory().clear();
                    player.changeGameMode(ManhuntGame.getGameMode());

                    Stats.MINED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.CRAFTED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.USED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.BROKEN.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.PICKED_UP.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.DROPPED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.KILLED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.KILLED_BY.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
                    Stats.CUSTOM.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));

                    if (ManhuntSettings.customTitles.get(player.getUuid())) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(
                                Text.literal(ManhuntConfig.config.getStartTitle()))
                        );
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(
                                Text.literal(ManhuntConfig.config.getStartSubtitle()).formatted(Formatting.GRAY))
                        );
                    }

                    if (ManhuntSettings.customSounds.get(player.getUuid())) {
                        player.playSoundToPlayer(
                                SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(),
                                SoundCategory.MASTER,
                                0.5F,
                                2.0F
                        );
                    }

                    if (ManhuntSettings.nightVision.get(player.getUuid()))
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.NIGHT_VISION,
                                StatusEffectInstance.INFINITE,
                                255,
                                false,
                                false)
                        );
                }
                if (paused) {
                    if (PauseCommand.playerPos.containsKey(player.getUuid()) && PauseCommand.playerYaw.containsKey(player.getUuid()) && PauseCommand.playerPitch.containsKey(player.getUuid())) {
                        player.teleport(
                                server.getWorld(player.getWorld().getRegistryKey()),
                                PauseCommand.playerPos.get(player.getUuid()).getX(),
                                PauseCommand.playerPos.get(player.getUuid()).getY(),
                                PauseCommand.playerPos.get(player.getUuid()).getZ(),
                                PauseCommand.playerYaw.get(player.getUuid()),
                                PauseCommand.playerPitch.get(player.getUuid())
                        );
                    } else {
                        PauseCommand.playerPos.put(player.getUuid(), player.getPos());
                        PauseCommand.playerYaw.put(player.getUuid(), player.getYaw());
                        PauseCommand.playerPitch.put(player.getUuid(), player.getPitch());
                    }
                }
                if (headStart && !paused) {
                    if (!headStartCountdown && player.isTeamPlayer(runnersTeam)) {
                        if (!headStartPos.containsKey(player.getUuid())) {
                            headStartPos.put(player.getUuid(), player.getBlockPos());
                        }

                        if (
                                Math.abs(
                                        (player.getX() - headStartPos.get(player.getUuid()).getX()) *
                                        (player.getZ() - headStartPos.get(player.getUuid()).getZ()))
                                        >= 0.5
                        ) {
                            headStartCountdown = true;
                        }
                    } else {
                        if (headStartTime >= 20) {
                            if (player.isTeamPlayer(huntersTeam)) {
                                if (PauseCommand.playerPos.containsKey(player.getUuid())) {
                                    player.teleport(
                                            server.getWorld(player.getWorld().getRegistryKey()),
                                            PauseCommand.playerPos.get(player.getUuid()).getX(),
                                            PauseCommand.playerPos.get(player.getUuid()).getY(),
                                            PauseCommand.playerPos.get(player.getUuid()).getZ(),
                                            player.getYaw(),
                                            player.getPitch()
                                    );
                                } else {
                                    PauseCommand.playerPos.put(player.getUuid(), player.getPos());
                                }
                            }
                        } else {
                            headStart = false;
                            headStartCountdown = false;
                            if (player.isTeamPlayer(huntersTeam)) {
                                player.clearStatusEffects();
                                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                                player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                                player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
                            }
                            player.setFireTicks(0);
                            player.setOnFire(false);
                            player.setHealth(20);
                            player.setAir(player.getMaxAir());
                            resetHungerManager(player);
                            player.sendMessage(Text.translatable("chat.manhunt.runner_head_start.go").formatted(Formatting.GOLD), true);
                            player.playSoundToPlayer(
                                    SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(),
                                    SoundCategory.MASTER,
                                    0.5F,
                                    0.5F
                            );
                        }
                    }
                }
            }
        }
    }

    public static void playerRespawn(ServerPlayerEntity player) {
        joinList.add(player.getUuid());
        if (ManhuntMod.gameState == GameState.PLAYING) {
            if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                if (ManhuntConfig.config.isHuntOnDeath()) {
                    if (player.getScoreboard().getTeam("runners").getPlayerList().size() != 1) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
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

        ManhuntSettings.slowDownManager.putIfAbsent(player.getUuid(), 0);

        if (!ManhuntGame.playList.contains(player.getUuid()) && player.getScoreboardTeam() != null) {
            scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), player.getScoreboardTeam());
        }

        if (!playerSpawnPos.containsKey(player.getUuid())) {
            ManhuntGame.setPlayerSpawn(ManhuntMod.overworld, player);
        }

        if (ManhuntMod.gameState == GameState.PREGAME) {
            player.setSpawnPoint(ManhuntMod.lobbyWorldRegistryKey, lobbySpawnPos, 180f, true, false);
        } else if (ManhuntMod.gameState == GameState.PLAYING) {
            if (paused) {
                if (!leftOnPause.contains(player.getUuid())) {
                    player.playSoundToPlayer(
                            SoundEvents.BLOCK_ANVIL_LAND,
                            SoundCategory.MASTER,
                            0.1f,
                            0.5F
                    );
                    if (!player.getStatusEffects().isEmpty()) {
                        PauseCommand.playerEffects.put(player.getUuid(), player.getStatusEffects());
                    }
                    PauseCommand.playerPos.put(player.getUuid(), player.getPos());
                    PauseCommand.playerYaw.put(player.getUuid(), player.getYaw());
                    PauseCommand.playerPitch.put(player.getUuid(), player.getPitch());
                    playerFood.put(player.getUuid(), player.getHungerManager().getFoodLevel());
                    playerSaturation.put(player.getUuid(), player.getHungerManager().getSaturationLevel());
                    playerExhaustion.put(player.getUuid(), player.getHungerManager().getExhaustion());
                    player.clearStatusEffects();
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(0);
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.DARKNESS,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false,
                            false)
                    );
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.RESISTANCE,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false,
                            false)
                    );
                }
                if (player.isTeamPlayer(scoreboard.getTeam("runners")) && scoreboard.getTeam("runners").getPlayerList().size() == 1) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
                    player.playSoundToPlayer(
                            SoundEvents.BLOCK_ANVIL_LAND,
                            SoundCategory.MASTER,
                            0.1f,
                            1.5f
                    );
                    player.clearStatusEffects();
                    if (PauseCommand.playerEffects.containsKey(player.getUuid())) {
                        for (StatusEffectInstance statusEffect : PauseCommand.playerEffects.get(player.getUuid())) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                    player.setHealth(0);
                    var hungerManager = player.getHungerManager();
                    hungerManager.setFoodLevel(playerFood.get(player.getUuid()));
                    hungerManager.setSaturationLevel(playerSaturation.get(player.getUuid()));
                    hungerManager.setExhaustion(playerExhaustion.get(player.getUuid()));

                    UnpauseCommand.unpauseGame(server);
                }
            } else {
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

                if (leftOnPause.contains(player.getUuid())) {
                    player.playSoundToPlayer(
                            SoundEvents.BLOCK_ANVIL_LAND,
                            SoundCategory.MASTER,
                            0.1f,
                            1.5f
                    );
                    player.clearStatusEffects();
                    if (PauseCommand.playerEffects.containsKey(player.getUuid())) {
                        for (StatusEffectInstance statusEffect : PauseCommand.playerEffects.get(player.getUuid())) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                }
            }
        }

        joinList.add(player.getUuid());

        if (!ManhuntSettings.customTitles.containsKey(player.getUuid())) {
            ManhuntSettings.customTitles.putIfAbsent(player.getUuid(), ManhuntConfig.config.isCustomTitlesDefault());
            ManhuntSettings.customSounds.putIfAbsent(player.getUuid(), ManhuntConfig.config.isCustomSoundsDefault());
            ManhuntSettings.customParticles.putIfAbsent(player.getUuid(), ManhuntConfig.config.isCustomParticlesDefault());
            ManhuntSettings.automaticCompass.putIfAbsent(player.getUuid(), ManhuntConfig.config.isAutomaticCompassDefault());
            ManhuntSettings.nightVision.putIfAbsent(player.getUuid(), false);
            ManhuntSettings.friendlyFire.putIfAbsent(player.getUuid(), true);
            ManhuntSettings.bedExplosions.putIfAbsent(player.getUuid(), ManhuntConfig.config.isBedExplosionsDefault());
            ManhuntSettings.lavaPvpInNether.putIfAbsent(player.getUuid(), ManhuntConfig.config.isLavaPvpInNetherDefault());

            Optional<DataContainer> dataContainer = ManhuntMod.datastore.getContainer("uuid", player.getUuid());
            if (dataContainer.isEmpty()) return;

            ManhuntSettings.customTitles.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "custom_titles"));
            ManhuntSettings.customSounds.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "custom_sounds"));
            ManhuntSettings.customParticles.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "custom_particles"));
            ManhuntSettings.automaticCompass.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "automatic_compass"));
            ManhuntSettings.nightVision.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "night_vision"));
            ManhuntSettings.friendlyFire.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "friendly_fire"));
            ManhuntSettings.bedExplosions.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "bed_explosions"));
            ManhuntSettings.lavaPvpInNether.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "lava_pvp_in_nether"));
        }
    }

    public static void playerLeave(ServerPlayNetworkHandler handler) {
        var player = handler.player;
        var scoreboard = player.getScoreboard();
        var runnersTeam = scoreboard.getTeam("runners");
        boolean isRunner = player.isTeamPlayer(runnersTeam);

        if (ManhuntMod.gameState == GameState.PREGAME) {
            if (isRunner && runnersTeam.getPlayerList().size() == 1) {
                starting = false;
                startingTime = 0;
            }
        } else if (ManhuntMod.gameState == GameState.PLAYING) {
            if (paused) {
                leftOnPause.add(player.getUuid());
            } else {
                if (isRunner && runnersTeam.getPlayerList().size() == 1) {
                    leftOnPause.add(player.getUuid());
                    PauseCommand.pauseGame(player.getServer());
                }
            }
        }

        var dataContainer = ManhuntMod.datastore.getOrCreateContainer("uuid", player.getUuid());

        dataContainer.transaction()
                .put(JavaTypes.UUID, "uuid", player.getUuid())
                .put(JavaTypes.BOOL, "custom_titles", ManhuntSettings.customTitles.get(player.getUuid()))
                .put(JavaTypes.BOOL, "custom_sounds", ManhuntSettings.customSounds.get(player.getUuid()))
                .put(JavaTypes.BOOL, "custom_particles", ManhuntSettings.customParticles.get(player.getUuid()))
                .put(JavaTypes.BOOL, "automatic_compass", ManhuntSettings.automaticCompass.get(player.getUuid()))
                .put(JavaTypes.BOOL, "night_vision", ManhuntSettings.nightVision.get(player.getUuid()))
                .put(JavaTypes.BOOL, "friendly_fire", ManhuntSettings.friendlyFire.get(player.getUuid()))
                .put(JavaTypes.BOOL, "bed_explosions", ManhuntSettings.bedExplosions.get(player.getUuid()))
                .put(JavaTypes.BOOL, "lava_pvp_in_nether", ManhuntSettings.lavaPvpInNether.get(player.getUuid()))
                .commit();
    }

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, World world, Hand hand) {
        var stack = player.getStackInHand(hand);
        var server = player.getServer();
        var scoreboard = server.getScoreboard();
        var huntersTeam = scoreboard.getTeam("hunters");
        var runnersTeam = scoreboard.getTeam("runners");

        if (ManhuntMod.gameState == GameState.PREGAME) {
            if (stack.getItem() == Items.PLAYER_HEAD) {
                ManhuntSettings.openPreferencesGui((ServerPlayerEntity) player);
            }
            if (stack.getItem() == Items.RECOVERY_COMPASS) {
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 8) ManhuntSettings.slowDownManager.put(player.getUuid(), ManhuntSettings.slowDownManager.get(player.getUuid()) + 1);
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 4) {
                    if (!player.isTeamPlayer(huntersTeam)) {
                        scoreboard.addScoreHolderToTeam(
                                player.getNameForScoreboard(), huntersTeam
                        );

                        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard())
                                        .formatted(ManhuntConfig.config.getHuntersColor()),
                                Text.translatable("role.manhunt.hunters")
                                        .formatted(ManhuntConfig.config.getHuntersColor())
                                ), false
                        );

                        player.playSoundToPlayer(
                                SoundEvents.ITEM_LODESTONE_COMPASS_LOCK,
                                SoundCategory.PLAYERS,
                                0.5F,
                               1.0F
                        );
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team",
                                Text.translatable("role.manhunt.hunter"))
                                        .formatted(Formatting.RED
                                ), false
                        );
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.RED_CONCRETE) {
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 8) ManhuntSettings.slowDownManager.put(player.getUuid(), ManhuntSettings.slowDownManager.get(player.getUuid()) + 1);
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 4) {
                    allReadyUps.add(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable(
                            "chat.manhunt.ready",
                            Text.literal(player.getNameForScoreboard())
                                    .formatted(Formatting.GREEN)),
                            false
                    );

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Ready", true);

                    ItemStack itemStack = new ItemStack(Items.LIME_CONCRETE);
                    itemStack.set(
                            DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.ready")
                                    .styled(style -> style.withColor(Formatting.GREEN)
                                    .withItalic(false))
                    );
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F,1.0F);

                    if (allReadyUps.size() == server.getPlayerManager().getPlayerList().size()) {
                        if (runnersTeam.getPlayerList().isEmpty()) {
                            server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.minimum",
                                    Text.translatable("role.manhunt.runner")
                                    ).formatted(Formatting.RED),
                                    false
                            );
                        } else {
                            startingTime = 120;
                            starting = true;
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.LIME_CONCRETE) {
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 8) ManhuntSettings.slowDownManager.put(player.getUuid(), ManhuntSettings.slowDownManager.get(player.getUuid()) + 1);
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 4) {
                    allReadyUps.remove(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.not_ready",
                            Text.literal(player.getNameForScoreboard())
                                    .formatted(Formatting.RED)
                            ), false
                    );

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);

                    ItemStack itemStack = new ItemStack(Items.RED_CONCRETE);
                    itemStack.set(
                            DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.not_ready")
                                    .styled(style -> style.withColor(Formatting.RED)
                                    .withItalic(false))
                    );
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5F,1.0F);
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.CLOCK) {
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 8) ManhuntSettings.slowDownManager.put(player.getUuid(), ManhuntSettings.slowDownManager.get(player.getUuid()) + 1);
                if (ManhuntSettings.slowDownManager.get(player.getUuid()) < 4) {
                    if (!player.isTeamPlayer(runnersTeam)) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                        server.getPlayerManager().broadcast(Text.translatable(
                                "chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard())
                                        .formatted(ManhuntConfig.config.getRunnersColor()),
                                Text.translatable("role.manhunt.runners")
                                        .formatted(ManhuntConfig.config.getRunnersColor())
                                ), false
                        );
                        player.playSoundToPlayer(
                                SoundEvents.ENTITY_ENDER_EYE_LAUNCH,
                                SoundCategory.PLAYERS,
                                0.5F,
                               1.0F
                        );
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team",
                                Text.translatable("role.manhunt.runner"))
                                .formatted(Formatting.RED),
                                false
                        );
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down")
                            .formatted(Formatting.RED)
                    );
                }
            }

            if (stack.getItem() == Items.COMMAND_BLOCK) {
                ManhuntSettings.openSettingsGui((ServerPlayerEntity) player);
            }
        } else if (ManhuntMod.gameState == GameState.PLAYING) {
            if (paused) {
                return TypedActionResult.fail(stack);
            }
            if (headStart && player.getScoreboardTeam() != null) {
                if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                    return TypedActionResult.fail(stack);
                } else {
                    if (!headStartCountdown) {
                        return TypedActionResult.fail(stack);
                    }
                }
            }
            if (stack.getItem() == Items.COMPASS && stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker") && (!ManhuntConfig.config.isAutomaticCompass() || !ManhuntSettings.automaticCompass.get(player.getUuid())) && player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && !allRunners.isEmpty() && !player.isSpectator() && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                player.getItemCooldownManager().set(stack.getItem(), 20);
                if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name") != null) {
                    NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                    nbt.putString("Name", allRunners.getFirst().getName().getString());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                }
                ServerPlayerEntity trackedPlayer = server.getPlayerManager().getPlayer(
                        stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));
                if (trackedPlayer != null) {
                    ManhuntGame.updateCompass((ServerPlayerEntity) player, stack, trackedPlayer);
                    player.playSoundToPlayer(
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            SoundCategory.MASTER,
                            0.1f,
                            0.5F
                    );
                }
            }
            if (!ManhuntConfig.config.isLavaPvpInNether()) {
                if (world.getRegistryKey() == ManhuntMod.theNether.getRegistryKey() && stack.getItem() == Items.LAVA_BUCKET) {
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED));
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }
        }

        return TypedActionResult.pass(stack);
    }

    public static ActionResult useBlock(PlayerEntity player, World world, Hand hand, HitResult hitResult) {
        if (ManhuntMod.gameState == GameState.PLAYING) {
            if (paused) {
                return ActionResult.FAIL;
            }
            if (headStart && player.getScoreboardTeam() != null) {
                if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                    return ActionResult.FAIL;
                } else {
                    if (!headStartCountdown) {
                        return ActionResult.FAIL;
                    }
                }
            }
            if (!ManhuntConfig.config.isBedExplosions()) {
                if (world.getRegistryKey() != ManhuntMod.overworld.getRegistryKey()) {
                    Vec3d pos = hitResult.getPos();
                    Block block = player.getWorld().getBlockState(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)).getBlock();
                    if (
                            player.getStackInHand(hand).getName().getString().toLowerCase().contains(" bed")
                                    ||
                                    block.getName().getString().toLowerCase().contains(" bed")
                    ) {
                        for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                            if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                                player.sendMessage(Text.translatable("chat.manhunt.disabled_if_close").formatted(Formatting.RED));
                                return ActionResult.FAIL;
                            }
                        }
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    private static void spawnLobby(MinecraftServer server) throws IOException {
        NbtCompound islandNbt = NbtIo.readCompressed(ManhuntMod.class.getResourceAsStream("/manhunt/lobby/island.nbt"), NbtSizeTracker.ofUnlimitedBytes());
        NbtCompound parkourNbt = NbtIo.readCompressed(ManhuntMod.class.getResourceAsStream("/manhunt/lobby/parkour.nbt"), NbtSizeTracker.ofUnlimitedBytes());
        ServerWorld lobby = server.getWorld(ManhuntMod.lobbyWorldRegistryKey);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 0), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, 0), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, -15), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, -15), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 16), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 0), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 16), 16, Unit.INSTANCE);
        placeStructure(lobby, new BlockPos(-21, 55, -6), islandNbt);
        placeStructure(lobby, new BlockPos(-21, 55, -54), parkourNbt);
    }

    private static void placeStructure(ServerWorld world, BlockPos pos, NbtCompound nbt) {
        StructureTemplate template = world.getStructureTemplateManager().createTemplate(nbt);
        template.place(
                world,
                pos,
                pos,
                new StructurePlacementData(),
                StructureBlockBlockEntity.createRandom(world.getSeed()),
                2
        );
    }

    private static boolean hasItem(PlayerEntity player, Item item) {
        boolean hasItem = player.getOffHandStack().getItem() == item;
        if (!hasItem) {
            for (ItemStack stack : player.getInventory().main) {
                if (stack.getItem() == item) {
                    hasItem = true;
                    break;
                }
            }
        }
        if (!hasItem) {
            for (ItemStack stack : player.getInventory().armor) {
                if (stack.getItem() == item) {
                    hasItem = true;
                    break;
                }
            }
        }
        return hasItem;
    }

    private static void clearWrongSlots(PlayerEntity player, Item item, int slot) {
        boolean hasItem = player.getOffHandStack().getItem() == item;
        if (hasItem) {
            player.getInventory().offHand.clear();
        }

        if (!hasItem) {
            for (ItemStack stack : player.getInventory().main) {
                if (stack.getItem() == item) {
                    if (player.getInventory().getSlotWithStack(stack) != slot) {
                        player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                        break;
                    } else if (stack.getCount() > 1) {
                        stack.setCount(1);
                        player.getInventory().setStack(slot, stack);
                    }
                }
            }
        }
    }

    private static void removeItem(PlayerEntity player, Item item) {
        boolean hasItem = player.getOffHandStack().getItem() == item;
        if (hasItem) {
            player.getInventory().offHand.clear();
        }

        if (!hasItem) {
            for (ItemStack stack : player.getInventory().main) {
                if (stack.getItem() == item) {
                    player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                }
            }
        }
    }

    private static void resetLobbyPlayer(ServerPlayerEntity player) {
        parkourTimer.put(player.getUuid(), 0);
        startedParkour.put(player.getUuid(), false);
        finishedParkour.put(player.getUuid(), false);
        player.changeGameMode(GameMode.ADVENTURE);
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        player.teleport(player.getServer().getWorld(ManhuntMod.lobbyWorldRegistryKey), lobbySpawn.getX(), lobbySpawn.getY(), lobbySpawn.getZ(), PositionFlag.ROT, 180f, 0);
    }

    public static void resetHungerManager(ServerPlayerEntity player) {
        var hungerManager = player.getHungerManager();
        hungerManager.setFoodLevel(20);
        hungerManager.setSaturationLevel(5f);
        hungerManager.setExhaustion(0f);
    }
}
