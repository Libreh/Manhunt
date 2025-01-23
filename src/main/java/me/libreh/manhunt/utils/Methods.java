package me.libreh.manhunt.utils;

import me.libreh.manhunt.Manhunt;
import me.libreh.manhunt.config.Config;
import me.libreh.manhunt.config.PlayerData;
import me.libreh.manhunt.game.GameState;
import me.libreh.manhunt.game.ManhuntGame;
import me.libreh.manhunt.world.ServerTaskExecutor;
import me.libreh.manhunt.world.ServerWorldController;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static me.libreh.manhunt.utils.Constants.*;
import static me.libreh.manhunt.utils.Fields.*;

public class Methods {
    public static void changeState(GameState newState) {
        gameState = newState;

        server.setMotd(gameState.getColor() + "[" + gameState.getMotd() + "]§f Manhunt");

        switch (gameState) {
            case GameState.PREGAME -> ManhuntGame.resetGame();
            case GameState.STARTED -> ManhuntGame.startGame();
            case GameState.POSTGAME -> ManhuntGame.endGame();
        }
    }

    public static boolean isPreGame() {
        return gameState == GameState.PREGAME || gameState == GameState.PRELOADING;
    }
    public static boolean isPlaying() {
        return gameState == GameState.WAITING || gameState == GameState.STARTED;
    }

    public static boolean isLobby(ServerWorld world) {
        return world.getRegistryKey() == LOBBY_REGISTRY_KEY;
    }
    public static boolean isOverworld(ServerWorld world) {
        return world.getRegistryKey() == World.OVERWORLD;
    }
    public static boolean isNether(ServerWorld world) {
        return world.getRegistryKey() == World.NETHER;
    }
    public static boolean isEnd(ServerWorld world) {
        return world.getRegistryKey() == World.END;
    }

    public static boolean isHeadstart() {
        return Config.getConfig().gameOptions.headStart != 0 && headStartTicks >= 20;
    }

    public static boolean requirePermissionOrOperator(ServerCommandSource source, String key) {
        return Permissions.check(source, key, Config.getConfig().defaultOpPermissionLevel) || Permissions.check(source, "manhunt.operator");
    }

    public static boolean playerPermissionOrOperator(ServerPlayerEntity player, String key) {
        return Permissions.check(player, key, Config.getConfig().defaultOpPermissionLevel) || Permissions.check(player, "manhunt.operator");
    }

    public static boolean isTeamless(PlayerEntity player) {
        return !isRunner(player) && !isHunter(player);
    }

    public static boolean isRunner(PlayerEntity player) {
        return player.isTeamPlayer(player.getScoreboard().getTeam("runners"));
    }
    public static boolean isHunter(PlayerEntity player) {
        return player.isTeamPlayer(player.getScoreboard().getTeam("hunters"));
    }

    public static void makeRunner(String playerName) {
        scoreboard.addScoreHolderToTeam(playerName, runnersTeam);
    }
    public static void makeRunner(PlayerEntity player) {
        makeRunner(player.getNameForScoreboard());
    }

    public static void makeHunter(String playerName) {
        scoreboard.addScoreHolderToTeam(playerName, huntersTeam);
    }
    public static void makeHunter(PlayerEntity player) {
        makeHunter(player.getNameForScoreboard());
    }

    public static void teleportToLobby(ServerPlayerEntity player) {
        var lobbyWorld = server.getWorld(LOBBY_REGISTRY_KEY);
        if (lobbyWorld == null) return;
        player.teleport(lobbyWorld, LOBBY_SPAWN.x, LOBBY_SPAWN.y, LOBBY_SPAWN.z,
                Set.of(), 180.0F, 0.0F, true);
    }

    public static void setPlayerSpawn(ServerPlayerEntity player) {
        var spawnPos = player.getWorldSpawnPos(overworld, overworld.getSpawnPos());
        SPAWN_POS.put(player.getUuid(), spawnPos.toBottomCenterPos());
    }

