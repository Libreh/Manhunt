package manhunt.game;

import manhunt.ManhuntMod;
import manhunt.command.PauseCommand;
import manhunt.command.UnpauseCommand;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.mrnavastar.sqlib.DataContainer;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
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
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

import java.io.IOException;
import java.util.LinkedList;

import static manhunt.ManhuntMod.*;
import static manhunt.game.ManhuntGame.endGame;
import static manhunt.game.ManhuntGame.setPlayerSpawn;

public class Events {
    public static void serverStart(MinecraftServer server) {
        ServerWorld lobby = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, lobbyKey));

        lobby.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        lobby.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        lobby.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
        lobby.getGameRules().get(GameRules.DO_MOB_LOOT).set(false, server);
        lobby.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        lobby.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        lobby.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        lobby.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);
        lobby.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(0, server);
        lobby.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        lobby.getGameRules().get(GameRules.SPAWN_RADIUS).set(0, server);
        lobby.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);
        lobby.getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(config.isSpectatorsGenerateChunks(), server);

        Scoreboard scoreboard = server.getScoreboard();

        for (Team team : scoreboard.getTeams()) {
            String name = team.getName();

            if (name.equals("hunters") || name.equals("runners")) {
                scoreboard.removeTeam(scoreboard.getTeam(name));
            }
        }

        scoreboard.addTeam("hunters");
        scoreboard.addTeam("runners");
        scoreboard.getTeam("hunters").setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        scoreboard.getTeam("runners").setCollisionRule(AbstractTeam.CollisionRule.NEVER);

        if (config.isTeamColor()) {
            scoreboard.getTeam("hunters").setColor(config.getHuntersColor());
            scoreboard.getTeam("runners").setColor(config.getRunnersColor());
        } else {
            scoreboard.getTeam("hunters").setColor(Formatting.RESET);
            scoreboard.getTeam("runners").setColor(Formatting.RESET);

        }

        if (config.isTeamSuffix()) {
            scoreboard.getTeam("hunters").setSuffix(Text.translatable("manhunt.chat.team_suffix", Text.literal("H").formatted(config.getHuntersColor())).formatted(Formatting.GRAY));
            scoreboard.getTeam("runners").setSuffix(Text.translatable("manhunt.chat.team_suffix", Text.literal("R").formatted(config.getRunnersColor())).formatted(Formatting.GRAY));
        } else {
            scoreboard.getTeam("hunters").setSuffix(Text.literal(""));
            scoreboard.getTeam("runners").setSuffix(Text.literal(""));
        }

        try {
            spawnStructure(server);
        } catch (IOException e) {
            LOGGER.fatal("Failed to spawn Manhunt mod lobby");
        }
    }

    public static void serverTick(MinecraftServer server) {
        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!hasItem(player, Items.PLAYER_HEAD, "Preferences")) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Preferences", true);

                    ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.item.preferences").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
                    stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(0, stack);
                } else {
                    int amount = 0;
                    for (ItemStack stack : player.getInventory().main) {
                        if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Preferences")) {
                            amount++;
                            if (amount > 1 || player.getInventory().getSlotWithStack(stack) != 0) {
                                player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                            }
                        }
                    }
                }

                if (config.getTeamPreset() == 1) {
                    if (!hasItem(player, Items.RECOVERY_COMPASS, "JoinHunters")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);
                        nbt.putBoolean("JoinHunters", true);

                        ItemStack stack = new ItemStack(Items.RECOVERY_COMPASS);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.item.join_hunters").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(2, stack);
                    } else {
                        int amount = 0;
                        for (ItemStack stack : player.getInventory().main) {
                            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("JoinHunters")) {
                                amount++;
                                if (amount > 1 || player.getInventory().getSlotWithStack(stack) != 2) {
                                    player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                                }
                            }
                        }
                    }

                    if (!hasItem(player, Items.CLOCK, "JoinRunners")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);
                        nbt.putBoolean("JoinRunners", true);

                        ItemStack stack = new ItemStack(Items.CLOCK);
                        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.item.join_runners").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(6, stack);
                    } else {
                        int amount = 0;
                        for (ItemStack stack : player.getInventory().main) {
                            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("JoinRunners")) {
                                amount++;
                                if (amount > 1 || player.getInventory().getSlotWithStack(stack) != 6) {
                                    player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                                }
                            }
                        }
                    }
                } else {
                    for (ItemStack stack : player.getInventory().main) {
                        if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("JoinHunters") || stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("JoinRunners"))) {
                            player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                        }
                    }
                }

                if (!hasItem(player, Items.RED_CONCRETE, "NotReady") && !hasItem(player, Items.LIME_CONCRETE, "Ready")) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);

                    ItemStack stack = new ItemStack(Items.RED_CONCRETE);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.item.not_ready").setStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(false)));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, stack);
                } else {
                    if (hasItem(player, Items.RED_CONCRETE, "NotReady")) {
                        int amount = 0;
                        for (ItemStack stack : player.getInventory().main) {
                            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("NotReady")) {
                                amount++;
                                if (amount > 1 || player.getInventory().getSlotWithStack(stack) != 4) {
                                    player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                                }
                            }
                        }
                    }
                    if (hasItem(player, Items.LIME_CONCRETE, "Ready")) {
                        int amount = 0;
                        for (ItemStack stack : player.getInventory().main) {
                            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Ready")) {
                                amount++;
                                if (amount > 1) {
                                    player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                                }
                            }
                        }
                    }
                }

                if (!hasItem(player, Items.COMMAND_BLOCK, "Settings")) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Settings", true);

                    ItemStack stack = new ItemStack(Items.COMMAND_BLOCK);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.item.settings").setStyle(Style.EMPTY.withColor(Formatting.AQUA).withItalic(false)));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(8, stack);
                } else {
                    int amount = 0;
                    for (ItemStack stack : player.getInventory().main) {
                        if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Settings")) {
                            amount++;
                            if (amount > 1 || player.getInventory().getSlotWithStack(stack) != 8) {
                                player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
                            }
                        }
                    }
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
                                player.sendMessage(Text.translatable("manhunt.chat.parkour_time", secSeconds, msSeconds), true);
                                parkourTimer.put(player.getUuid(), parkourTimer.get(player.getUuid()) + 1);
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 255, false, false, false));

                                if (player.getZ() < -24 && player.getZ() > -27 && player.getX() < -6 && player.getY() >= 70 && player.getY() < 72) {
                                    player.sendMessage(Text.translatable("manhunt.chat.parkour_time", secSeconds, msSeconds).formatted(Formatting.GREEN), true);
                                    player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1f, 2f);
                                    finishedParkour.put(player.getUuid(), true);
                                }
                            }

                            if (player.getZ() > -4 || player.getY() < 61 || (player.getZ() < -27 && player.getY() < 68)) {
                                player.sendMessage(Text.translatable("manhunt.chat.parkour_time", secSeconds, msSeconds).formatted(Formatting.RED), true);
                                resetPlayer(player, player.getServerWorld());
                                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1f, 0.5f);
                            }
                        }
                    }

                    if ((player.getX() < -24 || player.getX() > 24) || (player.getY() < 54 || player.getY() > 74) || (player.getZ() < -64 || player.getZ() > 32)) {
                        resetPlayer(player, player.getServerWorld());
                    }
                }

                if (player.getWorld().getRegistryKey() != lobbyWorld) {
                    resetPlayer(player, player.getServerWorld());
                }

                if (player.getWorld().getRegistryKey() != lobbyWorld) {
                    player.teleport(server.getWorld(lobbyWorld), 0.5, 63, 0.5, 0F, 0F);
                }
            }
        }

        if (ManhuntMod.getGameState() == GameState.PLAYING) {
            allRunners = new LinkedList<>();

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                    allRunners.add(player);
                }

                if (player.getWorld().getRegistryKey() == lobbyWorld) {
                    if (!playerSpawn.containsKey(player.getUuid())) {
                        ManhuntGame.setPlayerSpawn(overworld, player);
                    }

                    player.teleport(overworld, playerSpawn.get(player.getUuid()).getX(), playerSpawn.get(player.getUuid()).getY(), playerSpawn.get(player.getUuid()).getZ(), 0F, 0F);
                    player.setSpawnPoint(overworldWorld, playerSpawn.get(player.getUuid()), 0, true, false);
                }
            }

            if (config.getTimeLimit() != 0) {
                if (overworld.getTime() % (20 * 60 * 60) / (20 * 60) >= config.getTimeLimit()) {
                    endGame(server, true, true);
                }

                String hoursString;
                int hours = (int) Math.floor((double) overworld.getTime() % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                if (hours <= 9) {
                    hoursString = "0" + hours;
                } else {
                    hoursString = String.valueOf(hours);
                }
                String minutesString;
                int minutes = (int) Math.floor((double) overworld.getTime() % (20 * 60 * 60) / (20 * 60));
                if (minutes <= 9) {
                    minutesString = "0" + minutes;
                } else {
                    minutesString = String.valueOf(minutes);
                }
                String secondsString;
                int seconds = (int) Math.floor((double) overworld.getTime() % (20 * 60) / (20));
                if (seconds <= 9) {
                    secondsString = "0" + seconds;
                } else {
                    secondsString = String.valueOf(seconds);
                }

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(Text.translatable("manhunt.chat.duration", hoursString, minutesString, secondsString), true);
                }
            }

            if (isPaused()) {
                setPauseTime(getPauseTime() + 1);

                String hoursString;
                int hours = (int) Math.floor((double) getPauseTime() % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                if (hours <= 9) {
                    hoursString = "0" + hours;
                } else {
                    hoursString = String.valueOf(hours);
                }
                String minutesString;
                int minutes = (int) Math.floor((double) getPauseTime() % (20 * 60 * 60) / (20 * 60));
                if (minutes <= 9) {
                    minutesString = "0" + minutes;
                } else {
                    minutesString = String.valueOf(minutes);
                }
                String secondsString;
                int seconds = (int) Math.floor((double) getPauseTime() % (20 * 60) / (20));
                if (seconds <= 9) {
                    secondsString = "0" + seconds;
                } else {
                    secondsString = String.valueOf(seconds);
                }

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (playerPos.containsKey(player.getUuid())) {
                        player.teleport(server.getWorld(player.getWorld().getRegistryKey()), playerPos.get(player.getUuid()).getX(), playerPos.get(player.getUuid()).getY(), playerPos.get(player.getUuid()).getZ(), playerYaw.get(player.getUuid()), playerPitch.get(player.getUuid()));
                    } else {
                        playerPos.put(player.getUuid(), player.getPos());
                    }

                    player.sendMessage(Text.translatable("manhunt.chat.paused_for", hoursString, minutesString, secondsString).formatted(Formatting.YELLOW), true);
                }

                if (minutes >= config.getPauseTimeOnLeave()) {
                    setPauseTime(0);

                    UnpauseCommand.unpauseGame(server);
                }
            }

            if (isHeadstart()) {
                setHeadstartTime(getHeadstartTime() + 1);

                if (getHeadstartTime() >= 20) {
                    int seconds = (int) Math.floor((double) getHeadstartTime() % (20 * 60) / (20));

                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        if (player.getScoreboardTeam() != null && player.getScoreboardTeam() == player.getScoreboard().getTeam("hunters")) {
                            if (playerSpawn.get(player.getUuid()).getX() == player.getBlockX()) {
                                if (playerPos.containsKey(player.getUuid()) && playerYaw.containsKey(player.getUuid()) && playerPitch.containsKey(player.getUuid())) {
                                    player.teleport(server.getWorld(player.getWorld().getRegistryKey()), playerPos.get(player.getUuid()).getX(), playerPos.get(player.getUuid()).getY(), playerPos.get(player.getUuid()).getZ(), playerYaw.get(player.getUuid()), playerPitch.get(player.getUuid()));
                                } else {
                                    playerPos.put(player.getUuid(), player.getPos());
                                    playerYaw.put(player.getUuid(), player.getYaw());
                                    playerPitch.put(player.getUuid(), player.getPitch());
                                }
                            } else {
                                setPlayerSpawn(overworld, player);
                            }
                        }
                    }

                    if (seconds >= config.getRunnerHeadstart()) {
                        setHeadstart(false);

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            if (player.getScoreboardTeam() != null && player.getScoreboardTeam() == player.getScoreboard().getTeam("hunters")) {
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
    }

    public static void playerJoin(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;

        if (!slowDownManager.containsKey(player.getUuid())) {
            slowDownManager.put(player.getUuid(), 0);
        }

        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            player.getInventory().clear();
            player.changeGameMode(GameMode.ADVENTURE);
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setFireTicks(0);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);
            player.clearStatusEffects();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));

            for (AdvancementEntry advancement : server.getAdvancementLoader().getAdvancements()) {
                AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                for (String criteria : progress.getObtainedCriteria()) {
                    player.getAdvancementTracker().revokeCriterion(advancement, criteria);
                }
            }

            if (config.getTeamPreset() == 1) {
                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
            } else {
                if (config.getTeamPreset() == 2) {
                    if (player.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                    } else {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
                    }
                } else if (config.getTeamPreset() == 3) {
                    if (player.getScoreboard().getTeam("hunters").getPlayerList().isEmpty()) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
                    } else {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                    }
                } else {
                    player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));
                }
            }

            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
            player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
            player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

            ManhuntGame.setPlayerSpawn(overworld, player);
        }

        if (getGameState() == GameState.PLAYING) {
            if (!hasPlayed.contains(player.getUuid())) {
                hasPlayed.add(player.getUuid());

                player.teleport(server.getWorld(RegistryKey.of(RegistryKeys.WORLD, lobbyKey)), 0.5, 63, 0.5, PositionFlag.ROT, 0, 0);
                player.clearStatusEffects();
                player.getInventory().clear();
                player.setFireTicks(0);
                player.setOnFire(false);
                player.setHealth(20);
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(5);
                player.getHungerManager().setExhaustion(0);

                if (getGameState() == GameState.PLAYING) {
                    player.changeGameMode(GameMode.SURVIVAL);
                } else if (getGameState() == GameState.POSTGAME && config.isSpectateOnWin()) {
                    player.changeGameMode(GameMode.SPECTATOR);
                }

                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
            }

            if (isPaused()) {
                if (!leftOnPause.contains(player.getUuid())) {
                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 0.5f);
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
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, StatusEffectInstance.INFINITE, 255, false, false,false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getGamePausedTitle()).formatted(Formatting.YELLOW)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getGamePausedSubtitle()).formatted(Formatting.GOLD)));
                }

                if (player.getScoreboard().getTeam("runners").getPlayerList().size() == 1) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                    player.clearStatusEffects();
                    if (playerEffects.containsKey(player.getUuid())) {
                        for (StatusEffectInstance statusEffect : playerEffects.get(player.getUuid())) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                    player.getHungerManager().setFoodLevel(playerFood.get(player.getUuid()));
                    player.getHungerManager().setSaturationLevel(playerSaturation.get(player.getUuid()));
                    player.getHungerManager().setExhaustion(playerExhuastion.get(player.getUuid()));
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getGameUnpausedTitle()).formatted(Formatting.YELLOW)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getGameUnpausedSubtitle()).formatted(Formatting.GOLD)));

                    UnpauseCommand.unpauseGame(player.getServer());
                }
            } else {
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

                if (leftOnPause.contains(player.getUuid())) {
                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                    player.clearStatusEffects();
                    if (playerEffects.containsKey(player.getUuid())) {
                        for (StatusEffectInstance statusEffect : playerEffects.get(player.getUuid())) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getGameUnpausedTitle()).formatted(Formatting.YELLOW)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getGameUnpausedSubtitle()).formatted(Formatting.GOLD)));
                }
            }
        }

        if (!gameTitles.containsKey(player.getUuid())) {
            gameTitles.putIfAbsent(player.getUuid(), config.isGameTitlesDefault());
            manhuntSounds.putIfAbsent(player.getUuid(), config.isManhuntSoundsDefault());
            nightVision.putIfAbsent(player.getUuid(), false);
            friendlyFire.putIfAbsent(player.getUuid(), true);
            bedExplosions.putIfAbsent(player.getUuid(), config.isBedExplosionsDefault());
            lavaPvpInNether.putIfAbsent(player.getUuid(), config.isLavaPvpInNetherDefault());

            DataContainer dataContainer = ManhuntMod.getTable().get(player.getUuid());

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

        if (getGameState() == GameState.PLAYING) {
            if (isPaused()) {
                leftOnPause.add(player.getUuid());
            } else {
                if (player.getScoreboard().getTeam("runners").getPlayerList().contains(player.getNameForScoreboard()) && player.getScoreboard().getTeam("runners").getPlayerList().size() == 1) {
                    leftOnPause.add(player.getUuid());

                    PauseCommand.pauseGame(player.getServer());
                }
            }
        }

        DataContainer dataContainer = ManhuntMod.getTable().getOrCreateDataContainer(player.getUuid());

        dataContainer.put("game_titles", gameTitles.get(player.getUuid()));
        dataContainer.put("manhunt_sounds", manhuntSounds.get(player.getUuid()));
        dataContainer.put("night_vision", nightVision.get(player.getUuid()));
        dataContainer.put("friendly_fire", friendlyFire.get(player.getUuid()));
        dataContainer.put("bed_explosions", bedExplosions.get(player.getUuid()));
        dataContainer.put("lava_pvp_in_nether", lavaPvpInNether.get(player.getUuid()));
    }

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Preferences")) {
                ManhuntSettings.openPreferencesGui((ServerPlayerEntity) player);
            }

            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("JoinHunters")) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    if (!player.getScoreboard().getTeam("hunters").getPlayerList().contains(player.getNameForScoreboard())) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));

                        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.joined_team", Text.literal(player.getNameForScoreboard()).formatted(config.getHuntersColor()), Text.translatable("manhunt.role.hunters").formatted(config.getHuntersColor())), false);

                        player.playSoundToPlayer(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.5f, 1f);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.already_team", Text.translatable("manhunt.role.hunter").formatted(Formatting.RED)), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("NotReady")) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    readyList.add(player.getUuid());

                    player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.ready", Text.literal(player.getNameForScoreboard()).formatted(Formatting.GREEN)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Ready", true);

                    ItemStack itemStack = new ItemStack(Items.LIME_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.item.ready").setStyle(Style.EMPTY.withColor(Formatting.GREEN).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 1.0f);

                    if (readyList.size() == player.getServer().getPlayerManager().getPlayerList().size()) {
                        if (player.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                            player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.minimum", Text.translatable("manhunt.role.runner")).formatted(Formatting.RED), false);
                        } else {
                            ManhuntGame.startGame(player.getServer());
                        }
                    }
                } else {
                    player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Ready")) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    readyList.remove(player.getUuid());

                    player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.not_ready", Text.literal(player.getNameForScoreboard()).formatted(Formatting.RED)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);

                    ItemStack itemStack = new ItemStack(Items.LIME_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.item.not_ready").setStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5f, 1.0f);
                } else {
                    player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("JoinRunners")) {
                if (slowDownManager.get(player.getUuid()) < 8) slowDownManager.put(player.getUuid(), slowDownManager.get(player.getUuid()) + 1);
                if (slowDownManager.get(player.getUuid()) < 4) {
                    if (!player.getScoreboard().getTeam("runners").getPlayerList().contains(player.getNameForScoreboard())) {
                        player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("runners"));

                        player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.joined_team", Text.literal(player.getNameForScoreboard()).formatted(config.getRunnersColor()), Text.translatable("manhunt.role.runners").formatted(config.getRunnersColor())), false);

                        player.playSoundToPlayer(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.5f, 1f);
                    } else {
                        player.sendMessage(Text.translatable("manhunt.chat.already_team", Text.translatable("manhunt.role.runner").formatted(Formatting.RED)), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("manhunt.chat.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Settings")) {
                ManhuntSettings.openSettingsGui((ServerPlayerEntity) player);
            }
        }

        if (getGameState() == GameState.PLAYING) {
            if (isPaused()) {
                return TypedActionResult.fail(stack);
            }

            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker") && !config.isAutomaticCompass() && player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && allRunners != null && !allRunners.isEmpty() && !player.isSpectator() && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                player.getItemCooldownManager().set(stack.getItem(), 20);

                if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name") != null) {
                    NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                    nbt.putString("Name", allRunners.get(0).getName().getString());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                }

                ServerPlayerEntity trackedPlayer = player.getServer().getPlayerManager().getPlayer(stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));

                if (trackedPlayer != null) {
                    ManhuntGame.updateCompass((ServerPlayerEntity) player, stack, trackedPlayer);
                    player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.1f, 0.5f);
                }
            }

            if (!config.isBedExplosions()) {
                if (player.getWorld().getRegistryKey() != overworldWorld && stack.getName().toString().contains("_bed")) {
                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0F && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("manhunt.chat.disabled_if_close").formatted(Formatting.RED));
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }

            if (!config.isLavaPvpInNether()) {
                if (player.getWorld().getRegistryKey() == netherWorld && stack.getItem() == Items.LAVA_BUCKET) {
                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0F && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("manhunt.chat.disabled_if_close").formatted(Formatting.RED));
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }
        }

        return TypedActionResult.pass(stack);
    }

    public static void playerRespawn(ServerPlayerEntity player) {
        if (player.isTeamPlayer(player.getScoreboard().getTeam("runners")) && getGameState() == GameState.PLAYING) {
            if (!config.isRunnersHuntOnDeath()) {
                player.changeGameMode(GameMode.SPECTATOR);
            }
        }
    }

    private static void spawnStructure(MinecraftServer server) throws IOException {
        NbtCompound islandNbt = NbtIo.readCompressed(ManhuntMod.class.getResourceAsStream("/manhunt/lobby/island.nbt"), NbtSizeTracker.ofUnlimitedBytes());
        NbtCompound parkourNbt = NbtIo.readCompressed(ManhuntMod.class.getResourceAsStream("/manhunt/lobby/parkour.nbt"), NbtSizeTracker.ofUnlimitedBytes());

        ServerWorld lobby = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, lobbyKey));

        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 0), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, 0), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, -15), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, -15), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 16), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 0), 16, Unit.INSTANCE);
        lobby.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 16), 16, Unit.INSTANCE);
        placeStructure(lobby, new BlockPos(-21, 54, -6), islandNbt);
        placeStructure(lobby, new BlockPos(-21, 54, -54), parkourNbt);
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

    private static boolean hasItem(PlayerEntity player, Item item, String name) {
        boolean bool = false;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == item && stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean(name)) {
                bool = true;
                break;
            }
        }

        if (player.playerScreenHandler.getCursorStack().get(DataComponentTypes.CUSTOM_DATA) != null && player.playerScreenHandler.getCursorStack().get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean(name)) {
            bool = true;
        } else if (player.getOffHandStack().get(DataComponentTypes.CUSTOM_DATA) != null && player.getOffHandStack().get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean(name)) {
            bool = true;
        }

        return bool;
    }

    private static void resetPlayer(ServerPlayerEntity player, ServerWorld world) {
        parkourTimer.put(player.getUuid(), 0);
        startedParkour.put(player.getUuid(), false);
        finishedParkour.put(player.getUuid(), false);
        player.changeGameMode(GameMode.ADVENTURE);
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        player.teleport(world, 0.5, 63, 0.5, PositionFlag.ROT, 180, 0);
    }
}
