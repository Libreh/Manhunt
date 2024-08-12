package manhunt.game;

import manhunt.ManhuntMod;
import manhunt.command.CoordsCommand;
import manhunt.command.DurationCommand;
import manhunt.command.PauseCommand;
import manhunt.config.ManhuntConfig;
import manhunt.mixin.IServerWorld;
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
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;
import org.popcraft.chunky.ChunkyProvider;

import java.util.*;

public class ManhuntGame {
    public static boolean chunkyLoaded = false;
    public static BlockPos worldSpawnPos;
    public static final List<UUID> playList = new ArrayList<>();

    public static void start(MinecraftServer server) {
        server.setFlightEnabled(true);

        ManhuntMod.gameState = GameState.PLAYING;

        GameEvents.count = 0;

        if (ManhuntConfig.config.isSetMotd()) {
            server.setMotd(ManhuntMod.gameState.getColor() + "[" + ManhuntMod.gameState.getMotd() + "]§f Minecraft MANHUNT");
        }
        for (ServerWorld serverWorld : server.getWorlds()) {
            ((IServerWorld) serverWorld).getWorldProperties().setTime(0);
            serverWorld.setTimeOfDay(0);
            serverWorld.resetWeather();
        }

        var gameRules = server.getGameRules();
        gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
        gameRules.get(GameRules.DO_FIRE_TICK).set(true, server);
        gameRules.get(GameRules.DO_INSOMNIA).set(true, server);
        gameRules.get(GameRules.DO_MOB_LOOT).set(true, server);
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(true, server);
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(true, server);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(3, server);
        gameRules.get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
        gameRules.get(GameRules.FALL_DAMAGE).set(true, server);
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(false, server);

        if (ManhuntConfig.config.isVanilla()) {
            gameRules.get(GameRules.SPAWN_RADIUS).set(ManhuntConfig.config.getSpawnRadius(), server);
            gameRules.get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(ManhuntConfig.config.isSpectatorsGenerateChunks(), server);
            server.setDifficulty(ManhuntConfig.config.getDifficulty(), true);
            var worldBorder = server.getOverworld().getWorldBorder();
            worldBorder.setCenter(0, 0);
            worldBorder.setSize(ManhuntConfig.config.getWorldBorder());
        }

        if (chunkyLoaded && ManhuntConfig.config.isChunky()) {
            var chunky = ChunkyProvider.get().getApi();

            chunky.cancelTask("manhunt:overworld");
            chunky.cancelTask("manhunt:the_nether");
            chunky.cancelTask("manhunt:the_end");
        }

        var scoreboard = server.getScoreboard();
        var huntersTeam = scoreboard.getTeam("hunters");
        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        huntersTeam.setShowFriendlyInvisibles(false);
        var runnersTeam = scoreboard.getTeam("runners");
        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);
        runnersTeam.setShowFriendlyInvisibles(false);

        if (ManhuntConfig.config.isTeamColor()) {
            huntersTeam.setColor(ManhuntConfig.config.getHuntersColor());
            runnersTeam.setColor(ManhuntConfig.config.getRunnersColor());
        } else {
            Formatting reset = Formatting.RESET;
            ManhuntConfig.config.setHuntersColor(reset);
            ManhuntConfig.config.setRunnersColor(reset);
            huntersTeam.setColor(reset);
            huntersTeam.setColor(reset);
        }

        if (ManhuntConfig.config.getTimeLimit() != 0) {
            GameEvents.timeLimit = true;
            GameEvents.timeLimitLeft = ManhuntConfig.config.getTimeLimit() * 60 * 20;
        }

        if (ManhuntConfig.config.getRunnerHeadStart() != 0) {
            GameEvents.headStart = true;
            int headStart = ManhuntConfig.config.getRunnerHeadStart();
            if (headStart == 1) {
                headStart = 0;
            }
            GameEvents.headStartTime = (headStart * 20) + 20;
        }

        if (ManhuntConfig.config.isRunnerPreferences() && runnersTeam.getPlayerList().size() == 1) {
            String playerName = runnersTeam.getPlayerList().iterator().next();

            ManhuntConfig.config.setBedExplosions(ManhuntSettings.bedExplosions.get(server.getPlayerManager().getPlayer(playerName).getUuid()));
            ManhuntConfig.config.setLavaPvpInNether(ManhuntSettings.lavaPvpInNether.get(server.getPlayerManager().getPlayer(playerName).getUuid()));
        }

