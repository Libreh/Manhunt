package libreh.manhunt.event;

import libreh.manhunt.command.game.PauseCommand;
import libreh.manhunt.command.game.UnpauseCommand;
import libreh.manhunt.command.lobby.ResetCommand;
import libreh.manhunt.config.ManhuntConfig;
import libreh.manhunt.config.gui.GameOptionsGui;
import libreh.manhunt.config.gui.SettingsGui;
import libreh.manhunt.game.GameState;
import libreh.manhunt.game.ManhuntGame;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
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
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.*;

import static libreh.manhunt.ManhuntMod.*;

public class OnGameTick {
    public static final HashMap<UUID, Integer> PARKOUR_TIMER = new HashMap<>();
    public static final HashMap<UUID, Boolean> STARTED_PARKOUR = new HashMap<>();
    public static final HashMap<UUID, Boolean> FINISHED_PARKOUR = new HashMap<>();
    public static final List<UUID> JOIN_LIST = new ArrayList<>();
    public static final List<UUID> START_LIST = new ArrayList<>();
    public static final List<UUID> RESET_LIST = new ArrayList<>();
    public static final List<UUID> LEFT_ON_PAUSE = new ArrayList<>();
    public static final HashMap<UUID, BlockPos> PLAYER_SPAWN_POS = new HashMap<>();
    public static final HashMap<UUID, BlockPos> HEAD_START_POS = new HashMap<>();
    public static final BlockPos LOBBY_SPAWN_INT = new BlockPos(0, 0, 0);
    public static final Vec3d LOBBY_SPAWN_DOUBLE = new Vec3d(0.5, 0, 0.5);
    public static boolean starting;
    public static boolean waitForRunner;
    public static boolean hunterRelease;
    public static boolean headStart;
    public static boolean timeLimit;
    public static boolean paused;
    public static int startingTime;
    public static int hunterReleaseTime;
    public static int headStartTime;
    public static int timeLimitLeft;
    public static int pauseTimeLeft;
    public static List<ServerPlayerEntity> allRunners;
    public static List<ServerPlayerEntity> allHunters;
    public static boolean runnerHasStarted;
    public static boolean startReset = true;
    private static int tickCount;

