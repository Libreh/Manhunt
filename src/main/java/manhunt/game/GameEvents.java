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

import static manhunt.ManhuntMod.*;

public class GameEvents {
    public static final HashMap<UUID, Integer> PLAYER_FOOD = new HashMap<>();
    public static final HashMap<UUID, Float> PLAYER_SATURATION = new HashMap<>();
    public static final HashMap<UUID, Float> PLAYER_EXHAUSTION = new HashMap<>();
    public static final HashMap<UUID, Integer> PLAYER_AIR = new HashMap<>();
    public static final HashMap<UUID, Integer> PARKOUR_TIMER = new HashMap<>();
    public static final HashMap<UUID, Boolean> STARTED_PARKOUR = new HashMap<>();
    public static final HashMap<UUID, Boolean> FINISHED_PARKOUR = new HashMap<>();
    public static final List<UUID> READY_LIST = new ArrayList<>();
    public static final List<UUID> JOIN_LIST = new ArrayList<>();
    public static final List<UUID> START_LIST = new ArrayList<>();
    public static final List<UUID> RESET_LIST = new ArrayList<>();
    public static final List<UUID> POSITIONS_LIST = new ArrayList<>();
    public static final List<UUID> LEFT_ON_PAUSE = new ArrayList<>();
    public static final HashMap<UUID, BlockPos> PLAYER_SPAWN_POS = new HashMap<>();
    public static final HashMap<UUID, BlockPos> HEAD_START_POS = new HashMap<>();
    public static final BlockPos LOBBY_SPAWN_INT = new BlockPos(0, 64, 0);
    public static final Vec3d LOBBY_SPAWN_DOUBLE = new Vec3d(0.5, 64, 0.5);
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
    private static int count;