        PauseCommand.playerEffects.clear();
        PauseCommand.playerPos.clear();
        PauseCommand.playerYaw.clear();
        PauseCommand.playerPitch.clear();
        GameEvents.playerFood.clear();
        GameEvents.playerSaturation.clear();

        if (ManhuntConfig.config.isRunnersGlow()) {
            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                if (serverPlayer.isTeamPlayer(runnersTeam))
                    serverPlayer.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.GLOWING,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false)
                    );
            }
        }

        if (GameEvents.headStart) {
            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                if (serverPlayer.isTeamPlayer(huntersTeam)) {
                    serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                    serverPlayer.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(0);
                    serverPlayer.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(0);
                    serverPlayer.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.DARKNESS,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false,
                            false)
                    );
                    serverPlayer.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.RESISTANCE,
                            StatusEffectInstance.INFINITE,
                            255,
                            false,
                            false,
                            false)
                    );
                }
            }
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            GameEvents.startList.add(player.getUuid());
            playList.add(player.getUuid());
        }

        GameEvents.allReadyUps.clear();
        GameEvents.parkourTimer.clear();
        GameEvents.startedParkour.clear();
        GameEvents.finishedParkour.clear();
    }

    public static void end(MinecraftServer server, boolean hunterWin) {
        ManhuntMod.gameState = GameState.POSTGAME;

        if (ManhuntConfig.config.isSetMotd()) {
            server.setMotd(ManhuntMod.gameState.getColor() + "[" + ManhuntMod.gameState.getMotd() + "]§f Minecraft MANHUNT");
        }

        server.getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(true, server);

        var playerManager = server.getPlayerManager();

        playerManager.broadcast(Text.translatable("commands.seed.success",
                Texts.bracketedCopyable(String.valueOf(ManhuntMod.overworld.getSeed())).formatted(Formatting.GREEN)), false
        );
        DurationCommand.setDuration(server);
        playerManager.broadcast(Text.translatable("chat.manhunt.duration",
                Texts.bracketedCopyable(DurationCommand.duration).formatted(Formatting.GREEN)), false
        );

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.changeGameMode(getGameMode());

            if (ManhuntConfig.config.isCustomTitles() && ManhuntSettings.customTitles.get(player.getUuid())) {
                if (hunterWin) {
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                            Text.translatable("title.manhunt.hunters_won").formatted(ManhuntConfig.config.getHuntersColor()))
                    );
                } else {
                    player.networkHandler.sendPacket(new TitleS2CPacket(
                            Text.translatable("title.manhunt.runners_won").formatted(ManhuntConfig.config.getRunnersColor())))
                    ;
                }
                player.networkHandler.sendPacket(new SubtitleS2CPacket(
                        Text.translatable("title.manhunt.gg").formatted(Formatting.AQUA))
                );
            }

            if (ManhuntConfig.config.isCustomSounds() && ManhuntSettings.customSounds.get(player.getUuid())) {
                player.playSoundToPlayer(
                        SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE.value(),
                        SoundCategory.MASTER,
                       1.0F,
                        0.5F
                );
            }

            if (ManhuntConfig.config.isCustomParticles() && ManhuntSettings.customParticles.get(player.getUuid())) {
                SimpleParticleType particle = ParticleTypes.TRIAL_SPAWNER_DETECTION;
                if (!hunterWin) {
                    particle = ParticleTypes.TRIAL_SPAWNER_DETECTION_OMINOUS;
                }
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    player.getServerWorld().spawnParticles(
                            serverPlayer, particle, true,
                            player.getX(), player.getY() - 0.5, player.getZ(),
                            20,0.2, 0.4, 0.2, 0.01
                    );
                }
            }
        }
    }

    public static void reset(MinecraftServer server) {
        ManhuntMod.gameState = GameState.PREGAME;

        if (ManhuntConfig.config.isSetMotd()) {
            server.setMotd(ManhuntMod.gameState.getColor() + "[" + ManhuntMod.gameState.getMotd() + "]§f Minecraft MANHUNT");
        }

        var gameRules = server.getWorld(ManhuntMod.lobbyWorldRegistryKey).getGameRules();

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
        gameRules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);

        var scoreboard = server.getScoreboard();
        var huntersTeam = scoreboard.getTeam("hunters");
        huntersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        huntersTeam.setShowFriendlyInvisibles(true);
        var runnersTeam = scoreboard.getTeam("runners");
        runnersTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
        runnersTeam.setShowFriendlyInvisibles(true);

        if (ManhuntConfig.config.isTeamColor()) {
            huntersTeam.setColor(ManhuntConfig.config.getHuntersColor());
            runnersTeam.setColor(ManhuntConfig.config.getRunnersColor());
        } else {
            huntersTeam.setColor(Formatting.RESET);
            runnersTeam.setColor(Formatting.RESET);
        }

        var worldBorder = server.getOverworld().getWorldBorder();

        worldBorder.setCenter(0, 0);
        worldBorder.setSize(59999968);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            GameEvents.resetList.add(player.getUuid());
        }

        if (ManhuntConfig.config.getRolePreset() != 1 && ManhuntConfig.config.getRolePreset() != 5) {
            if (ManhuntConfig.config.getRolePreset() == 2) {
                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(
                            serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("runners")
                    );
                }
            } else if (ManhuntConfig.config.getRolePreset() == 3) {
                if (ManhuntSettings.playerList == null || ManhuntSettings.playerList.isEmpty()) {
                    ManhuntSettings.playerList = new ArrayList<>(server.getPlayerManager().getPlayerList());
                }

                ManhuntSettings.playerList.removeIf(ServerPlayerEntity::isDisconnected);

                ServerPlayerEntity runner = ManhuntSettings.playerList.getFirst();

                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(
                            serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("hunters")
                    );
                }

                server.getScoreboard().addScoreHolderToTeam(
                        runner.getNameForScoreboard(), server.getScoreboard().getTeam("runners")
                );
                ManhuntSettings.playerList.remove(runner);
            } else {
                List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());

                Collections.shuffle(players);

                ServerPlayerEntity hunter = players.getFirst();

                for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                    serverPlayer.getScoreboard().addScoreHolderToTeam(
                            serverPlayer.getNameForScoreboard(), server.getScoreboard().getTeam("runners")
                    );
                }

                server.getScoreboard().addScoreHolderToTeam(
                        hunter.getNameForScoreboard(), server.getScoreboard().getTeam("hunters")
                );
            }
        }

        CoordsCommand.hunterCoords.clear();
        CoordsCommand.runnerCoords.clear();
        GameEvents.leftOnPause.clear();
        GameEvents.playerSpawnPos.clear();
        GameEvents.headStartPos.clear();
        playList.clear();
        DurationCommand.duration = "";
        GameEvents.timeLimit = false;
        GameEvents.headStart = false;
        GameEvents.headStartTime = 0;
        GameEvents.paused = false;
        GameEvents.startReset = false;
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

        if (is != null) {
            if (is.length >= 2) {
                BlockPos blockPos = new BlockPos(is[0], is[1], is[2]);

                stack.set(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(
                        Optional.of(
                                GlobalPos.create(player.getWorld().getRegistryKey(), blockPos)
                        ), false)
                );
            }
        }
    }

    public static GameMode getGameMode() {
        if (ManhuntMod.gameState == GameState.PLAYING) {
            return GameMode.SURVIVAL;
        } else if (ManhuntMod.gameState == GameState.POSTGAME) {
            if (ManhuntConfig.config.isSpectateOnWin()) {
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
        var blockPos = worldSpawnPos;
        var worldBorder = world.getWorldBorder();
        if (!worldBorder.contains(blockPos)) {
            blockPos = world.getTopPosition(
                    Heightmap.Type.MOTION_BLOCKING, BlockPos.ofFloored(worldBorder.getCenterX(), 0.0, worldBorder.getCenterZ())
            );
        }
        long l;
        long m;
        int spawnRadius = 0;
        if (ManhuntConfig.config.isVanilla() && ManhuntConfig.config.getSpawnRadius() != 0) {
            spawnRadius = ManhuntConfig.config.getSpawnRadius();
        }
        int i = Math.max(0, spawnRadius);
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
            var blockPos2 = findOverworldSpawn(world, blockPos.getX() + r - i, blockPos.getZ() + s - i);
            if (blockPos2 == null) continue;
            GameEvents.playerSpawnPos.put(player.getUuid(), blockPos2);
            break;
        }
    }

    @Nullable
    private static BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
        int i;
        boolean bl = world.getDimension().hasCeiling();
        var worldChunk = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
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
        var serverChunkManager = world.getChunkManager();
        var chunkPos = new ChunkPos(
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