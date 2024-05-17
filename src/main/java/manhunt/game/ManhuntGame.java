package manhunt.game;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.mixin.ServerWorldAccessor;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static manhunt.ManhuntMod.*;

public class ManhuntGame {
    public static void startGame(MinecraftServer server) {
        server.setFlightEnabled(true);

        setGameState(GameState.PLAYING);

        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(true, server);
        server.getGameRules().get(GameRules.DO_INSOMNIA).set(true, server);
        server.getGameRules().get(GameRules.DO_MOB_LOOT).set(true, server);
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(true, server);
        server.getGameRules().get(GameRules.FALL_DAMAGE).set(true, server);
        server.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(3, server);
        server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
        server.getGameRules().get(GameRules.SPAWN_RADIUS).set(config.getSpawnRadius(), server);
        server.getGameRules().get(GameRules.FALL_DAMAGE).set(true, server);
        server.getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(config.isSpectatorsGenerateChunks(), server);

        if (config.isTeamColor()) {
            server.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
            server.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
        } else {
            server.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
            server.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
        }

        server.setDifficulty(config.getGameDifficulty(), true);

        for (ServerWorld serverWorld : server.getWorlds()) {
            ((ServerWorldAccessor) serverWorld).getWorldProperties().setTime(0);
            serverWorld.setTimeOfDay(0);
            serverWorld.resetWeather();
        }

        WorldBorder worldBorder = overworldWorld.getWorldBorder();

        overworldWorld.getWorldBorder().interpolateSize(worldBorder.getSize(), config.getWorldBorder(), 0);

        server.getScoreboard().getTeam("hunters").setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        server.getScoreboard().getTeam("runners").setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            startedParkour.put(player.getUuid(), true);

            hasPlayed.put(player.getUuid(), true);

            if (!playerSpawn.containsKey(player.getUuid())) {
                setPlayerSpawn(overworldWorld, player);
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

            double playerX = Double.parseDouble(String.valueOf(playerSpawn.get(player.getUuid()).getX()));
            double playerY = Double.parseDouble(String.valueOf(playerSpawn.get(player.getUuid()).getY()));
            double playerZ = Double.parseDouble(String.valueOf(playerSpawn.get(player.getUuid()).getZ()));
            player.teleport(overworldWorld, playerX, playerY, playerZ, 0, 0);
            player.setSpawnPoint(overworldWorld.getRegistryKey(), playerSpawn.get(player.getUuid()), 0, true, false);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);

            player.changeGameMode(GameMode.SURVIVAL);

            if (player.getScoreboardTeam().getName().equals("runners")) {
                if (config.isRunnerGlow()) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, StatusEffectInstance.INFINITE, 255, false, false));
                }
            }

            if (player.getScoreboardTeam().getName().equals("hunters")) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.getRunnerHeadstart() * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, config.getRunnerHeadstart() * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, config.getRunnerHeadstart() * 20, 248, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (config.getRunnerHeadstart() - 1) * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, config.getRunnerHeadstart() * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, config.getRunnerHeadstart() * 20, 255, false, false));
            }

            if (gameTitles.get(player.getUuid())) {
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getGameStartTitle())));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getGameStartSubtitle()).formatted(Formatting.GRAY)));
            }

            if (manhuntSounds.get(player.getUuid())) {
                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 0.5F, 2.0F);
            }

            if (nightVision.get(player.getUuid())) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 255, false, false));
            }
        }
    }

    public static void updateCompass(ServerPlayerEntity player, ItemStack stack, ServerPlayerEntity trackedPlayer) {
        stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(GlobalPos.create(trackedPlayer.getWorld().getRegistryKey(), trackedPlayer.getBlockPos())), false));
    }

    public static void endGame(MinecraftServer server, boolean hunterWin, boolean timeOver) {
        setGameState(GameState.POSTGAME);

        LOGGER.info("Seed: " + overworldWorld.getSeed());

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (config.isSpectateWin()) {
                player.changeGameMode(GameMode.SPECTATOR);
            }

            if (config.isGameTitles() && gameTitles.get(player.getUuid())) {
                if (hunterWin) {
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getHunterWinTitle()).formatted(Formatting.RED)));

                    if (timeOver) {
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getTimeLimitSubtitle()).formatted(Formatting.DARK_RED)));
                    } else {
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getRunnerDiedSubtitle()).formatted(Formatting.DARK_RED)));
                    }

                } else {
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(config.getRunnerWinTitle()).formatted(Formatting.GREEN)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(config.getEnderDragonDiedSubtitle()).formatted(Formatting.DARK_GREEN)));
                }
            }

            if (config.isManhuntSounds() && manhuntSounds.get(player.getUuid())) {
                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.MASTER, 1.0F, 2.0F);
            }
        }
    }

    public static void resetGame(MinecraftServer server) {
        setGameState(GameState.PREGAME);

        setStarted(false);

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

        Scoreboard scoreboard = server.getScoreboard();

        scoreboard.getTeam("hunters").setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        scoreboard.getTeam("runners").setCollisionRule(AbstractTeam.CollisionRule.NEVER);

        if (config.isTeamColor()) {
            scoreboard.getTeam("hunters").setColor(config.getHuntersColor());
            scoreboard.getTeam("runners").setColor(config.getRunnersColor());
        } else {
            scoreboard.getTeam("hunters").setColor(Formatting.RESET);
            scoreboard.getTeam("runners").setColor(Formatting.RESET);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.teleport(lobby, 0.5, 63, 0.5, PositionFlag.ROT, 0, 0);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setOnFire(false);
            player.setFireTicks(0);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);
            player.setScore(0);
            player.clearStatusEffects();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));

            for (AdvancementEntry advancement : server.getAdvancementLoader().getAdvancements()) {
                AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
                for (String criteria : progress.getObtainedCriteria()) {
                    player.getAdvancementTracker().revokeCriterion(advancement, criteria);
                }
            }

            player.changeGameMode(GameMode.ADVENTURE);



            if (!hasTeam.get(player.getUuid())) {
                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
            }
        }

        hasTeam.forEach((uuid, bool) -> {
            if (server.getPlayerManager().getPlayer(uuid) == null) {
                hasTeam.remove(uuid);
            }
        });

        hasPlayed.clear();
        playerSpawn.clear();
        hunterCoords.clear();
        runnerCoords.clear();

        overworldHandle.delete();
        netherHandle.delete();
        endHandle.delete();

        setWorldSpawnPos(new BlockPos(0, 0, 0));
        setOverworldSpawn(new BlockPos(0, 0, 0));

        try {
            FileUtils.deleteDirectory(getGameDir().resolve("world/dimensions/manhunt/overworld").toFile());
            FileUtils.deleteDirectory(getGameDir().resolve("world/dimensions/manhunt/the_nether").toFile());
            FileUtils.deleteDirectory(getGameDir().resolve("world/dimensions/manhunt/the_end").toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to delete dimension worlds");
        }

        loadManhuntWorlds(server);
    }

    public static void setPlayerSpawn(ServerWorld world, ServerPlayerEntity player) {
        if (getWorldSpawnPos().equals(new BlockPos(0, 0, 0))) {
            setWorldSpawnPos(setupSpawn(world));
        }

        if (getOverworldSpawn().equals(new BlockPos(0, 0, 0)) || config.getSpawnRadius() != 0) {
            BlockPos blockPos = getWorldSpawnPos();
            long l;
            long m;
            int i = Math.max(0, config.getSpawnRadius());
            int j = MathHelper.floor(world.getWorldBorder().getDistanceInsideBorder(blockPos.getX(), blockPos.getZ()));
            if (j < i) {
                i = j;
            }
            if (j <= 1) {
                i = 1;
            }
            int k = (m = (l = i * 2L + 1) * l) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)m;
            int n = k <= 16 ? k - 1 : 17;
            int o = Random.create().nextInt(k);
            for (int p = 0; p < k; ++p) {
                int q = (o + n * p) % k;
                int r = q % (i * 2 + 1);
                int s = q / (i * 2 + 1);
                BlockPos blockPos2 = findOverworldSpawn(world, blockPos.getX() + r - i, blockPos.getZ() + s - i);
                if (blockPos2 == null) continue;
                setOverworldSpawn(blockPos2);
                playerSpawn.put(player.getUuid(), getOverworldSpawn());
                break;
            }
        } else {
            playerSpawn.put(player.getUuid(), getOverworldSpawn());
        }
    }

    @Nullable
    private static BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
        int i;
        boolean bl = world.getDimension().hasCeiling();
        WorldChunk worldChunk = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        i = bl ? world.getChunkManager().getChunkGenerator().getSpawnHeight(world) : worldChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x & 0xF, z & 0xF);
        if (i < world.getBottomY()) {
            return null;
        }
        int j = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 0xF, z & 0xF);
        if (j <= i && j > worldChunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR, x & 0xF, z & 0xF)) {
            return null;
        }
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int k = i + 1; k >= world.getBottomY(); --k) {
            mutable.set(x, k, z);
            BlockState blockState = world.getBlockState(mutable);
            if (!blockState.getFluidState().isEmpty()) break;
            if (!Block.isFaceFullSquare(blockState.getCollisionShape(world, mutable), Direction.UP)) continue;
            return mutable.up().toImmutable();
        }
        return null;
    }

    private static BlockPos setupSpawn(ServerWorld world) {
        ServerChunkManager serverChunkManager = world.getChunkManager();
        ChunkPos chunkPos = new ChunkPos(serverChunkManager.getNoiseConfig().getMultiNoiseSampler().findBestSpawnPosition());
        int i = serverChunkManager.getChunkGenerator().getSpawnHeight(world);
        if (i < world.getBottomY()) {
            BlockPos blockPos = chunkPos.getStartPos();
            world.getTopY(Heightmap.Type.WORLD_SURFACE, blockPos.getX() + 8, blockPos.getZ() + 8);
        }
        BlockPos blockPos = chunkPos.getStartPos().add(8, i, 8);
        int j = 0;
        int k = 0;
        int l = 0;
        int m = -1;
        for (int o = 0; o < MathHelper.square(11); ++o) {
            if (j >= -5 && j <= 5 && k >= -5 && k <= 5 && (blockPos = SpawnLocating.findServerSpawnPoint(world, new ChunkPos(chunkPos.x + j, chunkPos.z + k))) != null) {
                break;
            }
            if (j == k || j < 0 && j == -k || j > 0 && j == 1 - k) {
                int p = l;
                l = -m;
                m = p;
            }
            j += l;
            k += m;
        }
        return blockPos;
    }

    public static void openPreferencesGui(ServerPlayerEntity player) {
        SimpleGui preferencesGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        preferencesGui.setTitle(Text.translatable("manhunt.preferences"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean bool;

        loreList = new ArrayList<>();
        name = "gametitles";
        item = Items.OAK_SIGN;
        bool = gameTitles.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        Item gameTitlesItem = item;
        boolean gameTitlesBool = bool;
        preferencesGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(gameTitlesItem)) {
                        gameTitles.put(player.getUuid(), !gameTitlesBool);
                        player.getItemCooldownManager().set(gameTitlesItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPreferencesGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "manhuntsounds";
        item = Items.FIRE_CHARGE;
        bool = manhuntSounds.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        Item manhuntSoundsItem = item;
        boolean manhuntSoundsBool = bool;
        preferencesGui.setSlot(1, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(manhuntSoundsItem)) {
                        manhuntSounds.put(player.getUuid(), !manhuntSoundsBool);
                        player.getItemCooldownManager().set(manhuntSoundsItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPreferencesGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "nightvision";
        item = Items.GLOWSTONE;
        bool = nightVision.get(player.getUuid());

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        Item nightVisionItem = item;
        boolean nightVisionBool = bool;
        preferencesGui.setSlot(slot, new GuiElementBuilder(item)
            .setName(Text.translatable("manhunt." + name))
            .setLore(loreList)
            .setCallback(() -> {
                if (!player.getItemCooldownManager().isCoolingDown(nightVisionItem)) {
                    nightVision.put(player.getUuid(), !nightVisionBool);
                    player.getItemCooldownManager().set(nightVisionItem, 10);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openPreferencesGui(player);
                }
            })
        );

        preferencesGui.open();
    }

    public static void openSettingsGui(ServerPlayerEntity player) {
        SimpleGui settingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);

        settingsGui.setTitle(Text.translatable("manhunt.settings"));

        List<Text> loreList;
        String name;
        int slot = 0;
        Item item;
        boolean bool;
        int integer;

        loreList = new ArrayList<>();
        name = "preloadchunks";
        item = Items.GRASS_BLOCK;
        bool = config.isPreloadChunks();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var preloadChunksItem = item;
        var preloadChunksBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(preloadChunksItem)) {
                        if (!preloadChunksBool) {
                            schedulePreload(player.getServer());
                        } else {
                            if (isChunkyIntegration()) {
                                ChunkyAPI chunky = ChunkyProvider.get().getApi();

                                chunky.cancelTask("manhunt:overworld");
                                chunky.cancelTask("manhunt:the_nether");
                            }
                        }
                        config.setPreloadChunks(!preloadChunksBool);
                        config.save();
                        player.getItemCooldownManager().set(preloadChunksItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "automaticcompass";
        item = Items.COMPASS;
        bool = config.isAutomaticCompass();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var automaticCompassItem = item;
        var automaticCompassBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(automaticCompassItem)) {
                        config.setAutomaticCompass(!automaticCompassBool);
                        config.save();
                        player.getItemCooldownManager().set(automaticCompassItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "teamcolor";
        item = Items.WHITE_BANNER;
        bool = config.isTeamColor();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));

        var teamColorItem = item;
        var teamColorBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) -> openTeamColorGui(player, type, teamColorItem, teamColorBool))
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runnerheadstart";
        item = Items.GOLDEN_BOOTS;
        integer = config.getRunnerHeadstart();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getRunnerHeadstart())).formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer != 10 && integer != 20 && integer != 30) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getRunnerHeadstart())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 10) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("10").formatted(Formatting.RED), Text.literal("20"), Text.literal("30")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 20) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("10"), Text.literal("20").formatted(Formatting.YELLOW), Text.literal("30")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("10"), Text.literal("20"), Text.literal("30").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));

        Item runnerHeadstartItem = item;
        int runnerHeadstartInt = integer;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .hideDefaultTooltip()
                .setCallback((index, type, action) -> {
                    if (!player.getItemCooldownManager().isCoolingDown(runnerHeadstartItem)) {
                        if (!type.shift) {
                            if (runnerHeadstartInt != 10 && runnerHeadstartInt != 20) {
                                config.setRunnerHeadstart(10);
                            } else {
                                if (runnerHeadstartInt == 10) {
                                    config.setRunnerHeadstart(20);
                                } else {
                                    config.setRunnerHeadstart(30);
                                }
                            }
                            config.save();
                            player.getItemCooldownManager().set(runnerHeadstartItem, 10);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openSettingsGui(player);
                        } else {
                            AnvilInputGui runnerHeadstartGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                            .setCallback(() -> {
                                                int value = runnerHeadstartInt;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                }

                                                config.setRunnerHeadstart(value);
                                                config.save();
                                                player.getItemCooldownManager().set(runnerHeadstartItem, 10);
                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                openSettingsGui(player);
                                            })
                                    );
                                }
                            };

                            runnerHeadstartGui.setTitle(Text.translatable("manhunt.entervalue"));
                            runnerHeadstartGui.setDefaultInputValue("");
                            runnerHeadstartGui.open();
                        }
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "timelimit";
        item = Items.CLOCK;
        integer = config.getTimeLimit();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getTimeLimit())).formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer != 30 && integer != 60 && integer != 90) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getTimeLimit())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 30) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("30").formatted(Formatting.RED), Text.literal("60"), Text.literal("90")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 60) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("30"), Text.literal("60").formatted(Formatting.YELLOW), Text.literal("90")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("30"), Text.literal("60"), Text.literal("90").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));

        Item timeLimitItem = item;
        int timeLimitInt = integer;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (!player.getItemCooldownManager().isCoolingDown(timeLimitItem)) {
                        if (!type.shift) {
                            if (timeLimitInt != 30 && timeLimitInt != 60) {
                                config.setTimeLimit(30);
                            } else {
                                if (timeLimitInt == 30) {
                                    config.setTimeLimit(60);
                                } else {
                                    config.setTimeLimit(90);
                                }
                            }
                            config.save();
                            player.getItemCooldownManager().set(timeLimitItem, 10);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openSettingsGui(player);
                        } else {
                            AnvilInputGui timeLimitGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                            .setCallback(() -> {
                                                int value = timeLimitInt;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                }

                                                config.setTimeLimit(value);
                                                config.save();
                                                player.getItemCooldownManager().set(timeLimitItem, 10);
                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                openSettingsGui(player);
                                            })
                                    );
                                }
                            };

                            timeLimitGui.setTitle(Text.translatable("manhunt.entervalue"));
                            timeLimitGui.setDefaultInputValue("");
                            timeLimitGui.open();
                        }
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "runnerglow";
        item = Items.SPECTRAL_ARROW;
        bool = config.isRunnerGlow();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var runnerGlowItem = item;
        var runnerGlowBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(runnerGlowItem)) {
                        config.setRunnerGlow(!runnerGlowBool);
                        config.save();
                        player.getItemCooldownManager().set(runnerGlowItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "gamedifficulty";
        item = Items.CREEPER_HEAD;
        Difficulty difficulty = config.getGameDifficulty();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (difficulty == Difficulty.EASY) {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("options.difficulty.easy").formatted(Formatting.GREEN), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (difficulty == Difficulty.NORMAL) {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal").formatted(Formatting.YELLOW), Text.translatable("options.difficulty.hard")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        Item gameDifficultyItem = item;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(gameDifficultyItem)) {
                        if (difficulty == Difficulty.EASY) {
                            config.setGameDifficulty(Difficulty.NORMAL);
                        } else if (difficulty == Difficulty.NORMAL) {
                            config.setGameDifficulty(Difficulty.HARD);
                        } else {
                            config.setGameDifficulty(Difficulty.EASY);
                        }
                        player.getItemCooldownManager().set(gameDifficultyItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );

        loreList = new ArrayList<>();
        name = "worldborder";
        item = Items.PRISMARINE_WALL;
        integer = config.getWorldBorder();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getWorldBorder())).formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer != 2816 && integer != 5888 && integer != 59999968) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getWorldBorder())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 2816) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("1st ring").formatted(Formatting.RED), Text.literal("2nd ring"), Text.literal("Maximum")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 5888) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("1st ring"), Text.literal("2nd ring").formatted(Formatting.YELLOW), Text.literal("Maximum")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("1st ring"), Text.literal("2nd ring"), Text.literal("Maximum").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));

        Item worldBorderItem = item;
        int worldBorderInt = integer;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (!player.getItemCooldownManager().isCoolingDown(worldBorderItem)) {
                        if (!type.shift) {
                            if (worldBorderInt != 2816 && worldBorderInt != 5888) {
                                config.setWorldBorder(2816);


                            } else {
                                if (worldBorderInt == 2816) {
                                    config.setWorldBorder(5888);
                                } else {
                                    config.setWorldBorder(59999968);
                                }
                            }
                            config.save();
                            player.getItemCooldownManager().set(worldBorderItem, 10);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openSettingsGui(player);
                        } else {
                            AnvilInputGui worldBorderGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                            .setCallback(() -> {
                                                int value = worldBorderInt;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                }

                                                config.setWorldBorder(value);
                                                config.save();
                                                player.getItemCooldownManager().set(worldBorderItem, 10);
                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                openSettingsGui(player);
                                            })
                                    );
                                }
                            };

                            worldBorderGui.setTitle(Text.translatable("manhunt.entervalue"));
                            worldBorderGui.setDefaultInputValue("");
                            worldBorderGui.open();
                        }
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "spawnradius";
        item = Items.BEDROCK;
        integer = config.getSpawnRadius();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer != 0 && integer != 5 && integer != 10) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getSpawnRadius())).formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            if (integer == 0) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("0").formatted(Formatting.RED), Text.literal("5"), Text.literal("10")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else if (integer == 5) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("0"), Text.literal("5").formatted(Formatting.YELLOW), Text.literal("10")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            } else {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("0"), Text.literal("5"), Text.literal("10").formatted(Formatting.GREEN)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").setStyle(Style.EMPTY.withColor(Formatting.GOLD).withItalic(false)));

        Item spawnRadiusItem = item;
        int spawnRadiusInt = integer;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback((index, type, action) ->  {
                    if (!player.getItemCooldownManager().isCoolingDown(spawnRadiusItem)) {
                        if (!type.shift) {
                            if (spawnRadiusInt != 0 && spawnRadiusInt != 5) {
                                config.setSpawnRadius(0);
                            } else {
                                if (spawnRadiusInt == 0) {
                                    config.setSpawnRadius(5);
                                } else {
                                    config.setSpawnRadius(10);
                                }
                            }
                            config.save();
                            player.getItemCooldownManager().set(spawnRadiusItem, 10);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openSettingsGui(player);
                        } else {
                            AnvilInputGui spawnRadiusGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                            .setCallback(() -> {
                                                int value = spawnRadiusInt;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                }

                                                config.setSpawnRadius(value);
                                                config.save();
                                                player.getItemCooldownManager().set(spawnRadiusItem, 10);
                                                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                                                openSettingsGui(player);
                                            })
                                    );
                                }
                            };

                            spawnRadiusGui.setTitle(Text.translatable("manhunt.entervalue"));
                            spawnRadiusGui.setDefaultInputValue("");
                            spawnRadiusGui.open();
                        }
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "spectatewin";
        item = Items.SPYGLASS;
        bool = config.isSpectateWin();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var spectateWinItem = item;
        var spectateWinBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(spectateWinItem)) {
                        config.setSpectateWin(!spectateWinBool);
                        config.save();
                        player.getItemCooldownManager().set(spectateWinItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "friendlyfire";
        item = Items.EMERALD;
        integer = config.getFriendlyFire();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("manhunt.lore." + name + ".always").formatted(Formatting.GREEN), Text.translatable("manhunt.lore." + name + ".perplayer"), Text.translatable("manhunt.lore." + name + ".never")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else if (integer == 1) {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("manhunt.lore." + name + ".always"), Text.translatable("manhunt.lore." + name + ".perplayer").formatted(Formatting.YELLOW), Text.translatable("manhunt.lore." + name + ".never")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("manhunt.lore." + name + ".always"), Text.translatable("manhunt.lore." + name + ".perplayer"), Text.translatable("manhunt.lore." + name + ".never").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        Item friendlyFireItem = item;
        int friendlyFireInt = integer;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name).formatted(Formatting.WHITE))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(friendlyFireItem)) {
                        if (friendlyFireInt == 0) {
                            config.setFriendlyFire(1);
                        } else if (friendlyFireInt == 1) {
                            config.setFriendlyFire(2);
                        } else {
                            config.setFriendlyFire(0);
                        }
                        config.save();
                        player.getItemCooldownManager().set(friendlyFireItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "bedexplosions";
        item = Items.RED_BED;
        bool = config.isBedExplosions();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("manhunt.lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var bedExplosionsItem = item;
        var bedExplosionsBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(bedExplosionsItem)) {
                        config.setBedExplosions(!bedExplosionsBool);
                        config.save();
                        player.getItemCooldownManager().set(bedExplosionsItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "lavapvpinnether";
        item = Items.LAVA_BUCKET;
        bool = config.isBedExplosions();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        loreList.add(Text.translatable("manhunt.lore." + name + ".second").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var lavaPvpInNetherItem = item;
        var lavaPvpInNetherBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(lavaPvpInNetherItem)) {
                        config.setBedExplosions(!lavaPvpInNetherBool);
                        config.save();
                        player.getItemCooldownManager().set(lavaPvpInNetherItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "gametitles";
        item = Items.OAK_SIGN;
        bool = config.isGameTitles();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var gameTitlesItem = item;
        var gameTitlesBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(gameTitlesItem)) {
                        config.setGameTitles(!gameTitlesBool);
                        config.save();
                        player.getItemCooldownManager().set(gameTitlesItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "manhuntsounds";
        item = Items.FIRE_CHARGE;
        bool = config.isManhuntSounds();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var manhuntSoundsItem = item;
        var manhuntSoundsBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(manhuntSoundsItem)) {
                        config.setManhuntSounds(!manhuntSoundsBool);
                        config.save();
                        player.getItemCooldownManager().set(manhuntSoundsItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );
        slot++;

        loreList = new ArrayList<>();
        name = "nightvision";
        item = Items.GLOWSTONE;
        bool = config.isNightVision();

        loreList.add(Text.translatable("manhunt.lore." + name).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
        }

        var nightVisionItem = item;
        var nightVisionBool = bool;
        settingsGui.setSlot(slot, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(nightVisionItem)) {
                        config.setNightVision(!nightVisionBool);
                        config.save();
                        player.getItemCooldownManager().set(nightVisionItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );

        settingsGui.open();
    }

    private static void openTeamColorGui(ServerPlayerEntity player, ClickType clickType, Item item, Boolean bool) {
        if (!player.getItemCooldownManager().isCoolingDown(item)) {
            if (clickType.shift) {
                var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                List<Text> loreList = new ArrayList<>();

                loreList.add(Text.translatable("manhunt.lore.hunterscolor").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

                teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                        .setName(Text.translatable("manhunt.hunterscolor").formatted(config.getHuntersColor()))
                        .setLore(loreList)
                        .setCallback(() -> {
                            SimpleGui huntersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                            huntersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                    .setName(Text.translatable("color.minecraft.white"))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.RESET);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                    .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.GRAY);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                    .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.DARK_GRAY);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                    .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.BLACK);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                    .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.RED);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                    .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.GOLD);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                    .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.YELLOW);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                    .setName(Text.translatable("manhunt.goback").formatted(Formatting.WHITE))
                                    .setCallback(teamColorGui::open)
                            );

                            huntersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                    .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.GREEN);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                    .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.DARK_GREEN);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                    .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.DARK_AQUA);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                    .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.BLUE);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                    .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.DARK_BLUE);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                    .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.DARK_PURPLE);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                    .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                    .setCallback(() -> {
                                        config.setHuntersColor(Formatting.LIGHT_PURPLE);
                                        config.save();
                                        player.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            huntersColorGui.open();
                        })
                );

                loreList = new ArrayList<>();

                loreList.add(Text.translatable("manhunt.lore.runnerscolor").setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));

                teamColorGui.setSlot(5, new GuiElementBuilder(Items.BLAZE_POWDER)
                        .setName(Text.translatable("manhunt.runnerscolor").formatted(config.getRunnersColor()))
                        .setLore(loreList)
                        .setCallback(() -> {
                            SimpleGui runnersColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);

                            runnersColorGui.setSlot(0, new GuiElementBuilder(Items.WHITE_WOOL)
                                    .setName(Text.translatable("color.minecraft.white"))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.RESET);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(1, new GuiElementBuilder(Items.LIGHT_GRAY_WOOL)
                                    .setName(Text.translatable("color.minecraft.light_gray").formatted(Formatting.GRAY))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.GRAY);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(2, new GuiElementBuilder(Items.GRAY_WOOL)
                                    .setName(Text.translatable("color.minecraft.gray").formatted(Formatting.DARK_GRAY))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.DARK_GRAY);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(3, new GuiElementBuilder(Items.BLACK_WOOL)
                                    .setName(Text.translatable("color.minecraft.black").formatted(Formatting.BLACK))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.BLACK);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(4, new GuiElementBuilder(Items.RED_WOOL)
                                    .setName(Text.translatable("color.minecraft.red").formatted(Formatting.RED))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.RED);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(5, new GuiElementBuilder(Items.ORANGE_WOOL)
                                    .setName(Text.translatable("color.minecraft.orange").formatted(Formatting.GOLD))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.GOLD);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(6, new GuiElementBuilder(Items.YELLOW_WOOL)
                                    .setName(Text.translatable("color.minecraft.yellow").formatted(Formatting.YELLOW))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.YELLOW);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                                    .setName(Text.translatable("manhunt.goback").formatted(Formatting.WHITE))
                                    .setCallback(teamColorGui::open)
                            );

                            runnersColorGui.setSlot(9, new GuiElementBuilder(Items.LIME_WOOL)
                                    .setName(Text.translatable("color.minecraft.lime").formatted(Formatting.GREEN))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.GREEN);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(10, new GuiElementBuilder(Items.GREEN_WOOL)
                                    .setName(Text.translatable("color.minecraft.green").formatted(Formatting.DARK_GREEN))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.DARK_GREEN);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(11, new GuiElementBuilder(Items.CYAN_WOOL)
                                    .setName(Text.translatable("color.minecraft.cyan").formatted(Formatting.DARK_AQUA))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.DARK_AQUA);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(12, new GuiElementBuilder(Items.LIGHT_BLUE_WOOL)
                                    .setName(Text.translatable("color.minecraft.light_blue").formatted(Formatting.BLUE))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.BLUE);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(13, new GuiElementBuilder(Items.BLUE_WOOL)
                                    .setName(Text.translatable("color.minecraft.blue").formatted(Formatting.DARK_BLUE))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.DARK_BLUE);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(14, new GuiElementBuilder(Items.PURPLE_WOOL)
                                    .setName(Text.translatable("color.minecraft.purple").formatted(Formatting.DARK_PURPLE))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.DARK_PURPLE);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.setSlot(15, new GuiElementBuilder(Items.MAGENTA_WOOL)
                                    .setName(Text.translatable("color.minecraft.magenta").formatted(Formatting.LIGHT_PURPLE))
                                    .setCallback(() -> {
                                        config.setRunnersColor(Formatting.LIGHT_PURPLE);
                                        config.save();
                                        player.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
                                        openTeamColorGui(player, clickType, item, bool);
                                    })
                            );

                            runnersColorGui.open();
                        })
                );

                teamColorGui.setSlot(8, new GuiElementBuilder(Items.STRUCTURE_VOID)
                        .setName(Text.translatable("manhunt.goback").formatted(Formatting.WHITE))
                        .setCallback(() -> openSettingsGui(player))
                );

                teamColorGui.open();
            } else {
                config.setTeamColor(!bool);
                config.save();
                player.getItemCooldownManager().set(item, 10);
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                openSettingsGui(player);
            }
        }
    }
}