    public static void onGameTick(MinecraftServer server) {
        var scoreboard = server.getScoreboard();
        var huntersTeam = server.getScoreboard().getTeam("hunters");
        var runnersTeam = server.getScoreboard().getTeam("runners");
        if (gameState == GameState.PREGAME) {
            if (!startReset) {
                boolean reset = false;
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (player != null && player.getWorld().getRegistryKey() != LOBBY_REGISTRY_KEY) {
                        reset = true;
                        break;
                    }
                }
                if (!reset) {
                    loadManhuntWorlds(server, ResetCommand.seed);
                    startReset = true;
                }
            }
            tickCount++;
            if (tickCount == 19) {
                if (starting && !paused) {
                    startingTime -= 20;
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
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.manhunt.starting_in",
                                    Text.literal(seconds + " seconds").styled(style -> style.withColor(color))).styled(style -> style.withColor(5766999))));
                            if (pitch != 0.0F) {
                                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(),
                                        SoundCategory.MASTER, 0.5F, pitch);
                                if (pitch == 0.5F) {
                                    player.playSoundToPlayer(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.MASTER,
                                            0.1F, pitch);
                                }
                            }

                        }
                    } else {
                        starting = false;
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new ClearTitleS2CPacket(true));
                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 20, 5));
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.manhunt.teleporting").styled(style -> style.withColor(11686066))));
                        }
                        ManhuntGame.start(server);
                    }
                }
                tickCount = 0;
            }
        } else if (gameState == GameState.PLAYING) {
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

            tickCount++;
            if (tickCount == 19) {
                if (paused) {
                    pauseTimeLeft -= 20;

                    int time = (int) Math.floor((double) pauseTimeLeft % (20 * 60 * 60) / (20 * 60));

                    if (time >= ManhuntConfig.CONFIG.getLeavePauseTime()) {
                        pauseTimeLeft = 0;
                        UnpauseCommand.unpauseGame(server);

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new ClearTitleS2CPacket(false));
                        }
                    } else {
                        String hoursString;
                        int hours =
                                (int) Math.floor((double) pauseTimeLeft % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                        if (hours <= 9) {
                            hoursString = "0" + hours;
                        } else {
                            hoursString = String.valueOf(hours);
                        }
                        String minutesString;
                        int minutes =
                                (int) Math.floor((double) pauseTimeLeft % (20 * 60 * 60) / (20 * 60));
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
                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 20, 5));
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(ManhuntConfig.CONFIG.getPausedTitle()).formatted(Formatting.YELLOW)));
                            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("chat.manhunt.time.triple", hoursString, minutesString, secondsString).formatted(Formatting.GOLD)));
                        }
                    }
                } else {
                    if (waitForRunner) {
                        if (!runnerHasStarted) {
                            if (ManhuntConfig.CONFIG.getHunterReleaseTime() != 0) {
                                hunterReleaseTime -= 20;
                                if (hunterReleaseTime >= 20) {
                                    int seconds =
                                            (int) Math.floor((double) hunterReleaseTime % (20 * 60) / (20));

                                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                        player.sendMessage(Text.translatable("chat.manhunt.hunter_release_time.start"
                                                        , Text.literal(String.valueOf(seconds))).formatted(Formatting.AQUA),
                                                true);
                                    }
                                } else {
                                    hunterRelease = true;
                                    runnerHasStarted = true;
                                }
                            } else {
                                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                    player.sendMessage(Text.translatable("chat.manhunt.wait_for_runner.start").formatted(Formatting.AQUA), true);
                                }
                            }
                        } else {
                            if (headStart) {
                                headStartTime -= 20;
                                if (headStartTime >= 20) {
                                    int seconds =
                                            (int) Math.floor((double) headStartTime % (20 * 60) / (20));
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
                                        player.sendMessage(Text.translatable("chat.manhunt.runner_head_start",
                                                Text.literal(String.valueOf(seconds)).styled(style -> style.withColor(color))).styled(style -> style.withColor(5766999)), true);
                                        if (pitch != 0.0F) {
                                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
                                                    SoundCategory.MASTER, 0.5F, pitch);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (timeLimit) {
                        timeLimitLeft -= 20;
                        if (timeLimitLeft % (20 * 60 * 60) / (20 * 60) >= ManhuntConfig.CONFIG.getTimeLimit() && gameState == GameState.PLAYING) {
                            ManhuntGame.end(server, true);
                            return;
                        } else {
                            String hoursString;
                            int hours =
                                    (int) Math.floor((double) timeLimitLeft % (20 * 60 * 60 * 24) / (20 * 60 * 60));
                            if (hours <= 9) {
                                hoursString = "0" + hours;
                            } else {
                                hoursString = String.valueOf(hours);
                            }
                            String minutesString;
                            int minutes =
                                    (int) Math.floor((double) timeLimitLeft % (20 * 60 * 60) / (20 * 60));
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

                            if (!ManhuntConfig.CONFIG.isWaitForRunner() && ManhuntConfig.CONFIG.getHunterReleaseTime() != 0 && ManhuntConfig.CONFIG.getRunnerHeadStart() != 0) {
                                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                    player.sendMessage(Text.translatable("chat.manhunt.time.triple", hoursString,
                                            minutesString, secondsString).styled(style -> style.withBold(true)), true);
                                }
                            }
                        }
                    }
                }
                tickCount = 0;
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (JOIN_LIST.contains(player.getUuid()) && !player.notInAnyWorld) {
                JOIN_LIST.remove(player.getUuid());
                if (!ManhuntGame.PLAY_LIST.contains(player.getUuid()) || player.interactionManager.getGameMode() != ManhuntGame.getGameMode() || gameState == GameState.PREGAME && player.getWorld().getRegistryKey() != LOBBY_REGISTRY_KEY || !player.getWorld().getRegistryKey().getValue().getNamespace().equals("fantasy")) {
                    player.clearStatusEffects();
                    player.setFireTicks(0);
                    player.setOnFire(false);
                    player.setHealth(player.getMaxHealth());
                    player.setAir(player.getMaxAir());
                    resetHungerManager(player);
                    player.setExperienceLevel(0);
                    player.setExperiencePoints(0);
                    player.setScore(0);

                    player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED).setBaseValue(1.0);

                    player.getInventory().clear();
                    player.changeGameMode(ManhuntGame.getGameMode());

                    for (AdvancementEntry advancement : player.getServer().getAdvancementLoader().getAdvancements()) {
                        AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                        for (String criteria : progress.getObtainedCriteria()) {
                            player.getAdvancementTracker().revokeCriterion(advancement, criteria);
                        }
                    }

                    if (SettingsGui.NIGHT_VISION.get(player.getUuid())) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                                StatusEffectInstance.INFINITE, 255, false, false, true));
                    }

                    if (gameState == GameState.PREGAME) {
                        player.teleport(server.getWorld(LOBBY_REGISTRY_KEY), LOBBY_SPAWN_DOUBLE.x,
                                LOBBY_SPAWN_DOUBLE.y, LOBBY_SPAWN_DOUBLE.z, Set.of(), 180.0F, 0.0F, true);
                        player.setSpawnPoint(LOBBY_REGISTRY_KEY, LOBBY_SPAWN_INT, 180.0F, true, false);
                    } else {
                        if (!PLAYER_SPAWN_POS.containsKey(player.getUuid())) {
                            ManhuntGame.setPlayerSpawn(getOverworld(), player);
                        }
                        int playerX = PLAYER_SPAWN_POS.get(player.getUuid()).getX();
                        int playerY = PLAYER_SPAWN_POS.get(player.getUuid()).getY();
                        int playerZ = PLAYER_SPAWN_POS.get(player.getUuid()).getZ();
                        player.teleport(getOverworld(), playerX, playerY, playerZ, Set.of(), 0.0F, 0.0F, true);
                        player.setSpawnPoint(getOverworld().getRegistryKey(),
                                PLAYER_SPAWN_POS.get(player.getUuid()), 0.0F,
                                true, false);
                    }
                }
                if (gameState == GameState.PREGAME) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION,
                            StatusEffectInstance.INFINITE, 255, false, false));
                    var rolePreset = ManhuntConfig.CONFIG.getRolePreset();
                    if (rolePreset == 1 || rolePreset == 7) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                    } else {
                        if (rolePreset == 2) {
                            var hunters = 0;
                            var runners = 0;
                            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                if (serverPlayer.isTeamPlayer(huntersTeam)) {
                                    hunters++;
                                } else {
                                    runners++;
                                }
                            }
                            if (hunters > runners) {
                                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                            } else {
                                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                            }
                        } else if (rolePreset == 3) {
                            scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                        } else if (rolePreset == 4) {
                            var runners = 0;
                            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                if (serverPlayer.isTeamPlayer(runnersTeam)) {
                                    runners++;
                                    break;
                                }
                            }
                            if (runners == 0) {
                                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                            } else {
                                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                            }
                        } else if (rolePreset == 5) {
                            var runners = 0;
                            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                if (serverPlayer.isTeamPlayer(runnersTeam)) {
                                    runners++;
                                    break;
                                }
                            }
                            if (runners == 0) {
                                if (GameOptionsGui.mainRunnerUUID != null) {
                                    scoreboard.addScoreHolderToTeam(server.getPlayerManager().getPlayer(GameOptionsGui.mainRunnerUUID).getNameForScoreboard(), runnersTeam);
                                } else {
                                    scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                                }
                            } else {
                                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                            }
                        } else {
                            var hunters = 0;
                            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                if (serverPlayer.isTeamPlayer(huntersTeam)) {
                                    hunters++;
                                    break;
                                }
                            }
                            if (hunters == 0) {
                                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                            } else {
                                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                            }
                        }
                    }
                }
                if (gameState != GameState.POSTGAME) {
                    if (player.getScoreboardTeam() == null) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);
                    }
                }
            }

            if (gameState == GameState.PREGAME) {
                if (RESET_LIST.contains(player.getUuid()) && !player.notInAnyWorld && !player.isDead()) {
                    RESET_LIST.remove(player.getUuid());
                    player.teleport(server.getWorld(LOBBY_REGISTRY_KEY), LOBBY_SPAWN_DOUBLE.getX(),
                            LOBBY_SPAWN_DOUBLE.getY(), LOBBY_SPAWN_DOUBLE.getZ(),
                            Set.of(), 180.0F, 0.0F, true);
                    player.setSpawnPoint(LOBBY_REGISTRY_KEY, LOBBY_SPAWN_INT, 180f, true, false);
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION,
                            StatusEffectInstance.INFINITE, 255, false, false));
                    player.setOnFire(false);
                    player.setFireTicks(0);
                    player.setHealth(player.getMaxHealth());
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

                    player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED).setBaseValue(1.0);

                    if (player.getScoreboardTeam() == null)
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("hunters"));
                }
                if (!hasItem(player, Items.PLAYER_HEAD)) {
                    var nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    var stack = new ItemStack(Items.PLAYER_HEAD);
                    stack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.settings").styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(0, stack);
                } else {
                    removeDuplicateItems(player, Items.PLAYER_HEAD, 0);
                }
                if (ManhuntConfig.CONFIG.getRolePreset() == 1) {
                    if (!hasItem(player, Items.RECOVERY_COMPASS)) {
                        var nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        var stack = new ItemStack(Items.RECOVERY_COMPASS);
                        stack.set(DataComponentTypes.CUSTOM_NAME,
                                Text.translatable("item.manhunt.join_hunters").styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(2, stack);
                    } else {
                        removeDuplicateItems(player, Items.RECOVERY_COMPASS, 2);
                    }

                    if (!hasItem(player, Items.CLOCK)) {
                        var nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        var stack = new ItemStack(Items.CLOCK);
                        stack.set(DataComponentTypes.CUSTOM_NAME,
                                Text.translatable("item.manhunt.join_runners").styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(6, stack);
                    } else {
                        removeDuplicateItems(player, Items.CLOCK, 6);
                    }
                    if (!hasItem(player, Items.RED_CONCRETE) && !hasItem(player,
                            Items.LIME_CONCRETE)) {
                        var nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        var stack = new ItemStack(Items.RED_CONCRETE);
                        stack.set(DataComponentTypes.CUSTOM_NAME,
                                Text.translatable("item.manhunt.not_ready").styled(style -> style.withColor(Formatting.RED).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(4, stack);
                    } else {
                        if (hasItem(player, Items.RED_CONCRETE)) {
                            removeDuplicateItems(player, Items.RED_CONCRETE, 4);
                        }
                        if (hasItem(player, Items.LIME_CONCRETE)) {
                            removeDuplicateItems(player, Items.LIME_CONCRETE, 4);
                        }
                    }
                } else {
                    clearItem(player, Items.RECOVERY_COMPASS);
                    clearItem(player, Items.RED_CONCRETE);
                    clearItem(player, Items.LIME_CONCRETE);
                    clearItem(player, Items.CLOCK);
                }
                if (!hasItem(player, Items.COMMAND_BLOCK)) {
                    var nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    var stack = new ItemStack(Items.COMMAND_BLOCK);
                    stack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.config").styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(8, stack);
                } else {
                    removeDuplicateItems(player, Items.COMMAND_BLOCK, 8);
                }

                if (!Permissions.check(player, "manhunt.parkour") && player.getZ() < -2 && player.getWorld()
                        .getRegistryKey() == LOBBY_REGISTRY_KEY) {
                    PARKOUR_TIMER.putIfAbsent(player.getUuid(), 0);
                    STARTED_PARKOUR.putIfAbsent(player.getUuid(), false);
                    FINISHED_PARKOUR.putIfAbsent(player.getUuid(), false);


                    int tick = PARKOUR_TIMER.get(player.getUuid());
                    String secStr;
                    var sec = (int) Math.floor((double) (tick % (20 * 60)) / (20));
                    if (sec < 10) {
                        secStr = "0" + sec;
                    } else {
                        secStr = String.valueOf(sec);
                    }
                    String msStr;
                    var ms = (int) Math.floor((double) (tick * 5) % 100);
                    if (ms < 10) {
                        msStr = "0" + ms;
                    } else if (ms > 99) {
                        msStr = "00";
                    } else {
                        msStr = String.valueOf(ms);
                    }

                    if (!FINISHED_PARKOUR.get(player.getUuid())) {
                        if (!STARTED_PARKOUR.get(player.getUuid()) && player.getZ() < -3 && !(player.getZ() < -6)) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER
                                    , 1.0F, 1.0F);
                            STARTED_PARKOUR.put(player.getUuid(), true);
                        }

                        if (STARTED_PARKOUR.get(player.getUuid())) {
                            if (player.getZ() < -3) {
                                player.sendMessage(Text.translatable("chat.manhunt.time.double", secStr, msStr), true);
                                PARKOUR_TIMER.put(player.getUuid(),
                                        PARKOUR_TIMER.get(player.getUuid()) + 1);
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY,
                                        StatusEffectInstance.INFINITE, 255, false, false, false));

                                if (player.getZ() < -24 && player.getZ() > -27 && player.getX() < -6 && player.getY()
                                        >= -4 && player.getY() < 8) {
                                    player.sendMessage(Text.translatable("chat.manhunt.time.double", secStr, msStr).formatted(Formatting.GREEN), true);
                                    player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(),
                                            SoundCategory.MASTER, 1.0F, 2.0F);
                                    FINISHED_PARKOUR.put(player.getUuid(), true);
                                }
                            }
                        }
                    }
                    if (STARTED_PARKOUR.get(player.getUuid())) {
                        if (sec > 59 || (player.getZ() > -3 || player.getY() < -2 || (player.getZ() < -27 && player.getY() < 6))) {
                            if (!FINISHED_PARKOUR.get(player.getUuid())) {
                                player.sendMessage(Text.translatable("chat.manhunt.time.double", secStr, msStr).formatted(Formatting.RED), true);
                            }
                            resetLobbyPlayer(player);
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER
                                    , 1.0F, 0.5F);
                        }
                    }
                } else {
                    if (player.getY() < -4) {
                        resetLobbyPlayer(player);
                    }
                }
            }

            if (gameState == GameState.PLAYING) {
                if (START_LIST.contains(player.getUuid()) && !player.notInAnyWorld) {
                    START_LIST.remove(player.getUuid());
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2, 255, false, false,
                            false));
                    if (!PLAYER_SPAWN_POS.containsKey(player.getUuid())) {
                        ManhuntGame.setPlayerSpawn(getOverworld(), player);
                    }
                    double playerX =
                            Double.parseDouble(String.valueOf(PLAYER_SPAWN_POS.get(player.getUuid()).getX()));
                    double playerY =
                            Double.parseDouble(String.valueOf(PLAYER_SPAWN_POS.get(player.getUuid()).getY()));
                    double playerZ =
                            Double.parseDouble(String.valueOf(PLAYER_SPAWN_POS.get(player.getUuid()).getZ()));
                    player.teleport(getOverworld(), playerX, playerY, playerZ, Set.of(), 0.0F, 0.0F, true);
                    player.setSpawnPoint(getOverworld().getRegistryKey(),
                            PLAYER_SPAWN_POS.get(player.getUuid()), 0.0F, true
                            , false);
                    player.setFireTicks(0);
                    player.setOnFire(false);
                    player.setHealth(player.getMaxHealth());
                    player.setAir(player.getMaxAir());
                    resetHungerManager(player);

                    player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED).setBaseValue(1.0);

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

                    if (SettingsGui.CUSTOM_TITLES.get(player.getUuid())) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(ManhuntConfig.CONFIG.getStartTitle())));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(ManhuntConfig.CONFIG.getStartSubtitle()).formatted(Formatting.GRAY)));
                    }

                    if (SettingsGui.CUSTOM_SOUNDS.get(player.getUuid())) {
                        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.MASTER,
                                0.5F, 2.0F);
                    }

                    if (SettingsGui.NIGHT_VISION.get(player.getUuid()))
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                                StatusEffectInstance.INFINITE, 255, false, false, false));
                }
                if (LEFT_ON_PAUSE.contains(player.getUuid())) {
                    LEFT_ON_PAUSE.remove(player.getUuid());
                    player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED).setBaseValue(1.0);
                    player.clearStatusEffects();
                    if (PauseCommand.PLAYER_EFFECTS.containsKey(player.getUuid())) {
                        for (StatusEffectInstance statusEffect : PauseCommand.PLAYER_EFFECTS.get(player.getUuid())) {
                            player.addStatusEffect(statusEffect);
                        }
                    }
                }
                if (paused) {
                    if (PauseCommand.PLAYER_POS.containsKey(player.getUuid()) && PauseCommand.PLAYER_YAW.containsKey(player.getUuid()) && PauseCommand.PLAYER_PITCH.containsKey(player.getUuid())) {
                        player.teleport(server.getWorld(player.getWorld().getRegistryKey()),
                                PauseCommand.PLAYER_POS.get(player.getUuid()).getX(),
                                PauseCommand.PLAYER_POS.get(player.getUuid()).getY(),
                                PauseCommand.PLAYER_POS.get(player.getUuid()).getZ(),
                                Set.of(),
                                PauseCommand.PLAYER_YAW.get(player.getUuid()),
                                PauseCommand.PLAYER_PITCH.get(player.getUuid()),
                                true
                        );
                    } else {
                        PauseCommand.PLAYER_POS.put(player.getUuid(), player.getPos());
                        PauseCommand.PLAYER_YAW.put(player.getUuid(), player.getYaw());
                        PauseCommand.PLAYER_PITCH.put(player.getUuid(), player.getPitch());
                    }
                } else {
                    if (waitForRunner) {
                        if (!runnerHasStarted) {
                            if (player.isTeamPlayer(runnersTeam)) {
                                if (!HEAD_START_POS.containsKey(player.getUuid())) {
                                    HEAD_START_POS.put(player.getUuid(), player.getBlockPos());
                                }

                                if (Math.abs((player.getX() - HEAD_START_POS.get(player.getUuid()).getX()) * (player.getZ() - HEAD_START_POS.get(player.getUuid()).getZ())) >= 0.5) {
                                    runnerHasStarted = true;
                                }
                            }
                            freezeHunters(server);
                        } else {
                            if (!headStart) {
                                waitForRunner = false;
                                ManhuntGame.resetWorldTime(server);
                                runnerHasStarted(server);
                            } else {
                                checkHeadStart(server);
                            }
                        }
                    } else if (headStart) {
                        checkHeadStart(server);
                    }
                }
            }
        }
    }

    private static void resetLobbyPlayer(ServerPlayerEntity player) {
        PARKOUR_TIMER.put(player.getUuid(), 0);
        STARTED_PARKOUR.put(player.getUuid(), false);
        FINISHED_PARKOUR.put(player.getUuid(), false);
        player.changeGameMode(GameMode.ADVENTURE);
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        player.teleport(player.getServer().getWorld(LOBBY_REGISTRY_KEY), LOBBY_SPAWN_DOUBLE.getX(),
                LOBBY_SPAWN_DOUBLE.getY(), LOBBY_SPAWN_DOUBLE.getZ(), Set.of(), 180F, 0.0F, true);
    }

    public static void resetHungerManager(ServerPlayerEntity player) {
        var hungerManager = player.getHungerManager();
        hungerManager.setFoodLevel(20);
        hungerManager.setSaturationLevel(5f);
    }

    public static void freezeHunters(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters"))) {
                if (PLAYER_SPAWN_POS.containsKey(player.getUuid())) {
                    player.teleport(server.getWorld(player.getWorld().getRegistryKey()),
                            PLAYER_SPAWN_POS.get(player.getUuid()).getX(),
                            PLAYER_SPAWN_POS.get(player.getUuid()).getY(),
                            PLAYER_SPAWN_POS.get(player.getUuid()).getZ(),
                            Set.of(),
                            0.0F,
                            0.0F,
                            true
                    );
                }
            }
        }
    }

    private static void runnerHasStarted(MinecraftServer server) {
        String translationKey = "chat.manhunt.wait_for_runner.end";
        if (hunterRelease) {
            translationKey = "chat.manhunt.hunter_release_time.end";
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters"))) {
                player.clearStatusEffects();
                player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED).setBaseValue(1.0);
                player.setFireTicks(0);
                player.setOnFire(false);
                player.setHealth(player.getMaxHealth());
                player.setAir(player.getMaxAir());
                resetHungerManager(player);
            }
            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BANJO.value(), SoundCategory.MASTER, 0.5F, 0.5F);
            player.sendMessage(Text.translatable(translationKey).formatted(Formatting.GOLD), true);
        }
    }

    private static void checkHeadStart(MinecraftServer server) {
        if (headStartTime >= 20) {
            freezeHunters(server);
        } else {
            headStart = false;
            headStartTime = 0;
            runnerHasStarted(server);
        }
    }

    private static boolean hasItem(PlayerEntity player, Item item) {
        return player.getInventory().contains(itemStack -> itemStack.getItem() == item);
    }

    private static void removeDuplicateItems(PlayerEntity player, Item item, int slot) {
        var itemCount = 0;

        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == item) {
                itemCount++;
                if (player.getInventory().getSlotWithStack(stack) != slot) {
                    player.getInventory().removeOne(stack);
                    break;
                } else if (itemCount > 1) {
                    player.getInventory().removeOne(stack);
                    itemCount--;
                } else if (stack.getCount() > 1) {
                    stack.setCount(1);
                    player.getInventory().setStack(slot, stack);
                }
            }
        }

        if (itemCount != 0) {
            if (player.playerScreenHandler.getCursorStack().getItem() == item) {
                itemCount++;
                for (ItemStack stack : player.getInventory().main) {
                    if (stack.getItem() == item) {
                        if (itemCount > 1) {
                            player.getInventory().removeOne(stack);
                            itemCount--;
                        }
                    }
                }
            } else if (player.getOffHandStack().getItem() == item) {
                itemCount++;
                for (ItemStack stack : player.getInventory().main) {
                    if (stack.getItem() == item) {
                        if (itemCount > 1) {
                            player.getInventory().removeOne(stack);
                            itemCount--;
                        }
                    }
                }
            }
        }
    }

    private static void clearItem(PlayerEntity player, Item item) {
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == item) {
                player.getInventory().removeStack(player.getInventory().getSlotWithStack(stack));
            }
        }

        if (player.playerScreenHandler.getCursorStack().getItem() == item) {
            player.playerScreenHandler.setCursorStack(ItemStack.EMPTY);
        }

        if (player.getOffHandStack().getItem() == item) {
            player.getInventory().removeOne(player.getOffHandStack());
        }
    }
}