    public static void serverStart(MinecraftServer server) {
        gameState = GameState.PREGAME;

        if (ManhuntConfig.CONFIG.isSetMotd()) {
            server.setMotd(gameState.getColor() + "[" + gameState.getMotd() + "]§f Minecraft " + "MANHUNT");
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

        if (ManhuntConfig.CONFIG.isTeamColor()) {
            huntersTeam.setColor(ManhuntConfig.CONFIG.getHuntersColor());
            runnersTeam.setColor(ManhuntConfig.CONFIG.getRunnersColor());
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
            count++;
            if (count == 19) {
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
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.manhunt" +
                                            ".starting_in",
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
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.manhunt" +
                                    ".teleporting").styled(style -> style.withColor(11686066))));
                        }
                        ManhuntGame.start(server);
                    }
                }
                count = 0;
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

            count++;
            if (count == 19) {
                if (paused) {
                    pauseTimeLeft -= 20;

                    String minutesString;
                    int minutes = (int) Math.floor((double) pauseTimeLeft % (20 * 60 * 60) / (20 * 60));

                    if (minutes >= ManhuntConfig.CONFIG.getLeavePauseTime()) {
                        pauseTimeLeft = 0;
                        UnpauseCommand.unpauseGame(server);

                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            player.networkHandler.sendPacket(new ClearTitleS2CPacket(false));
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
                            player.networkHandler.sendPacket(new TitleFadeS2CPacket(0, 20, 5));
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(ManhuntConfig.CONFIG.getPausedTitle()).formatted(Formatting.YELLOW)));
                            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("chat.manhunt" + ".time.double", minutesString, secondsString).formatted(Formatting.GOLD)));
                        }
                    }
                } else {
                    if (waitForRunner) {
                        if (!runnerHasStarted) {
                            if (ManhuntConfig.CONFIG.getHunterReleaseTime() != 0) {
                                hunterReleaseTime -= 20;
                                if (hunterReleaseTime >= 20) {
                                    int seconds = (int) Math.floor((double) hunterReleaseTime % (20 * 60) / (20));

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

                            if (!ManhuntConfig.CONFIG.isWaitForRunner() && ManhuntConfig.CONFIG.getHunterReleaseTime() != 0 && ManhuntConfig.CONFIG.getRunnerHeadStart() != 0) {
                                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                                    player.sendMessage(Text.translatable("chat.manhunt.time.triple", hoursString,
                                            minutesString, secondsString).styled(style -> style.withBold(true)), true);
                                }
                            }
                        }
                    }
                }
                count = 0;
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (JOIN_LIST.contains(player.getUuid()) && !player.notInAnyWorld) {
                JOIN_LIST.remove(player.getUuid());
                if (player.interactionManager.getGameMode() != ManhuntGame.getGameMode() || gameState == GameState.PREGAME && player.getWorld().getRegistryKey() != LOBBY_REGISTRY_KEY || !player.getWorld().getRegistryKey().getValue().getNamespace().equals("fantasy") || !ManhuntGame.PLAY_LIST.contains(player.getUuid())) {
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

                    if (ManhuntSettings.NIGHT_VISION.get(player.getUuid())) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                                StatusEffectInstance.INFINITE, 255, false, false));
                    }

                    if (gameState == GameState.PREGAME) {
                        player.teleport(server.getWorld(LOBBY_REGISTRY_KEY), LOBBY_SPAWN_DOUBLE.x,
                                LOBBY_SPAWN_DOUBLE.y, LOBBY_SPAWN_DOUBLE.z, 180f, 0f);
                        player.setSpawnPoint(LOBBY_REGISTRY_KEY, LOBBY_SPAWN_INT, 180f, true, false);
                    } else {
                        if (!PLAYER_SPAWN_POS.containsKey(player.getUuid())) {
                            ManhuntGame.setPlayerSpawn(overworld, player);
                        }
                        int playerX = PLAYER_SPAWN_POS.get(player.getUuid()).getX();
                        int playerY = PLAYER_SPAWN_POS.get(player.getUuid()).getY();
                        int playerZ = PLAYER_SPAWN_POS.get(player.getUuid()).getZ();
                        player.teleport(overworld, playerX, playerY, playerZ, 0, 0);
                        player.setSpawnPoint(overworld.getRegistryKey(), PLAYER_SPAWN_POS.get(player.getUuid()), 0f,
                                true, false);
                    }
                }
                if (gameState == GameState.PREGAME) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION,
                            StatusEffectInstance.INFINITE, 255, false, false, false));
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
                                if (ManhuntSettings.mainRunnerUUID != null) {
                                    scoreboard.addScoreHolderToTeam(server.getPlayerManager().getPlayer(ManhuntSettings.mainRunnerUUID).getNameForScoreboard(), runnersTeam);
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
                            LOBBY_SPAWN_DOUBLE.getY(), LOBBY_SPAWN_DOUBLE.getZ(), PositionFlag.ROT, 180.0F, 0);
                    player.setSpawnPoint(LOBBY_REGISTRY_KEY, LOBBY_SPAWN_INT, 180f, true, false);
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION,
                            StatusEffectInstance.INFINITE, 255, false, false, false));
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

                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

                    if (player.getScoreboardTeam() == null)
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("hunters"));
                }
                if (!hasItemPreGame(player, Items.PLAYER_HEAD)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
                    stack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.preferences").styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                    stack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(0, stack);
                } else {
                    clearDifferentSlotPreGame(player, Items.PLAYER_HEAD, 0);
                }
                if (ManhuntConfig.CONFIG.getRolePreset() == 1) {
                    if (!hasItemPreGame(player, Items.RECOVERY_COMPASS)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.RECOVERY_COMPASS);
                        stack.set(DataComponentTypes.CUSTOM_NAME,
                                Text.translatable("item.manhunt.join_hunters").styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(2, stack);
                    } else {
                        clearDifferentSlotPreGame(player, Items.RECOVERY_COMPASS, 2);
                    }

                    if (!hasItemPreGame(player, Items.CLOCK)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.CLOCK);
                        stack.set(DataComponentTypes.CUSTOM_NAME,
                                Text.translatable("item.manhunt.join_runners").styled(style -> style.withColor(Formatting.GOLD).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(6, stack);
                    } else {
                        clearDifferentSlotPreGame(player, Items.CLOCK, 6);
                    }
                    if (!hasItemPreGame(player, Items.RED_CONCRETE) && !hasItemPreGame(player, Items.LIME_CONCRETE)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);

                        ItemStack stack = new ItemStack(Items.RED_CONCRETE);
                        stack.set(DataComponentTypes.CUSTOM_NAME,
                                Text.translatable("item.manhunt.not_ready").styled(style -> style.withColor(Formatting.RED).withItalic(false)));
                        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(4, stack);
                    } else {
                        if (hasItemPreGame(player, Items.RED_CONCRETE)) {
                            clearDifferentSlotPreGame(player, Items.RED_CONCRETE, 4);
                        }
                        if (hasItemPreGame(player, Items.LIME_CONCRETE)) {
                            clearDifferentSlotPreGame(player, Items.LIME_CONCRETE, 4);
                        }
                    }
                } else {
                    removeItemPreGame(player, Items.RECOVERY_COMPASS);
                    removeItemPreGame(player, Items.RED_CONCRETE);
                    removeItemPreGame(player, Items.LIME_CONCRETE);
                    removeItemPreGame(player, Items.CLOCK);
                }
                if (!hasItemPreGame(player, Items.COMMAND_BLOCK)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);

                    ItemStack stack = new ItemStack(Items.COMMAND_BLOCK);
                    stack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.settings").styled(style -> style.withColor(Formatting.YELLOW).withItalic(false)));
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(8, stack);
                } else {
                    clearDifferentSlotPreGame(player, Items.COMMAND_BLOCK, 8);
                }

                if (!Permissions.check(player, "manhunt.parkour") && player.getZ() < -2 && player.getWorld().getRegistryKey() == LOBBY_REGISTRY_KEY) {
                    PARKOUR_TIMER.putIfAbsent(player.getUuid(), 0);
                    STARTED_PARKOUR.putIfAbsent(player.getUuid(), false);
                    FINISHED_PARKOUR.putIfAbsent(player.getUuid(), false);

                    int ticks = PARKOUR_TIMER.get(player.getUuid());
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

                    if (!FINISHED_PARKOUR.get(player.getUuid())) {
                        if (!STARTED_PARKOUR.get(player.getUuid()) && player.getZ() < -3 && !(player.getZ() < -6)) {
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER
                                    , 1.0F, 1.0F);
                            STARTED_PARKOUR.put(player.getUuid(), true);
                        }

                        if (STARTED_PARKOUR.get(player.getUuid())) {
                            if (player.getZ() < -3) {
                                player.sendMessage(Text.translatable("chat.manhunt.time.double", secSeconds,
                                        msSeconds), true);
                                PARKOUR_TIMER.put(player.getUuid(), PARKOUR_TIMER.get(player.getUuid()) + 1);
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY,
                                        StatusEffectInstance.INFINITE, 255, false, false, false));

                                if (player.getZ() < -24 && player.getZ() > -27 && player.getX() < -6 && player.getY() >= 70 && player.getY() < 72) {
                                    player.sendMessage(Text.translatable("chat.manhunt.time.double", secSeconds,
                                            msSeconds).formatted(Formatting.GREEN), true);
                                    player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(),
                                            SoundCategory.MASTER, 1.0F, 2.0F);
                                    FINISHED_PARKOUR.put(player.getUuid(), true);
                                }
                            }
                        }
                    }
                    if (STARTED_PARKOUR.get(player.getUuid())) {
                        if (player.getZ() > -3 || player.getY() < 61 || (player.getZ() < -27 && player.getY() < 68)) {
                            if (!FINISHED_PARKOUR.get(player.getUuid())) {
                                player.sendMessage(Text.translatable("chat.manhunt.time.double", secSeconds,
                                        msSeconds).formatted(Formatting.RED), true);
                            }
                            resetLobbyPlayer(player);
                            player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER
                                    , 1.0F, 0.5F);
                        }
                    }
                } else {
                    if (player.getY() < 58) {
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
                        ManhuntGame.setPlayerSpawn(overworld, player);
                    }
                    double playerX = Double.parseDouble(String.valueOf(PLAYER_SPAWN_POS.get(player.getUuid()).getX()));
                    double playerY = Double.parseDouble(String.valueOf(PLAYER_SPAWN_POS.get(player.getUuid()).getY()));
                    double playerZ = Double.parseDouble(String.valueOf(PLAYER_SPAWN_POS.get(player.getUuid()).getZ()));
                    player.teleport(overworld, playerX, playerY, playerZ, 0, 0);
                    player.setSpawnPoint(overworld.getRegistryKey(), PLAYER_SPAWN_POS.get(player.getUuid()), 0f, true
                            , false);
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

                    if (ManhuntSettings.CUSTOM_TITLES.get(player.getUuid())) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(ManhuntConfig.CONFIG.getStartTitle())));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(ManhuntConfig.CONFIG.getStartSubtitle()).formatted(Formatting.GRAY)));
                    }

                    if (ManhuntSettings.CUSTOM_SOUNDS.get(player.getUuid())) {
                        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_HARP.value(), SoundCategory.MASTER,
                                0.5F, 2.0F);
                    }

                    if (ManhuntSettings.NIGHT_VISION.get(player.getUuid()))
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                                StatusEffectInstance.INFINITE, 255, false, false));
                }
                if (LEFT_ON_PAUSE.contains(player.getUuid())) {
                    LEFT_ON_PAUSE.remove(player.getUuid());
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                    player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                    player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
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
                                PauseCommand.PLAYER_YAW.get(player.getUuid()),
                                PauseCommand.PLAYER_PITCH.get(player.getUuid()));
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

    public static void playerRespawn(ServerPlayerEntity player) {
        JOIN_LIST.add(player.getUuid());
        if (!ManhuntGame.PLAY_LIST.contains(player.getUuid())) {
            ManhuntGame.PLAY_LIST.add(player.getUuid());
        }
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
        JOIN_LIST.add(player.getUuid());
        ManhuntSettings.SLOW_DOWN_MANAGER.putIfAbsent(player.getUuid(), 0);
        if (!ManhuntGame.PLAY_LIST.contains(player.getUuid()) && player.getScoreboardTeam() != null) {
            scoreboard.removeScoreHolderFromTeam(player.getNameForScoreboard(), player.getScoreboardTeam());
        }
        if (!PLAYER_SPAWN_POS.containsKey(player.getUuid())) {
            ManhuntGame.setPlayerSpawn(overworld, player);
        }
        if (gameState == GameState.PREGAME) {
            player.setSpawnPoint(LOBBY_REGISTRY_KEY, LOBBY_SPAWN_INT, 180f, true, false);
        } else if (gameState == GameState.PLAYING) {
            if (paused) {
                if (player.isTeamPlayer(scoreboard.getTeam("runners")) && scoreboard.getTeam("runners").getPlayerList().size() == 1) {
                    UnpauseCommand.unpauseGame(server);
                } else if (!LEFT_ON_PAUSE.contains(player.getUuid())) {
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
                if (LEFT_ON_PAUSE.contains(player.getUuid())) {
                    player.playSoundToPlayer(SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.MASTER, 0.1f, 1.5f);
                }
            }
        }

        ManhuntSettings.CUSTOM_TITLES.putIfAbsent(player.getUuid(), ManhuntConfig.CONFIG.isCustomTitlesDefault());
        ManhuntSettings.CUSTOM_SOUNDS.putIfAbsent(player.getUuid(), ManhuntConfig.CONFIG.isCustomSoundsDefault());
        ManhuntSettings.CUSTOM_PARTICLES.putIfAbsent(player.getUuid(), ManhuntConfig.CONFIG.isCustomParticlesDefault());
        ManhuntSettings.TRACKER_TYPE.putIfAbsent(player.getUuid(), 4);
        ManhuntSettings.NIGHT_VISION.putIfAbsent(player.getUuid(), false);
        ManhuntSettings.FRIENDLY_FIRE.putIfAbsent(player.getUuid(), true);
        ManhuntSettings.BED_EXPLOSIONS.putIfAbsent(player.getUuid(), ManhuntConfig.CONFIG.isBedExplosionsDefault());
        ManhuntSettings.LAVA_PVP_IN_NETHER.putIfAbsent(player.getUuid(),
                ManhuntConfig.CONFIG.isLavaPvpInNetherDefault());

        if (!ManhuntSettings.CUSTOM_TITLES.containsKey(player.getUuid())) {
            Optional<DataContainer> dataContainer = DATA_STORE.getContainer("uuid", player.getUuid());
            if (dataContainer.isEmpty()) return;

            ManhuntSettings.CUSTOM_TITLES.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "custom_titles"));
            ManhuntSettings.CUSTOM_SOUNDS.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "custom_sounds"));
            ManhuntSettings.CUSTOM_PARTICLES.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "custom_particles"));
            ManhuntSettings.TRACKER_TYPE.put(player.getUuid(), dataContainer.get().get(JavaTypes.INT, "tracker_type"));
            ManhuntSettings.NIGHT_VISION.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL, "night_vision"));
            ManhuntSettings.FRIENDLY_FIRE.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "friendly_fire"));
            ManhuntSettings.BED_EXPLOSIONS.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
                    "bed_explosions"));
            ManhuntSettings.LAVA_PVP_IN_NETHER.put(player.getUuid(), dataContainer.get().get(JavaTypes.BOOL,
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
                starting = false;
                startingTime = 0;
                startReset = false;
                runnersTeam.getPlayerList().remove(player.getNameForScoreboard());
            }
        } else if (gameState == GameState.PLAYING) {
            if (paused) {
                LEFT_ON_PAUSE.add(player.getUuid());
            } else {
                if (isRunner && runnersTeam.getPlayerList().size() == 1) {
                    LEFT_ON_PAUSE.add(player.getUuid());
                    PauseCommand.pauseGame(player.getServer());
                }
            }
        }

        var dataContainer = DATA_STORE.getOrCreateContainer("uuid", player.getUuid());

        dataContainer.transaction().put(JavaTypes.UUID, "uuid", player.getUuid()).put(JavaTypes.BOOL, "custom_titles"
                , ManhuntSettings.CUSTOM_TITLES.get(player.getUuid())).put(JavaTypes.BOOL, "custom_sounds",
                ManhuntSettings.CUSTOM_SOUNDS.get(player.getUuid())).put(JavaTypes.BOOL, "custom_particles",
                ManhuntSettings.CUSTOM_PARTICLES.get(player.getUuid())).put(JavaTypes.INT, "tracker_type",
                ManhuntSettings.TRACKER_TYPE.get(player.getUuid())).put(JavaTypes.BOOL, "night_vision",
                ManhuntSettings.NIGHT_VISION.get(player.getUuid())).put(JavaTypes.BOOL, "friendly_fire",
                ManhuntSettings.FRIENDLY_FIRE.get(player.getUuid())).put(JavaTypes.BOOL, "bed_explosions",
                ManhuntSettings.BED_EXPLOSIONS.get(player.getUuid())).put(JavaTypes.BOOL, "lava_pvp_in_nether",
                ManhuntSettings.LAVA_PVP_IN_NETHER.get(player.getUuid())).commit();
    }

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, World world, Hand hand) {
        var stack = player.getStackInHand(hand);
        var server = player.getServer();
        var scoreboard = server.getScoreboard();
        var huntersTeam = scoreboard.getTeam("hunters");
        var runnersTeam = scoreboard.getTeam("runners");

        if (gameState == GameState.PREGAME) {
            if (stack.getItem() == Items.PLAYER_HEAD) {
                ManhuntSettings.openPreferencesGui((ServerPlayerEntity) player);
            }
            if (stack.getItem() == Items.RECOVERY_COMPASS) {
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ManhuntSettings.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    if (!player.isTeamPlayer(huntersTeam)) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), huntersTeam);

                        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getHuntersColor()), Text.translatable("role.manhunt.hunters").formatted(ManhuntConfig.CONFIG.getHuntersColor())), false);

                        player.playSoundToPlayer(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.5F
                                , 1.0F);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team", Text.translatable("role" +
                                ".manhunt.hunter")).formatted(Formatting.RED), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.RED_CONCRETE) {
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ManhuntSettings.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    READY_LIST.add(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.ready",
                            Text.literal(player.getNameForScoreboard()).formatted(Formatting.GREEN)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Ready", true);

                    ItemStack itemStack = new ItemStack(Items.LIME_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.ready").styled(style -> style.withColor(Formatting.GREEN).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5F, 1.0F);

                    if (READY_LIST.size() == server.getPlayerManager().getPlayerList().size()) {
                        int runners = 0;
                        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                            if (serverPlayer.isTeamPlayer(runnersTeam)) {
                                runners++;
                                break;
                            }
                        }
                        if (runners == 0) {
                            server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.minimum",
                                    Text.translatable("role.manhunt.runner")).formatted(Formatting.RED), false);
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
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ManhuntSettings.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    READY_LIST.remove(player.getUuid());

                    server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.not_ready",
                            Text.literal(player.getNameForScoreboard()).formatted(Formatting.RED)), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);

                    ItemStack itemStack = new ItemStack(Items.RED_CONCRETE);
                    itemStack.set(DataComponentTypes.CUSTOM_NAME,
                            Text.translatable("item.manhunt.not_ready").styled(style -> style.withColor(Formatting.RED).withItalic(false)));
                    itemStack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(4, itemStack);

                    player.playSoundToPlayer(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5F, 1.0F);
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }
            if (stack.getItem() == Items.CLOCK) {
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 8)
                    ManhuntSettings.SLOW_DOWN_MANAGER.put(player.getUuid(),
                            ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) + 1);
                if (ManhuntSettings.SLOW_DOWN_MANAGER.get(player.getUuid()) < 4) {
                    if (!player.isTeamPlayer(runnersTeam)) {
                        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), runnersTeam);
                        server.getPlayerManager().broadcast(Text.translatable("chat.manhunt.joined_team",
                                Text.literal(player.getNameForScoreboard()).formatted(ManhuntConfig.CONFIG.getRunnersColor()), Text.translatable("role.manhunt.runners").formatted(ManhuntConfig.CONFIG.getRunnersColor())), false);
                        player.playSoundToPlayer(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.5F,
                                1.0F);
                    } else {
                        player.sendMessage(Text.translatable("chat.manhunt.already_team", Text.translatable("role" +
                                ".manhunt.runner")).formatted(Formatting.RED), false);
                    }
                } else {
                    player.sendMessage(Text.translatable("chat.manhunt.slow_down").formatted(Formatting.RED));
                }
            }

            if (stack.getItem() == Items.COMMAND_BLOCK) {
                ManhuntSettings.openSettingsGui((ServerPlayerEntity) player);
            }
        } else if (gameState == GameState.PLAYING) {
            if (paused) {
                return TypedActionResult.fail(stack);
            } else {
                if (GameEvents.waitForRunner && !GameEvents.runnerHasStarted) {
                    return TypedActionResult.fail(stack);
                } else {
                    if (headStart && player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                        return TypedActionResult.fail(stack);
                    }
                }
            }
            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker") && ManhuntConfig.CONFIG.getTrackerType() == 2 || ManhuntSettings.TRACKER_TYPE.get(player.getUuid()) == 2 && player.isTeamPlayer(huntersTeam) && !allRunners.isEmpty() && !player.isSpectator() && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                player.getItemCooldownManager().set(stack.getItem(), 20);
                if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name") != null) {
                    NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                    nbt.putString("Name", allRunners.getFirst().getName().getString());
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                }
                ServerPlayerEntity trackedPlayer =
                        server.getPlayerManager().getPlayer(stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("Name"));
                if (trackedPlayer != null) {
                    ManhuntGame.updateCompass((ServerPlayerEntity) player, stack, trackedPlayer);
                    player.playSoundToPlayer(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.1f, 0.5F);
                }
            }
            if (!ManhuntConfig.CONFIG.isLavaPvpInNether()) {
                if (world.getRegistryKey() == theNether.getRegistryKey() && stack.getItem() == Items.LAVA_BUCKET) {
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
        if (gameState == GameState.PLAYING) {
            if (paused) {
                return ActionResult.FAIL;
            } else {
                if (GameEvents.waitForRunner && !GameEvents.runnerHasStarted) {
                    return ActionResult.FAIL;
                } else {
                    if (headStart && player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                        return ActionResult.FAIL;
                    }
                }
            }
            if (!ManhuntConfig.CONFIG.isBedExplosions()) {
                if (world.getRegistryKey() != overworld.getRegistryKey()) {
                    Vec3d pos = hitResult.getPos();
                    Block block =
                            player.getWorld().getBlockState(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z)).getBlock();
                    if (player.getStackInHand(hand).getName().getString().toLowerCase().contains(" bed") || block.getName().getString().toLowerCase().contains(" bed")) {
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
        NbtCompound islandNbt =
                NbtIo.readCompressed(ManhuntMod.class.getResourceAsStream("/manhunt/lobby/island.nbt"),
                        NbtSizeTracker.ofUnlimitedBytes());
        NbtCompound parkourNbt =
                NbtIo.readCompressed(ManhuntMod.class.getResourceAsStream("/manhunt/lobby/parkour" + ".nbt"),
                        NbtSizeTracker.ofUnlimitedBytes());
        ServerWorld lobby = server.getWorld(LOBBY_REGISTRY_KEY);
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
        template.place(world, pos, pos, new StructurePlacementData(),
                StructureBlockBlockEntity.createRandom(world.getSeed()), 2);
    }

    private static boolean hasItemPreGame(PlayerEntity player, Item item) {
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

    private static void clearDifferentSlotPreGame(PlayerEntity player, Item item, int slot) {
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

    private static void removeItemPreGame(PlayerEntity player, Item item) {
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
        PARKOUR_TIMER.put(player.getUuid(), 0);
        STARTED_PARKOUR.put(player.getUuid(), false);
        FINISHED_PARKOUR.put(player.getUuid(), false);
        player.changeGameMode(GameMode.ADVENTURE);
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        player.teleport(player.getServer().getWorld(LOBBY_REGISTRY_KEY), LOBBY_SPAWN_DOUBLE.getX(),
                LOBBY_SPAWN_DOUBLE.getY(), LOBBY_SPAWN_DOUBLE.getZ(), PositionFlag.ROT, 180f, 0);
    }

    public static void resetHungerManager(ServerPlayerEntity player) {
        var hungerManager = player.getHungerManager();
        hungerManager.setFoodLevel(20);
        hungerManager.setSaturationLevel(5f);
        hungerManager.setExhaustion(0f);
    }

    private static void freezeHunters(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters"))) {
                if (PauseCommand.PLAYER_POS.containsKey(player.getUuid())) {
                    player.teleport(server.getWorld(player.getWorld().getRegistryKey()),
                            PauseCommand.PLAYER_POS.get(player.getUuid()).getX(),
                            PauseCommand.PLAYER_POS.get(player.getUuid()).getY(),
                            PauseCommand.PLAYER_POS.get(player.getUuid()).getZ(), player.getYaw(), player.getPitch());
                } else {
                    PauseCommand.PLAYER_POS.put(player.getUuid(), player.getPos());
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
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
                player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
                player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);
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
}
