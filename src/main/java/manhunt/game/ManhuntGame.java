package manhunt.game;

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
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
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
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static manhunt.ManhuntMod.*;

public class ManhuntGame {
    public static void gameStart(MinecraftServer server) {
        server.setFlightEnabled(true);

        state = GameState.PLAYING;

        if (config.isSetMotd()) {
            server.setMotd(state.getColor() + "[" + state.getMotd() + "]§f Minecraft MANHUNT");
        }

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
        server.getGameRules().get(GameRules.SPAWN_RADIUS).set(spawnRadius, server);

        if (chunkyLoaded && config.isChunky()) {
            ChunkyAPI chunky = ChunkyProvider.get().getApi();

            chunky.cancelTask("manhunt:overworld");
            chunky.cancelTask("manhunt:the_nether");
        }

        Scoreboard scoreboard = server.getScoreboard();
        Team huntersTeam = scoreboard.getTeam("hunters");
        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        huntersTeam.setShowFriendlyInvisibles(false);

        Team runnersTeam = scoreboard.getTeam("runners");
        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        runnersTeam.setShowFriendlyInvisibles(false);

        if (config.isTeamColor()) {
            huntersTeam.setColor(config.getHuntersColor());
            runnersTeam.setColor(config.getRunnersColor());
        } else {
            Formatting reset = Formatting.RESET;
            config.setHuntersColor(reset);
            config.setRunnersColor(reset);
            huntersTeam.setColor(reset);
            huntersTeam.setColor(reset);
        }

        WorldBorder worldBorder = server.getOverworld().getWorldBorder();

        worldBorder.setCenter(0, 0);
        worldBorder.setSize(config.getWorldBorder());

        if (config.getTimeLimit() != 0) {
            duration = true;
            durationTime = config.getTimeLimit() * 60 * 20;
        }

        if (config.getRunnerHeadstart() != 0) {
            headstart = true;
            headstartTime = (config.getRunnerHeadstart() * 20) + 20;
        }

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

        if (config.isRunnersGlow()) {
            for (String playerName : server.getScoreboard().getTeam("runners").getPlayerList()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.GLOWING,
                        StatusEffectInstance.INFINITE,
                        255,
                        false,
                        false)
                );
            }
        }

        if (headstart) {
            for (String playerName : server.getScoreboard().getTeam("hunters").getPlayerList()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
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
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            startedParkour.put(player.getUuid(), true);

            if (!playerSpawnPos.containsKey(player.getUuid())) {
                setPlayerSpawn(getOverworld(), player);
            }

            double playerX = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getX()));
            double playerY = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getY()));
            double playerZ = Double.parseDouble(String.valueOf(playerSpawnPos.get(player.getUuid()).getZ()));
            player.teleport(
                    getOverworld(),
                    playerX,
                    playerY,
                    playerZ,
                    0,
                    0
            );
            player.setSpawnPoint(
                    getOverworld().getRegistryKey(),
                    playerSpawnPos.get(player.getUuid()),
                    0f,
                    true,
                    false
            );
            player.clearStatusEffects();
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setHealth(player.getMaxHealth());
            player.setAir(player.getMaxAir());
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5f);
            player.getHungerManager().setExhaustion(0f);

            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
            player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0.41999998688697815);
            player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(1.0);

            player.getInventory().clear();
            player.changeGameMode(getGameMode());

            Stats.MINED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.CRAFTED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.USED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.BROKEN.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.PICKED_UP.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.DROPPED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.KILLED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.KILLED_BY.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.CUSTOM.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));

            if (gameTitles.get(player.getUuid())) {
                player.networkHandler.sendPacket(new TitleS2CPacket(
                        Text.literal(config.getGameStartTitle()))
                );
                player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.literal(config.getGameStartSubtitle()).formatted(Formatting.GRAY))
                );
            }

            if (manhuntSounds.get(player.getUuid())) {
                player.playSoundToPlayer(
                        SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                        SoundCategory.MASTER,
                        0.5f,
                        2f
                );
            }

            if (nightVision.get(player.getUuid()))
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NIGHT_VISION,
                        StatusEffectInstance.INFINITE,
                        255,
                        false,
                        false)
                );
        }
    }

    public static void updateCompass(ServerPlayerEntity player, ItemStack stack, ServerPlayerEntity trackedPlayer) {
        NbtCompound playerTag = trackedPlayer.writeNbt(new NbtCompound());
        NbtList positions = playerTag.getList("Positions", 10);
        int i;
        for (i = 0; i < positions.size(); ++i) {
            NbtCompound compound = positions.getCompound(i);
            if (compound.getString("LodestoneDimension").equals(
                    player.writeNbt(new NbtCompound()).getString("Dimension"))
            ) {
                NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().copyFrom(compound);
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                break;
            }
        }

        int[] is = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getIntArray("LodestonePos");

        BlockPos blockPos = new BlockPos(is[0], is[1], is[2]);

        stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(
                Optional.of(
                        GlobalPos.create(player.getWorld().getRegistryKey(), blockPos)
                ), false)
        );
    }

    public static void gameOver(MinecraftServer server, boolean hunterWin) {
        state = GameState.POSTGAME;

        if (config.isSetMotd()) {
            server.setMotd(state.getColor() + "[" + state.getMotd() + "]§f Minecraft MANHUNT");
        }

        LOGGER.info("Seed: {}", getOverworld().getSeed());

        server.getPlayerManager().broadcast(Text.translatable("text.both", Text.literal("Seed:"), Text.literal(String.valueOf(getOverworld().getSeed())).formatted(Formatting.GREEN)), false);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.changeGameMode(getGameMode());

            if (config.isGameTitles() && gameTitles.get(player.getUuid())) {
                if (hunterWin) {
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                            Text.translatable("title.hunters_won").formatted(config.getHuntersColor()))
                    );
                } else {
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                            Text.translatable("title.runners_won").formatted(config.getRunnersColor())))
                    ;
                }
                player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.translatable("title.gg").formatted(Formatting.AQUA))
                );
            }

            if (config.isManhuntSounds() && manhuntSounds.get(player.getUuid())) {
                player.playSoundToPlayer(
                        SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                        SoundCategory.MASTER,
                        1f,
                        2f
                );
            }
        }
    }

    public static void gameReset(MinecraftServer server, long seed) {
        state = GameState.PREGAME;

        if (config.isSetMotd()) {
            server.setMotd(state.getColor() + "[" + state.getMotd() + "]§f Minecraft MANHUNT");
        }

        duration = false;
        headstart = false;
        headstartTime = 0;
        paused = false;

        GameRules gameRules = server.getWorld(lobbyRegistry).getGameRules();

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
        Team huntersTeam = scoreboard.getTeam("hunters");
        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        huntersTeam.setShowFriendlyInvisibles(true);

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

        for (String playerName : huntersTeam.getPlayerList()) {
            if (server.getPlayerManager().getPlayer(playerName) == null) {
                scoreboard.removeScoreHolderFromTeam(playerName, huntersTeam);
            }
        }

        for (String playerName : runnersTeam.getPlayerList()) {
            if (server.getPlayerManager().getPlayer(playerName) == null) {
                scoreboard.removeScoreHolderFromTeam(playerName, huntersTeam);
            }
        }

        WorldBorder worldBorder = server.getOverworld().getWorldBorder();

        worldBorder.setCenter(0, 0);
        worldBorder.setSize(59999968);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.teleport(
                    server.getWorld(lobbyRegistry),
                    lobbySpawn.getX(),
                    lobbySpawn.getY(),
                    lobbySpawn.getZ(),
                    PositionFlag.ROT,
                    180f,
                    0
            );
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
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5.0F);
            player.getHungerManager().setExhaustion(0.0F);
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);
            player.setScore(0);
            player.getEnderChestInventory().clear();
            player.getInventory().clear();
            player.changeGameMode(getGameMode());

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

        if (config.getTeamPreset() != 1) {
            if (config.getTeamPreset() == 2) {
                if (playerList == null || playerList.isEmpty()) {
                    playerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                }

                for (ServerPlayerEntity serverPlayer : playerList) {
                    ServerPlayerEntity player = playerList.get(0);
                    if (player == null) {
                        playerList.remove(serverPlayer);
                    }
                }

                ServerPlayerEntity runner = playerList.get(0);

                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(
                            serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("hunters")
                    );
                }

                server.getScoreboard().addScoreHolderToTeam(
                        runner.getNameForScoreboard(), server.getScoreboard().getTeam("runners")
                );

                playerList.remove(runner);
            } else if (config.getTeamPreset() == 3) {
                List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());

                Collections.shuffle(players);

                ServerPlayerEntity hunter = players.get(0);

                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(
                            serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("runners")
                    );
                }

                server.getScoreboard().addScoreHolderToTeam(
                        hunter.getNameForScoreboard(), server.getScoreboard().getTeam("hunters")
                );
            } else {
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(
                            serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("runners")
                    );
                }
            }
        }

        hunterCoords.clear();
        runnerCoords.clear();
        allReadyUps.clear();
        leftOnPause.clear();
        playerSpawnPos.clear();
        parkourTimer.clear();
        startedParkour.clear();
        finishedParkour.clear();

        worldSpawnPos = new BlockPos(0, 0, 0);

        try {
            FileUtils.deleteDirectory(gameDir.resolve("world/dimensions/manhunt/overworld").toFile());
            FileUtils.deleteDirectory(gameDir.resolve("world/dimensions/manhunt/the_nether").toFile());
            FileUtils.deleteDirectory(gameDir.resolve("world/dimensions/manhunt/the_end").toFile());
        } catch (IOException e) {
            LOGGER.error("Failed to delete dimension worlds");
        }

        loadManhuntWorlds(server, seed);
    }

    public static GameMode getGameMode() {
        if (state == GameState.PLAYING) {
            return GameMode.SURVIVAL;
        } else if (state == GameState.POSTGAME) {
            if (config.isSpectateOnWin()) {
                return GameMode.SPECTATOR;
            } else {
                return GameMode.SURVIVAL;
            }
        }

        return GameMode.ADVENTURE;
    }

    public static void setPlayerSpawn(ServerWorld world, ServerPlayerEntity player) {
        if (worldSpawnPos.equals(new BlockPos(0, 0, 0))) {
            worldSpawnPos = setupSpawn(world);
        }

        BlockPos blockPos = worldSpawnPos;
        long l;
        long m;
        int i = Math.max(0, world.getServer().getGameRules().get(GameRules.SPAWN_RADIUS).get());
        int j = MathHelper.floor(
                player.getServer().getOverworld().getWorldBorder().getDistanceInsideBorder(
                        blockPos.getX(), blockPos.getZ()
                )
        );
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
            playerSpawnPos.put(player.getUuid(), blockPos2);
            break;
        }
    }

    @Nullable
    private static BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
        int i;
        boolean bl = world.getDimension().hasCeiling();
        WorldChunk worldChunk = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        i = bl
                ?
                world.getChunkManager().getChunkGenerator().getSpawnHeight(world)
                :
                worldChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x & 0xF, z & 0xF);

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
        ChunkPos chunkPos = new ChunkPos(
                serverChunkManager.getNoiseConfig().getMultiNoiseSampler().findBestSpawnPosition()
        );
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
            if (j >= -5 && j <= 5 && k >= -5 && k <= 5 && (
                    blockPos = SpawnLocating.findServerSpawnPoint(world, new ChunkPos(chunkPos.x + j, chunkPos.z + k))
            ) != null) {
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