    public static void resetWorldTime() {
        var worldProperties = server.getSaveProperties().getMainWorldProperties();
        worldProperties.setTime(0);
        worldProperties.setRainTime(0);
        worldProperties.setRaining(false);
        worldProperties.setThunderTime(0);
        worldProperties.setThundering(false);
        for (ServerWorld world : server.getWorlds()) {
            world.setTimeOfDay(1000);
            world.resetWeather();
        }
    }

    public static boolean notSpamming(PlayerEntity player) {
        var playerUuid = player.getUuid();
        var spamPrevention = SPAM_PREVENTION.get(playerUuid);
        if (spamPrevention < 8) {
            SPAM_PREVENTION.put(playerUuid, spamPrevention + 1);
        }

        return spamPrevention < 4;
    }

    public static void lockPlayer(PlayerEntity player) {
        player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).setBaseValue(0);
        player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).setBaseValue(0);
        player.getAttributeInstance(EntityAttributes.BLOCK_BREAK_SPEED).setBaseValue(0);
    }

    public static void resetAttributes(PlayerEntity player) {
        var attributes = player.getAttributes();
        for (var attribute : attributes.getAttributesToSend()) {
            attributes.resetToBaseValue(attribute.getAttribute());
        }
    }

    public static void updateGameMode(ServerPlayerEntity player, boolean forceReset) {
        var playerUuid = player.getUuid();
        var isRespawnNeeded = (player.interactionManager.getGameMode() != getGameMode() || forceReset) && isPreGame() && gameState != GameState.POSTGAME;
        var hasPlayed = PLAY_LIST.contains(playerUuid);
        var isStuckInLobby = isLobby(player.getServerWorld()) && !isPreGame();
        var isStuckInWorld = !isLobby(player.getServerWorld()) && isPreGame();
        if (isRespawnNeeded || !hasPlayed || isStuckInLobby || isStuckInWorld) {
            if (!player.isAlive()) {
                var newPlayer = player;
                var networkHandler = newPlayer.networkHandler;
                networkHandler.onClientStatus(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
                newPlayer = networkHandler.player;

                if (ServerWorldController.taskExecutor == null) {
                    ServerWorldController.taskExecutor = new ServerTaskExecutor(server);
                }

                ServerPlayerEntity finalNewPlayer = newPlayer;
                ServerWorldController.taskExecutor.execute(() -> updateGameMode(finalNewPlayer, true));
                return;
            }

            respawnPlayer(player);
        }

        if (player.interactionManager.getGameMode() != getGameMode()) {
            player.changeGameMode(getGameMode());
        }

        if (isPreGame() && isTeamless(player)) {
            joinPresetMode(player);
        } else if (isPlaying() && !PLAY_LIST.contains(playerUuid)) {
            PLAY_LIST.add(player.getUuid());

            joinPresetMode(player);
        }

        if (isPreGame()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false));
        }
    }

    public static void respawnPlayer(ServerPlayerEntity player) {
        resetPlayerHealth(player);
        resetAttributes(player);

        player.setExperienceLevel(0);
        player.setExperiencePoints(0);
        player.setScore(0);

        player.getInventory().clear();
        player.getEnderChestInventory().clear();

        player.lockRecipes(server.getRecipeManager().values());
        for (AdvancementEntry advancement : server.getAdvancementLoader().getAdvancements()) {
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            for (String criteria : progress.getObtainedCriteria()) {
                player.getAdvancementTracker().revokeCriterion(advancement, criteria);
            }
        }
        Stats.MINED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.CRAFTED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.USED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.BROKEN.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.PICKED_UP.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.DROPPED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.KILLED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.KILLED_BY.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
        Stats.CUSTOM.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));

        var playerUuid = player.getUuid();
        if (isPreGame()) {
            teleportToLobby(player);
        } else {
            if (!SPAWN_POS.containsKey(playerUuid)) {
                setPlayerSpawn(player);
            }

            Vec3d pos = SPAWN_POS.get(playerUuid);
            player.teleport(overworld, pos.x, pos.y, pos.z, Set.of(), 0.0F, 0.0F, true);
            player.setSpawnPoint(overworld.getRegistryKey(), new BlockPos((int) pos.x, (int) pos.y, (int) pos.z),
                    0.0F, true, false);
        }
    }

    public static int updateTracker(ServerPlayerEntity player) {
        ServerPlayerEntity trackedPlayer = null;

        float distance = -1.0F;
        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            if (isRunner(serverPlayer)) {
                if (trackedPlayer == null) {
                    trackedPlayer = serverPlayer;
                }

                if (player.getWorld().getRegistryKey() == serverPlayer.getWorld().getRegistryKey()) {
                    float currentDistance = player.distanceTo(serverPlayer);
                    if (distance == -1.0F || currentDistance < distance) {
                        trackedPlayer = serverPlayer;
                        distance = currentDistance;
                    }
                }
            }
        }

        ItemStack itemStack = null;
        int slot = PlayerInventory.NOT_FOUND;
        var inventory = player.getInventory();

        if (isTracker(inventory.getMainHandStack())) {
            itemStack = inventory.getMainHandStack();
            slot = inventory.getSlotWithStack(itemStack);
        } else if (isTracker(inventory.getStack(PlayerInventory.OFF_HAND_SLOT))) {
            itemStack = inventory.getStack(PlayerInventory.OFF_HAND_SLOT);
            slot = PlayerInventory.OFF_HAND_SLOT;
        } else {
            for (int i = 0; i < PlayerInventory.MAIN_SIZE && itemStack == null; i++) {
                var stack = inventory.getStack(i);
                if (isTracker(stack)) {
                    itemStack = stack;
                    slot = i;
                }
            }
        }

        if (slot != PlayerInventory.NOT_FOUND && itemStack != null) {
            BlockPos trackPos = BlockPos.ORIGIN;
            if (trackedPlayer != null) {
                var trackedUuid = trackedPlayer.getUuid();
                var world = player.getServerWorld();
                if (isOverworld(world) && OVERWORLD_POSITION.containsKey(trackedUuid)) {
                    trackPos = OVERWORLD_POSITION.get(trackedUuid);
                } else if (isNether(world) && NETHER_POSITION.containsKey(trackedUuid)) {
                    trackPos = NETHER_POSITION.get(trackedUuid);
                } else if (isEnd(world) && END_POSITION.containsKey(trackedUuid)) {
                    trackPos = END_POSITION.get(trackedUuid);
                }
            }

            var trackerCpnt = new LodestoneTrackerComponent(Optional.of(GlobalPos.create(player.getWorld().getRegistryKey(), trackPos)), false);

            itemStack.set(DataComponentTypes.LODESTONE_TRACKER, trackerCpnt);

            player.getInventory().setStack(slot, itemStack);

            if (player.squaredDistanceTo(trackPos.getX(), trackPos.getY(), trackPos.getZ()) < 64) {
                return 500;
            } else if (player.squaredDistanceTo(trackPos.getX(), trackPos.getY(), trackPos.getZ()) < 256) {
                return 1000;
            } else if (player.squaredDistanceTo(trackPos.getX(), trackPos.getY(), trackPos.getZ()) < 512) {
                return 2000;
            } else {
                return 4000;
            }
        }

        return 1000;
    }

    public static void giveTracker(ServerPlayerEntity player) {
        ItemStack itemStack = new ItemStack(Items.COMPASS);

        itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("item.manhunt.tracker").styled(style -> style
                .withColor(Formatting.AQUA).withItalic(false)));

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("Remove", true);
        nbt.putBoolean("Tracker", true);
        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        player.giveItemStack(itemStack);
        updateTracker(player);
    }

    public static boolean hasTracker(PlayerEntity player) {
        ItemStack itemStack = null;
        int slot = PlayerInventory.NOT_FOUND;
        var inventory = player.getInventory();

        if (!isTracker(player.playerScreenHandler.getCursorStack())) {
            if (isTracker(inventory.getMainHandStack())) {
                itemStack = inventory.getMainHandStack();
                slot = inventory.getSlotWithStack(itemStack);
            } else if (isTracker(inventory.getStack(PlayerInventory.OFF_HAND_SLOT))) {
                itemStack = inventory.getStack(PlayerInventory.OFF_HAND_SLOT);
                slot = PlayerInventory.OFF_HAND_SLOT;
            } else {
                for (int i = 0; i < PlayerInventory.MAIN_SIZE && itemStack == null; i++) {
                    var stack = inventory.getStack(i);
                    if (isTracker(stack)) {
                        itemStack = stack;
                        slot = i;
                    }
                }
            }
        } else {
            slot = -2;
        }

        return slot != PlayerInventory.NOT_FOUND || itemStack != null;
    }

    public static boolean isTracker(ItemStack item) {
        return item.isOf(Items.COMPASS) && item.get(DataComponentTypes.CUSTOM_DATA) != null && item.get(DataComponentTypes.CUSTOM_DATA).contains("Remove");
    }

    public static GameMode getGameMode() {
        if (!isPreGame()) {
            return GameMode.SURVIVAL;
        }

        return GameMode.ADVENTURE;
    }

    public static void parkourReset(ServerPlayerEntity player) {
        PARKOUR_TIMER.put(player.getUuid(), 0);
        STARTED_PARKOUR.put(player.getUuid(), false);
        FINISHED_PARKOUR.put(player.getUuid(), false);
        player.changeGameMode(GameMode.ADVENTURE);
        player.removeStatusEffect(StatusEffects.INVISIBILITY);
        teleportToLobby(player);
    }

    public static void resetPlayerHealth(ServerPlayerEntity player) {
        player.clearStatusEffects();
        player.setFireTicks(0);
        player.setOnFire(false);
        player.setAir(player.getMaxAir());
        player.setHealth(player.getMaxHealth());
        var nbt = new NbtCompound();
        player.getHungerManager().writeNbt(nbt);
        nbt.putInt("foodLevel", 20);
        nbt.putInt("foodTickTimer", 0);
        nbt.putFloat("foodSaturationLevel", 5.0F);
        nbt.putFloat("foodExhaustionLevel", 0);
        player.getHungerManager().readNbt(nbt);
    }

    public static void joinPresetMode(ServerPlayerEntity player) {
        if (!Config.getConfig().gameOptions.presetMode.equals("free_select") && !Config.getConfig().gameOptions.presetMode.equals("no_selection")) {
            switch (Config.getConfig().gameOptions.presetMode) {
                case "equal_split" -> {
                    int hunters = 0;
                    int runners = 0;
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        if (serverPlayer.isTeamPlayer(huntersTeam)) {
                            hunters++;
                        } else {
                            runners++;
                        }
                    }
                    if (hunters > runners) {
                        makeRunner(player);
                    } else {
                        makeHunter(player);
                    }
                }
                case "speedrun_showdown" -> makeRunner(player);
                case "runner_cycle" -> {
                    int runners = 0;
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        if (isRunner(serverPlayer)) {
                            runners++;
                            break;
                        }
                    }
                    if (runners == 0) {
                        makeRunner(player);
                    } else {
                        makeHunter(player);
                    }
                }
                case "hunter_infection" -> {
                    int hunters = 0;
                    for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                        if (isHunter(serverPlayer)) {
                            hunters++;
                            break;
                        }
                    }
                    if (hunters == 0) {
                        makeHunter(player);
                    } else {
                        makeRunner(player);
                    }
                }
            }
        } else {
            if (isTeamless(player)) {
                makeHunter(player);
            }
        }
    }

    public static void resetPresetMode() {
        if (!Config.getConfig().gameOptions.presetMode.equals("free_select") && !Config.getConfig().gameOptions.presetMode.equals("no_selection")) {
            switch (Config.getConfig().gameOptions.presetMode) {
                case "equal_split" -> equalSplit();
                case "speedrun_showdown" -> speedrunShowdown();
                case "runner_cycle" -> runnerCycle();
                case "hunter_infection" -> hunterInfection();
            }
        } else {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (isTeamless(player)) {
                    makeHunter(player);
                }
            }
        }
    }

    public static void equalSplit() {
        List<String> playerNames = new ArrayList<>(List.of(server.getPlayerManager().getPlayerNames()));
        Collections.shuffle(playerNames);

        boolean runner = true;
        for (String playerName : playerNames) {
            runner = !runner;

            if (runner) {
                makeRunner(playerName);
            } else {
                makeHunter(playerName);
            }
        }
    }

    public static void speedrunShowdown() {
        for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
            makeRunner(serverPlayer);
        }
    }

    public static void runnerCycle() {
        if (presetModeList.isEmpty()) {
            presetModeList = new ArrayList<>();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                presetModeList.add(player.getUuid());
            }
        }
        Collections.shuffle(presetModeList);
        presetModeList.removeIf(uuid -> Objects.requireNonNull(server.getPlayerManager().getPlayer(uuid)).isDisconnected());

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            makeHunter(player);
        }
        ServerPlayerEntity runner = server.getPlayerManager().getPlayer(presetModeList.getFirst());
        scoreboard.addScoreHolderToTeam(runner.getNameForScoreboard(), runnersTeam);
        presetModeList.remove(runner.getUuid());
    }

    public static void hunterInfection() {
        List<UUID> uuidList = new ArrayList<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            uuidList.add(player.getUuid());
        }
        Collections.shuffle(uuidList);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            makeRunner(player);
        }
        ServerPlayerEntity hunter = server.getPlayerManager().getPlayer(uuidList.getFirst());
        scoreboard.addScoreHolderToTeam(hunter.getNameForScoreboard(), huntersTeam);
    }

    public static void teamWins(String teamName, Formatting formatting) {
        changeState(GameState.POSTGAME);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            var data = PlayerData.get(player);

            if (Config.getConfig().globalPreferences.customSounds.equals("always") || data.customSounds) {
                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE.value(), SoundCategory.MASTER, 1.0F, 0.5F);
            }
            if (Config.getConfig().globalPreferences.customTitles.equals("always") || data.customTitles) {
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.manhunt.gg").formatted(Formatting.AQUA)));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("title.manhunt." + teamName + "_win").formatted(formatting)));
            }

        }
    }

    public static void runnersWin() {
        teamWins("runners", Config.getConfig().gameOptions.teamColor.runnersColor);
    }
    public static void huntersWin() {
        teamWins("hunters", Config.getConfig().gameOptions.teamColor.huntersColor);
    }

    public static void deleteWorld() {
        for (String string : Config.getConfig().filesToReset) {
            try {
                File file = WORLD_DIR.resolve(string).toFile();
                if (file.exists()) {
                    if (file.isDirectory()) {
                        FileUtils.deleteDirectory(file);
                    } else {
                        FileUtils.delete(file);
                    }
                }
            } catch (IOException e) {
                Manhunt.LOGGER.info("Failed to delete " + string, e);
            }
        }
    }

    public static void unzipLobbyWorld() {
        try (ZipInputStream zis = new ZipInputStream(Manhunt.class.getResourceAsStream("/manhunt/lobby_world.zip"))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(GAME_DIR.resolve("world") + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Manhunt.LOGGER.info("Failed to unzip world", e);
        }

        Manhunt.LOGGER.info("Manhunt lobby loaded");
    }
}
