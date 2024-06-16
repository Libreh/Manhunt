package manhunt.game;

import manhunt.mixin.ServerWorldAccessor;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

        server.setDifficulty(config.getDifficulty(), true);

        for (ServerWorld serverWorld : server.getWorlds()) {
            ((ServerWorldAccessor) serverWorld).getWorldProperties().setTime(0);
            serverWorld.setTimeOfDay(0);
            serverWorld.resetWeather();
        }

        overworld.getWorldBorder().setSize(config.getWorldBorder());

        server.getScoreboard().getTeam("hunters").setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        server.getScoreboard().getTeam("runners").setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);

        if (config.getRunnerHeadstart() != 0) setHeadstart(true);

        if (config.isRunnerPreferences() && server.getScoreboard().getTeam("runners").getPlayerList().size() == 1) {
            String playerName = server.getScoreboard().getTeam("runners").getPlayerList().iterator().next();

            config.setBedExplosions(bedExplosions.get(server.getPlayerManager().getPlayer(playerName).getUuid()));
            config.setLavaPvpInNether(lavaPvpInNether.get(server.getPlayerManager().getPlayer(playerName).getUuid()));
        }

        playerEffects.clear();
        playerPos.clear();
        playerYaw.clear();
        playerPitch.clear();
        playerFood.clear();
        playerSaturation.clear();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            startedParkour.put(player.getUuid(), true);

            hasPlayed.add(player.getUuid());

            if (!playerSpawn.containsKey(player.getUuid())) {
                setPlayerSpawn(overworld, player);
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
            player.teleport(overworld, playerX, playerY, playerZ, 0, 0);
            player.setSpawnPoint(overworldWorld, playerSpawn.get(player.getUuid()), 0, true, false);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setHealth(20.0F);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5.0F);
            player.getHungerManager().setExhaustion(0.0F);

            player.changeGameMode(GameMode.SURVIVAL);

            if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                if (config.isRunnersGlow()) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, StatusEffectInstance.INFINITE, 255, false, false));
                }
            }

            if (isHeadstart() && player.getScoreboardTeam() != null && player.getScoreboardTeam() == player.getScoreboard().getTeam("hunters")) {
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0);
                player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(0);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, StatusEffectInstance.INFINITE, 255, false, false,false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
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
        NbtCompound playerTag = trackedPlayer.writeNbt(new NbtCompound());
        NbtList positions = playerTag.getList("Positions", 10);
        int i;
        for (i = 0; i < positions.size(); ++i) {
            NbtCompound compound = positions.getCompound(i);
            if (compound.getString("LodestoneDimension").equals(player.writeNbt(new NbtCompound()).getString("Dimension"))) {
                NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().copyFrom(compound);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                break;
            }
        }

        int[] is = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getIntArray("LodestonePos");

        BlockPos blockPos = new BlockPos(is[0], is[1], is[2]);

        stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.of(GlobalPos.create(player.getWorld().getRegistryKey(), blockPos)), false));
    }

    public static void endGame(MinecraftServer server, boolean hunterWin, boolean timeOver) {
        setGameState(GameState.POSTGAME);

        LOGGER.info("Seed: " + overworld.getSeed());

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (config.isSpectateOnWin()) {
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

    public static void resetGame(MinecraftServer server, long seed) {
        setGameState(GameState.PREGAME);

        setHeadstart(false);

        setHeadstartTime(0);

        setPaused(false);

        setDragonKilled(false);

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
            player.getEnderChestInventory().clear();
            player.getInventory().clear();
            player.setOnFire(false);
            player.setFireTicks(0);
            player.setHealth(20.0F);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5.0F);
            player.getHungerManager().setExhaustion(0.0F);
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

            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
            player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
            player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

            player.changeGameMode(GameMode.ADVENTURE);

            if (player.getScoreboardTeam() == null) {
                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
            }
        }

        if (config.getTeamPreset() != 1) {
            if (config.getTeamPreset() == 2) {
                if (playerList == null || playerList.isEmpty()) {
                    playerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                }

                for (ServerPlayerEntity serverPlayer : playerList) {
                    if (playerList.get(0) == null) {
                        playerList.remove(playerList.get(0));
                    }
                }

                ServerPlayerEntity runner = playerList.get(0);

                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("hunters"));
                }

                server.getScoreboard().addScoreHolderToTeam(runner.getNameForScoreboard(), server.getScoreboard().getTeam("runners"));

                playerList.remove(runner);
            } else if (config.getTeamPreset() == 3) {
                List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());

                Collections.shuffle(players);

                ServerPlayerEntity hunter = players.get(0);

                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("runners"));
                }

                server.getScoreboard().addScoreHolderToTeam(hunter.getNameForScoreboard(), server.getScoreboard().getTeam("hunters"));
            } else {
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("runners"));
                }
            }
        }

        hunterCoords.clear();
        runnerCoords.clear();
        readyList.clear();
        hasPlayed.clear();
        leftOnPause.clear();
        playerSpawn.clear();
        parkourTimer.clear();
        startedParkour.clear();
        finishedParkour.clear();

        setWorldSpawnPos(new BlockPos(0, 0, 0));
        setOverworldSpawn(new BlockPos(0, 0, 0));

        try {
            FileUtils.deleteDirectory(getGameDir().resolve("world/dimensions/manhunt/overworld").toFile());
            FileUtils.deleteDirectory(getGameDir().resolve("world/dimensions/manhunt/the_nether").toFile());
            FileUtils.deleteDirectory(getGameDir().resolve("world/dimensions/manhunt/the_end").toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to delete dimension worlds");
        }

        loadManhuntWorlds(server, seed);
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
}