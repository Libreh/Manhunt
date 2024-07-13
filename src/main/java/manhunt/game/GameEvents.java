package manhunt.game;

import manhunt.ManhuntMod;
import manhunt.command.PauseCommand;
import manhunt.command.UnpauseCommand;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.mrnavastar.sqlib.DataContainer;
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
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Style;
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
import java.util.LinkedList;

import static manhunt.ManhuntMod.*;
import static manhunt.game.ManhuntGame.*;

public class GameEvents {
    private static boolean headstartCountdown = false;
    private static int count;

    public static void serverStart(MinecraftServer server) {
        state = GameState.PREGAME;

        if (config.isSetMotd()) {
            server.setMotd(state.getColor() + "[" + state.getMotd() + "]§f Minecraft MANHUNT");
        }

        GameRules gameRules = server.getWorld(lobbyRegistry).getGameRules();
        spawnRadius = gameRules.get(GameRules.SPAWN_RADIUS).get();
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
        gameRules.get(GameRules.SPAWN_RADIUS).set(0, server);
        gameRules.get(GameRules.FALL_DAMAGE).set(false, server);

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

        if (config.isTeamColor()) {
            huntersTeam.setColor(config.getHuntersColor());
            runnersTeam.setColor(config.getRunnersColor());
        } else {
            huntersTeam.setColor(Formatting.RESET);
            runnersTeam.setColor(Formatting.RESET);
        }

        try {
            spawnLobby(server);
        } catch (IOException e) {
            LOGGER.error("Failed to spawn Manhunt mod lobby");
        }
    }

    public static void serverTick(MinecraftServer server) {
        if (state == GameState.PLAYING) {
            allPlayers = server.getPlayerManager().getPlayerList();
            allRunners = new LinkedList<>();

            Team runners = server.getScoreboard().getTeam("runners");
            for (ServerPlayerEntity player : allPlayers) {
                if (player != null) {
                    if (player.isTeamPlayer(runners)) {
                        allRunners.add(player);
                    }
                }
            }

            if (duration && !paused) {
                durationTime = durationTime - 1;
            }

            if (paused) {
                if (config.getRunnerLeavingPauseTime() != 0) {
                    pauseTime = pauseTime - 1;
                } else {
                    pauseTime = pauseTime + 1;
                }
            }

            if (headstart && !paused && headstartCountdown) {
                headstartTime = headstartTime - 1;
            }

            count++;
            if (count == 19) {
                if (duration && !paused) {
                    if (durationTime % (20 * 60 * 60) / (20 * 60) >= config.getTimeLimit() && state == GameState.PLAYING) {
                        gameOver(server, true);
                        return;
                    } else {
                        String hoursString;
                        int hours = (int) Math.floor((double) durationTime % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                        if (hours <= 9) {
                            hoursString = "0" + hours;
                        } else {
                            hoursString = String.valueOf(hours);
                        }
                        String minutesString;
                        int minutes = (int) Math.floor((double) durationTime % (20 * 60 * 60) / (20 * 60));
                        if (minutes <= 9) {
                            minutesString = "0" + minutes;
                        } else {
                            minutesString = String.valueOf(minutes);
                        }
                        String secondsString;
                        int seconds = (int) Math.floor((double) durationTime % (20 * 60) / (20));
                        if (seconds <= 9) {
                            secondsString = "0" + seconds;
                        } else {
                            secondsString = String.valueOf(seconds);
                        }

                        if (!headstart) {
                            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                player.sendMessage(Text.translatable(
                                                "chat.time.triple",
                                                hoursString,
                                                minutesString,
                                                secondsString
                                        ).setStyle(Style.EMPTY.withBold(true)),
                                        true
                                );
                            }
                        }
                    }
                }

                if (paused) {
                    String minutesString;
                    int minutes = (int) Math.floor((double) pauseTime % (20 * 60 * 60) / (20 * 60));

                    if (minutes >= config.getRunnerLeavingPauseTime()) {
                        pauseTime = 0;
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
                        int seconds = (int) Math.floor((double) pauseTime % (20 * 60) / (20));
                        if (seconds <= 9) {
                            secondsString = "0" + seconds;
                        } else {
                            secondsString = String.valueOf(seconds);
                        }

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 20, 5)
                            );

                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(
                                    config.getGamePausedTitle()).formatted(Formatting.YELLOW))
                            );
                            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable(
                                    "chat.time.double",
                                    minutesString,
                                    secondsString).formatted(Formatting.GOLD))
                            );
                        }
                    }
                }

                if (headstart && !paused) {
                    if (!headstartCountdown) {
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.sendMessage(Text.translatable("chat.runner_headstart.start").formatted(Formatting.AQUA), true);
                        }
                    } else {
                        if (headstartTime >= 20) {
                            int seconds = (int) Math.floor((double) headstartTime % (20 * 60) / (20));
                            Formatting formatting;
                            float pitch = 0f;
                            if (seconds <= 10) {
                                if (seconds <= 5) {
                                    if (seconds <= 3) {
                                        formatting = Formatting.DARK_RED;
                                        pitch = 2f;
                                    } else {
                                        formatting = Formatting.RED;
                                        pitch = 1.0f;
                                    }
                                } else {
                                    pitch = 0.5f;
                                    formatting = Formatting.GOLD;
                                }
                            } else {
                                formatting = Formatting.YELLOW;
                            }

                            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                player.sendMessage(Text.translatable("chat.runner_headstart.hunter", Text.literal(seconds + " seconds").formatted(formatting)), true);

                                if (pitch != 0f) {
                                    player.playSoundToPlayer(
                                            SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
                                            SoundCategory.MASTER,
                                            0.5f,
                                            pitch
                                    );
                                }
                            }
                        }
                    }
                }
                count = 0;
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (newPlayersList.contains(player.getUuid()) && !player.notInAnyWorld) {
                newPlayersList.remove(player.getUuid());

                if (player.interactionManager.getGameMode() != getGameMode() || (player.getWorld().getRegistryKey() == lobbyRegistry || player.getWorld().getRegistryKey() == World.OVERWORLD) && state != GameState.PREGAME || player.getWorld().getRegistryKey() != lobbyRegistry && state == GameState.PREGAME) {
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
                    player.setFireTicks(0);
                    player.setOnFire(false);
                    player.setHealth(player.getMaxHealth());
                    player.setAir(player.getMaxAir());
                    player.getHungerManager().setFoodLevel(20);
                    player.getHungerManager().setSaturationLevel(5f);
                    player.getHungerManager().setExhaustion(0f);
                    player.setExperienceLevel(0);
                    player.setExperiencePoints(0);
                    player.setScore(0);

                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

                    player.getInventory().clear();
                    player.changeGameMode(getGameMode());

                    for (AdvancementEntry advancement : player.getServer().getAdvancementLoader().getAdvancements()) {
                        AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                        for (String criteria : progress.getObtainedCriteria()) {
                            player.getAdvancementTracker().revokeCriterion(advancement, criteria);
                        }
                    }

                    if (nightVision.get(player.getUuid())) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 255, false, false));
                    }

                    if (state == GameState.PREGAME) {
                        player.teleport(server.getWorld(lobbyRegistry), lobbySpawn.x, lobbySpawn.y, lobbySpawn.z, 180f, 0f);
                        player.setSpawnPoint(lobbyRegistry, lobbySpawnPos, 180f, true, false);
                    } else {
                        if (!playerSpawnPos.containsKey(player.getUuid())) {
                            setPlayerSpawn(getOverworld(), player);
                        }

                        double playerX = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getX()));
                        double playerY = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getY()));
                        double playerZ = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getZ()));
                        player.teleport(getOverworld(), playerX, playerY, playerZ, 0, 0);
                    }
                }

                if (state == GameState.PREGAME) {
                    if (config.getTeamPreset() == 1 || config.getTeamPreset() == 5) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
                    } else {
                        if (config.getTeamPreset() == 2) {
                            player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                        } else if (config.getTeamPreset() == 3) {
                            if (player.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                            } else {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
                            }
                        } else {
                            if (player.getScoreboard().getTeam("hunters").getPlayerList().isEmpty()) {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
                            } else {
                                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                            }
                        }
                    }
                }
            }

            if (player.getScoreboardTeam() == null) player.getScoreboard().addScoreHolderToTeam(
                    player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters")
            );

            if (state == GameState.PREGAME) {
                if (!hasItem(player, Items.PLAYER_HEAD)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.preferences").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
                    stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(0, stack);
                } else {
                    clearWrongSlots(player, Items.PLAYER_HEAD, 0);
                }

                if (config.getTeamPreset() == 1) {
                    if (!hasItem(player, Items.RECOVERY_COMPASS)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.RECOVERY_COMPASS);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.join_hunters").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(2, stack);
                    } else {
                        clearWrongSlots(player, Items.RECOVERY_COMPASS, 2);
                    }

                    if (!hasItem(player, Items.CLOCK)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.CLOCK);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.join_runners").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(6, stack);
                    } else {
                        clearWrongSlots(player, Items.CLOCK, 6);
                    }
                } else {
                    removeItem(player, Items.RECOVERY_COMPASS);
                    removeItem(player, Items.CLOCK);
                }

                if (!hasItem(player, Items.RED_CONCRETE) && !hasItem(player, Items.LIME_CONCRETE)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.RED_CONCRETE);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.not_ready").setStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(false)));
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

                if (!hasItem(player, Items.COMMAND_BLOCK)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.COMMAND_BLOCK);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.settings").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(8, stack);
                } else {
                    clearWrongSlots(player, Items.COMMAND_BLOCK, 8);
                }

                if (!Permissions.check(player, "manhunt.parkour") && player.getZ() < 0) {
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
                        if (!startedParkour.get(player.getUuid()) && player.getZ() < -4 && !(player.getZ() < -6)) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1f, 1f);
                            startedParkour.put(player.getUuid(), true);
                        }

                        if (startedParkour.get(player.getUuid())) {
                            if (player.getZ() < -4) {
                                player.sendMessage(Text.translatable("chat.time.double", secSeconds, msSeconds), true);
                                parkourTimer.put(player.getUuid(), parkourTimer.get(player.getUuid()) + 1);
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 255, false, false, false));

                                if (player.getZ() < -24 && player.getZ() > -27 && player.getX() < -6 && player.getY() >= 70 && player.getY() < 72) {
                                    player.sendMessage(Text.translatable("chat.time.double", secSeconds, msSeconds).formatted(Formatting.GREEN), true);
                                    player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1f, 2f);
                                    finishedParkour.put(player.getUuid(), true);
                                }
                            }

                            if (player.getZ() > -4 || player.getY() < 61 || (player.getZ() < -27 && player.getY() < 68)) {
                                player.sendMessage(Text.translatable("chat.time.double", secSeconds, msSeconds).formatted(Formatting.RED), true);
                                resetLobbyPlayer(player);
                                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1f, 0.5f);
                            }
                        }
                    }
                }
            }

            if (state == GameState.PLAYING) {
                if (paused) {
                    if (playerPos.containsKey(player.getUuid())) {
                        player.teleport(
                                server.getWorld(player.getWorld().getRegistryKey()),
                                playerPos.get(player.getUuid()).getX(),
                                playerPos.get(player.getUuid()).getY(),
                                playerPos.get(player.getUuid()).getZ(),
                                0f,
                                0f
                        );
                    } else {
                        playerPos.put(player.getUuid(), player.getPos());
                    }
                }

                if (headstart && !paused) {
                    if (!headstartCountdown) {
                        if (player.getScoreboard().getTeam("runners").getPlayerList().contains(player.getNameForScoreboard())) {
                            if (
                                    Math.abs(
                                            (player.getX() - playerSpawnPos.get(player.getUuid()).getX())
                                                    *
                                                    (player.getZ() - playerSpawnPos.get(player.getUuid()).getZ()))
                                            >= 0.5
                            ) {
                                headstartCountdown = true;
                            }
                        }
                    }

                    if (headstartTime >= 20) {
                        if (player.getScoreboard().getTeam("hunters").getPlayerList().contains(player.getNameForScoreboard())) {
                            if (playerPos.containsKey(player.getUuid())) {
                                player.teleport(
                                        server.getWorld(player.getWorld().getRegistryKey()),
                                        playerPos.get(player.getUuid()).getX(),
                                        playerPos.get(player.getUuid()).getY(),
                                        playerPos.get(player.getUuid()).getZ(),
                                        0f,
                                        0f
                                );
                            } else {
                                playerPos.put(player.getUuid(), player.getPos());
                            }
                        }
                    } else {
                        headstart = false;
                        headstartCountdown = false;

                        if (player.getScoreboard().getTeam("hunters").getPlayerList().contains(player.getNameForScoreboard())) {
                            player.clearStatusEffects();
                            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                            player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                            player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
                        }
                    }
                }
            }
        }
    }

    public static void playerRespawn(ServerPlayerEntity player) {
        if (state == GameState.PREGAME) {
            if (!player.notInAnyWorld) {
                player.setSpawnPoint(lobbyRegistry, lobbySpawnPos, 180f, true, false);
                player.teleport(
                        player.getServer().getWorld(lobbyRegistry),
                        lobbySpawn.getX(),
                        lobbySpawn.getY(),
                        lobbySpawn.getZ(),
                        PositionFlag.ROT,
                        180f,
                        0
                );
            }
        } else if (state == GameState.PLAYING) {
            if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                if (config.isRunnersHuntOnDeath() && player.getScoreboard().getTeam("runners").getPlayerList().size() != 1) {
                    player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
                } else {
                    player.changeGameMode(GameMode.SPECTATOR);
                }
            }
        }
    }

    public static void playerJoin(ServerPlayNetworkHandler handler) {
        ServerPlayerEntity player = handler.player;

        slowDownManager.putIfAbsent(player.getUuid(), 0);

        if (state == GameState.PREGAME) {
            player.setSpawnPoint(lobbyRegistry, lobbySpawnPos, 180f, true, false);
        } else if (state == GameState.PLAYING) {
            if (paused) {
                if (!leftOnPause.contains(player.getUuid())) {
                    player.playSoundToPlayer(
                            SoundEvents.BLOCK_ANVIL_LAND,
                            SoundCategory.MASTER,
                            0.1f,
                            0.5f
                    );
                    if (!player.getStatusEffects().isEmpty()) {
                        playerEffects.put(player.getUuid(), player.getStatusEffects());
                    }
                    playerPos.put(player.getUuid(), player.getPos());
                    playerYaw.put(player.getUuid(), player.getYaw());
                    playerPitch.put(player.getUuid(), player.getPitch());
                    playerFood.put(player.getUuid(), player.getHungerManager().getFoodLevel());
                    playerSaturation.put(player.getUuid(), player.getHungerManager().getSaturationLevel());
                    playerExhuastion.put(player.getUuid(), player.getHungerManager().getExhaustion());
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

                if (player.getScoreboard().getTeam("runners").getPlayerList().size() == 1) {
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
                    if (playerEffects.containsKey(player.getUuid())) {
                        for (StatusEffectInstance statusEffect : playerEffects.get(player.getUuid())) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                    player.getHungerManager().setFoodLevel(playerFood.get(player.getUuid()));
                    player.getHungerManager().setSaturationLevel(playerSaturation.get(player.getUuid()));
                    player.getHungerManager().setExhaustion(playerExhuastion.get(player.getUuid()));

                    UnpauseCommand.unpauseGame(player.getServer());
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
                    if (playerEffects.containsKey(player.getUuid())) {
                        for (StatusEffectInstance statusEffect : playerEffects.get(player.getUuid())) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                }
            }
        }

        newPlayersList.add(player.getUuid());

        if (!gameTitles.containsKey(player.getUuid())) {
            gameTitles.putIfAbsent(player.getUuid(), config.isGameTitlesDefault());
            manhuntSounds.putIfAbsent(player.getUuid(), config.isManhuntSoundsDefault());
            nightVision.putIfAbsent(player.getUuid(), false);
            friendlyFire.putIfAbsent(player.getUuid(), true);
            bedExplosions.putIfAbsent(player.getUuid(), config.isBedExplosionsDefault());
            lavaPvpInNether.putIfAbsent(player.getUuid(), config.isLavaPvpInNetherDefault());

            DataContainer dataContainer = table.get(player.getUuid());

            if (dataContainer != null) {
                gameTitles.put(player.getUuid(), dataContainer.getBool("game_titles"));
                manhuntSounds.put(player.getUuid(), dataContainer.getBool("manhunt_sounds"));
                nightVision.put(player.getUuid(), dataContainer.getBool("night_vision"));
                friendlyFire.put(player.getUuid(), dataContainer.getBool("friendly_fire"));
                bedExplosions.put(player.getUuid(), dataContainer.getBool("bed_explosions"));
                lavaPvpInNether.put(player.getUuid(), dataContainer.getBool("lava_pvp_in_nether"));
            }
        }
    }

    public static void playerLeave(ServerPlayNetworkHandler handler) {
        ServerPlayerEntity player = handler.player;

        if (state == GameState.PLAYING) {
            if (paused) {
                leftOnPause.add(player.getUuid());
            } else {
                if (player.getScoreboard().getTeam("runners")
                        .getPlayerList().contains(player.getNameForScoreboard()) &&
                        player.getScoreboard().getTeam("runners").getPlayerList().size() == 1) {
                    leftOnPause.add(player.getUuid());

                    PauseCommand.pauseGame(player.getServer());
                }
            }
        }

        DataContainer dataContainer = table.getOrCreateDataContainer(player.getUuid());

        dataContainer.put("game_titles", gameTitles.get(player.getUuid()));
        dataContainer.put("manhunt_sounds", manhuntSounds.get(player.getUuid()));
        dataContainer.put("night_vision", nightVision.get(player.getUuid()));
        dataContainer.put("friendly_fire", friendlyFire.get(player.getUuid()));
        dataContainer.put("bed_explosions", bedExplosions.get(player.getUuid()));
        dataContainer.put("lava_pvp_in_nether", lavaPvpInNether.get(player.getUuid()));
    }

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (state == GameState.PREGAME) {
            if (stack.getItem() == Items.PLAYER_HEAD) {
                ManhuntSettings.openPreferencesGui((ServerPlayerEntity) player);
            }

            if (stack.getItem() == Items.RECOVERY_COMPASS) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    if (!player.getScoreboard().getTeam("hunters").getPlayerList().contains(player.getNameForScoreboard())) {
                        player.getScoreboard().addScoreHolderToTeam(
                                player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters")
                        );

                        player.getServer().getPlayerManager().broadcast(Text.translatable("chat.joined_team",
                                Text.literal(player.getNameForScoreboard())
                                        .formatted(config.getHuntersColor()),
                                Text.translatable("role.manhunt.hunters")
                                        .formatted(config.getHuntersColor())
                                ), false
                        );

                        player.playSoundToPlayer(
                                SoundEvents.ITEM_LODESTONE_COMPASS_LOCK,
                                SoundCategory.PLAYERS,
                                0.5f,
                                1f
                        );
                    } else {
                        player.sendMessage(Text.translatable("chat.already_team",
                                Text.translatable("role.manhunt.hunter"))
                                        .formatted(Formatting.RED
                                ), false
                        );
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.getItem() == Items.RED_CONCRETE) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    allReadyUps.add(player.getUuid());

                    player.getServer().getPlayerManager().broadcast(Text.translatable(
                            "chat.ready",
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
                                    .setStyle(Style.EMPTY.withColor(Formatting.GREEN)
                                    .withItalic(false))
                    );
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 1f);

                    if (allReadyUps.size() == player.getServer().getPlayerManager().getPlayerList().size()) {
                        if (player.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                            player.getServer().getPlayerManager().broadcast(Text.translatable("chat.minimum",
                                            Text.translatable("role.manhunt.runner"))
                                    .formatted(Formatting.RED), false
                            );
                        } else {
                            gameStart(player.getServer());
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.getItem() == Items.LIME_CONCRETE) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    allReadyUps.remove(player.getUuid());

                    player.getServer().getPlayerManager().broadcast(Text.translatable("chat.not_ready",
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
                                    .setStyle(Style.EMPTY.withColor(Formatting.RED)
                                    .withItalic(false))
                    );
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5f, 1f);
                } else {
                    player.sendMessage(Text.translatable("chat.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.getItem() == Items.CLOCK) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    if (!player.getScoreboard().getTeam("runners").getPlayerList().contains(player.getNameForScoreboard())) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

                        player.getServer().getPlayerManager().broadcast(Text.translatable(
                                "chat.joined_team",
                                Text.literal(player.getNameForScoreboard())
                                        .formatted(config.getRunnersColor()),
                                Text.translatable("role.manhunt.runners")
                                        .formatted(config.getRunnersColor())
                                ), false
                        );

                        player.playSoundToPlayer(
                                SoundEvents.ENTITY_ENDER_EYE_LAUNCH,
                                SoundCategory.PLAYERS,
                                0.5f,
                                1f
                        );
                    } else {
                        player.sendMessage(Text.translatable("chat.already_team",
                                Text.translatable("role.manhunt.runner"))
                                .formatted(Formatting.RED),
                                false
                        );
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.slow_down")
                            .formatted(Formatting.RED)
                    );
                }
            }

            if (stack.getItem() == Items.COMMAND_BLOCK) {
                ManhuntSettings.openSettingsGui((ServerPlayerEntity) player);
            }
        }

        if (state == GameState.PLAYING) {
            if (paused) {
                return TypedActionResult.fail(stack);
            }

            if (stack.getItem() == Items.COMPASS && stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker") && !config.isAutomaticCompass() && player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && !allRunners.isEmpty() && !player.isSpectator() && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                player.getItemCooldownManager().set(stack.getItem(), 20);

                if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name") != null) {
                    NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                    nbt.putString("Name", allRunners.get(0).getName().getString());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                }

                ServerPlayerEntity trackedPlayer = player.getServer().getPlayerManager().getPlayer(
                        stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));

                if (trackedPlayer != null) {
                    updateCompass((ServerPlayerEntity) player, stack, trackedPlayer);
                    player.playSoundToPlayer(
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            SoundCategory.MASTER,
                            0.1f,
                            0.5f
                    );
                }
            }

            if (!config.isLavaPvpInNether()) {
                if (world.getRegistryKey() == getTheNether().getRegistryKey() && stack.getItem() == Items.LAVA_BUCKET) {
                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("chat.disabled_if_close").formatted(Formatting.RED));
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }
        }

        return TypedActionResult.pass(stack);
    }

    public static ActionResult useBlock(PlayerEntity player, World world, Hand hand, HitResult hitResult) {
        if (!config.isBedExplosions()) {
            if (world.getRegistryKey() != getOverworld().getRegistryKey()) {
                Vec3d pos = hitResult.getPos();
                Block block = player.getWorld().getBlockState(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)).getBlock();
                if (
                        player.getStackInHand(hand).getName().getString().toLowerCase().contains(" bed")
                                ||
                        block.getName().getString().toLowerCase().contains(" bed")
                ) {
                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0f && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("chat.disabled_if_close").formatted(Formatting.RED));
                            return ActionResult.FAIL;
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

        ServerWorld lobby = server.getWorld(lobbyRegistry);

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
        player.teleport(player.getServer().getWorld(lobbyRegistry), lobbySpawn.getX(), lobbySpawn.getY(), lobbySpawn.getZ(), PositionFlag.ROT, 180f, 0);
    }
}
