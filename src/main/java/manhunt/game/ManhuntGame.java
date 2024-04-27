package manhunt.game;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.ManhuntMod;
import manhunt.mixin.ServerWorldInterface;
import manhunt.world.ManhuntWorldModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static manhunt.ManhuntMod.*;

public class ManhuntGame {
    public static void startGame(MinecraftServer server) {
        server.setFlightEnabled(true);

        setGameState(GameState.PLAYING);

        ServerWorld overWorld = server.getWorld(ManhuntMod.overworldKey);

        for (ServerWorld serverWorld : server.getWorlds()) {
            ((ServerWorldInterface) serverWorld).getWorldProperties().setTime(0);
            serverWorld.setTimeOfDay(0);
            serverWorld.resetWeather();
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
        server.getGameRules().get(GameRules.SPAWN_RADIUS).set(10, server);
        server.getGameRules().get(GameRules.FALL_DAMAGE).set(true, server);

        server.setPvpEnabled(true);

        if (config.isTeamColor()) {
            server.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
            server.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
        } else {
            server.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
            server.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
        }

        server.setDifficulty(config.getGameDifficulty(), true);

        overWorld.getWorldBorder().setSize(config.getWorldBorder());

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            setPlayerSpawn(server.getWorld(ManhuntMod.overworldKey), player);

            Stats.MINED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.CRAFTED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.USED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.BROKEN.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.PICKED_UP.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.DROPPED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.KILLED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.KILLED_BY.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.CUSTOM.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));

            double playerX = Double.parseDouble(String.valueOf(ManhuntMod.playerSpawn.get(player).getX()));
            double playerY = Double.parseDouble(String.valueOf(ManhuntMod.playerSpawn.get(player).getY()));
            double playerZ = Double.parseDouble(String.valueOf(ManhuntMod.playerSpawn.get(player).getZ()));
            player.teleport(overWorld, playerX, playerY, playerZ, 0, 0);
            player.setSpawnPoint(ManhuntMod.overworldKey, ManhuntMod.playerSpawn.get(player), 0, true, false);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);

            updateGameMode(player);

            if (player.isTeamPlayer(player.getScoreboard().getTeam("players"))) {
                player.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), player.getScoreboard().getTeam("players"));
            }

            if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                isRunner.put(player, true);
            }

            if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.getRunnerHeadstart() * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, config.getRunnerHeadstart() * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, config.getRunnerHeadstart() * 20, 248, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (config.getRunnerHeadstart() - 1) * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, config.getRunnerHeadstart() * 20, 255, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, config.getRunnerHeadstart() * 20, 255, false, false));
            }

            if (gameTitles.get(player)) {
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.manhunt")));
                player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.glhf").formatted(Formatting.GRAY)));
            }

            if (manhuntSounds.get(player)) {
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
            }

            if (nightVision.get(player)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 255, false, false));
            }
        }
    }

    public static void updateGameMode(ServerPlayerEntity player) {
        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            player.changeGameMode(GameMode.ADVENTURE);
        } else if (ManhuntMod.getGameState() == GameState.PLAYING) {
            player.changeGameMode(GameMode.SURVIVAL);
        } else {
            player.changeGameMode(GameMode.SPECTATOR);
        }
    }

    private void cycleTrackedPlayer(ServerPlayerEntity player, @Nullable NbtCompound stackNbt) {
        if (stackNbt != null && stackNbt.getBoolean("Tracker") && player.isTeamPlayer(player.getServer().getScoreboard().getTeam("hunters"))) {

            if (!stackNbt.contains("Info")) {
                stackNbt.put("Info", new NbtCompound());
            }

            int next;
            int previous = -1;
            NbtCompound info = stackNbt.getCompound("Info");

            if (getAllRunners().isEmpty())
                player.sendMessage(Text.translatable("manhunt.item.tracker.norunners"));
            else {
                for (int i = 0; i < getAllRunners().size(); i++) {
                    ServerPlayerEntity serverPlayer = getAllRunners().get(i);
                    if (serverPlayer != null) {
                        if (Objects.equals(serverPlayer.getName().getString(), info.getString("Name"))) {
                            previous = i;
                        }
                    }
                }

                if (previous + 1 >= getAllRunners().size()) {
                    next = 0;
                } else {
                    next = previous + 1;
                }

                if (previous != next) {
                    updateCompass(player, stackNbt, getAllRunners().get(next));
                    player.sendMessage(Text.translatable("manhunt.item.tracker.switchrunner", getAllRunners().get(next).getName().getString()));
                }
            }
        }
    }

    public static void updateCompass(ServerPlayerEntity player, NbtCompound nbt, ServerPlayerEntity trackedPlayer) {
        nbt.remove("LodestonePos");
        nbt.remove("LodestoneDimension");

        nbt.put("Info", new NbtCompound());
        if (trackedPlayer.getScoreboardTeam() != null && trackedPlayer.isTeamPlayer(trackedPlayer.getScoreboard().getTeam("runners"))) {
            NbtCompound playerTag = trackedPlayer.writeNbt(new NbtCompound());
            NbtList positions = playerTag.getList("Positions", 10);
            int i;
            for (i = 0; i < positions.size(); ++i) {
                NbtCompound compound = positions.getCompound(i);
                if (Objects.equals(compound.getString("LodestoneDimension"), player.writeNbt(new NbtCompound()).getString("Dimension"))) {
                    nbt.copyFrom(compound);
                    break;
                }
            }

            NbtCompound info = nbt.getCompound("Info");
            info.putLong("LastUpdateTime", player.getWorld().getTime());
            info.putString("Name", trackedPlayer.getName().getString());
            info.putString("Dimension", playerTag.getString("Dimension"));
        }
    }

    public static void resetGame(MinecraftServer server) {
        setGameState(GameState.PREGAME);

        if (getAllRunners() == null) return;

        if (!getAllRunners().isEmpty()) {
            for (ServerPlayerEntity player : getAllRunners()) {
                isRunner.put(player, true);
            }
        }

        new ManhuntWorldModule().resetWorlds(server);
    }

    public static void setPlayerSpawn(ServerWorld world, ServerPlayerEntity player) {
        if (getWorldSpawnPos().equals(new BlockPos(0, 0, 0))) {
            setWorldSpawnPos(setupSpawn(world));
        }
        BlockPos blockPos = getWorldSpawnPos();
        long l;
        long m;
        int i = Math.max(0, world.getServer().getSpawnRadius(world));
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
            playerSpawn.put(player, blockPos2);
            break;
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
        Item item;
        boolean bool;

        loreList = new ArrayList<>();
        name = "gametitles";
        item = Items.OAK_SIGN;
        bool = gameTitles.get(player);

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).formatted(Formatting.GRAY));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).formatted(Formatting.GRAY));
        }

        Item finalItem = item;
        boolean finalBool = bool;
        preferencesGui.setSlot(0, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(finalItem)) {
                        gameTitles.put(player, !finalBool);
                        player.getItemCooldownManager().set(finalItem, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPreferencesGui(player);
                    }
                })
        );

        loreList = new ArrayList<>();
        name = "manhuntsounds";
        item = Items.FIRE_CHARGE;
        bool = manhuntSounds.get(player);

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).formatted(Formatting.GRAY));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).formatted(Formatting.GRAY));
        }

        Item finalItem1 = item;
        boolean finalBool1 = bool;
        preferencesGui.setSlot(1, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(finalItem1)) {
                        manhuntSounds.put(player, !finalBool1);
                        player.getItemCooldownManager().set(finalItem1, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openPreferencesGui(player);
                    }
                })
        );

        loreList = new ArrayList<>();
        name = "nightvision";
        item = Items.SPYGLASS;
        bool = nightVision.get(player);

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).formatted(Formatting.GRAY));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).formatted(Formatting.GRAY));
        }

        Item finalItem2 = item;
        boolean finalBool2 = bool;
        preferencesGui.setSlot(2, new GuiElementBuilder(item)
            .setName(Text.translatable("manhunt." + name))
            .setLore(loreList)
            .setCallback(() -> {
                if (!player.getItemCooldownManager().isCoolingDown(finalItem2)) {
                    nightVision.put(player, !finalBool2);
                    player.getItemCooldownManager().set(finalItem2, 10);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                    openPreferencesGui(player);
                }
            })
        );

        preferencesGui.open();
    }

    public static void openSettingsGui(ServerPlayerEntity player) {
        SimpleGui settingsGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

        settingsGui.setTitle(Text.translatable("manhunt.settings"));

        List<Text> loreList;
        String name;
        Item item;
        boolean bool;
        int integer;

        loreList = new ArrayList<>();
        name = "trackercompass";
        item = Items.SPYGLASS;
        bool = config.isTrackerCompass();

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.trackercompass.automatic").formatted(Formatting.GREEN), Text.translatable("manhunt.trackercompass.manual")).formatted(Formatting.GRAY));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.trackercompass.automatic"), Text.translatable("manhunt.trackercompass.manual").formatted(Formatting.RED)).formatted(Formatting.GRAY));
        }

        var finalItem1 = item;
        var finalBool1 = bool;
        settingsGui.setSlot(0, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(finalItem1)) {
                        config.setTrackerCompass(!finalBool1);
                        config.save();
                        player.getItemCooldownManager().set(finalItem1, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );

        loreList = new ArrayList<>();
        name = "teamcolor";
        item = Items.WHITE_BANNER;
        bool = config.isTeamColor();

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (bool) {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on").formatted(Formatting.GREEN), Text.translatable("manhunt.off")).formatted(Formatting.GRAY));
        } else {
            loreList.add(Text.translatable("manhunt.lore.double", Text.translatable("manhunt.on"), Text.translatable("manhunt.off").formatted(Formatting.RED)).formatted(Formatting.GRAY));
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").formatted(Formatting.GOLD));

        var finalItem2 = item;
        var finalBool2 = bool;
        settingsGui.setSlot(1, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .setCallback((clickType) -> openTeamColorGui(player, clickType, finalItem2, finalBool2))
        );

        loreList = new ArrayList<>();
        name = "runnerheadstart";
        item = Items.GOLDEN_BOOTS;
        integer = config.getRunnerHeadstart();

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getRunnerHeadstart())).formatted(Formatting.RED)).formatted(Formatting.GRAY));
        } else if (integer != 10 && integer != 20 && integer != 30) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getRunnerHeadstart())).formatted(Formatting.GREEN)).formatted(Formatting.GRAY));
        } else {
            if (integer == 10) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("10").formatted(Formatting.RED), Text.literal("20"), Text.literal("30")).formatted(Formatting.GRAY));
            } else if (integer == 20) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("10"), Text.literal("20").formatted(Formatting.YELLOW), Text.literal("30")).formatted(Formatting.GRAY));
            } else {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("10"), Text.literal("20"), Text.literal("30").formatted(Formatting.GREEN)).formatted(Formatting.GRAY));
            }
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").formatted(Formatting.GOLD));

        Item finalItem3 = item;
        int finalInteger = integer;
        settingsGui.setSlot(2, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .hideFlags()
                .setCallback((clickType) -> {
                    if (!player.getItemCooldownManager().isCoolingDown(finalItem3)) {
                        if (!clickType.shift) {
                            if (finalInteger != 10 && finalInteger != 20) {
                                config.setRunnerHeadstart(10);
                            } else {
                                if (finalInteger == 10) {
                                    config.setRunnerHeadstart(20);
                                } else {
                                    config.setRunnerHeadstart(30);
                                }
                            }
                            config.save();
                            player.getItemCooldownManager().set(finalItem3, 10);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openSettingsGui(player);
                        } else {
                            AnvilInputGui runnerHeadstartGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                            .setCallback(() -> {
                                                int value = finalInteger;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                }

                                                config.setRunnerHeadstart(value);
                                                config.save();
                                                player.getItemCooldownManager().set(finalItem3, 10);
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

        loreList = new ArrayList<>();
        name = "timelimit";
        item = Items.CLOCK;
        integer = config.getTimeLimit();

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getTimeLimit())).formatted(Formatting.RED)).formatted(Formatting.GRAY));
        } else if (integer != 30 && integer != 60 && integer != 90) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getTimeLimit())).formatted(Formatting.GREEN)).formatted(Formatting.GRAY));
        } else {
            if (integer == 30) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("30").formatted(Formatting.RED), Text.literal("60"), Text.literal("90")).formatted(Formatting.GRAY));
            } else if (integer == 60) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("30"), Text.literal("60").formatted(Formatting.YELLOW), Text.literal("90")).formatted(Formatting.GRAY));
            } else {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("30"), Text.literal("60"), Text.literal("90").formatted(Formatting.GREEN)).formatted(Formatting.GRAY));
            }
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").formatted(Formatting.GOLD));

        Item finalItem4 = item;
        int finalInteger1 = integer;
        settingsGui.setSlot(3, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .hideFlags()
                .setCallback((clickType) -> {
                    if (!player.getItemCooldownManager().isCoolingDown(finalItem4)) {
                        if (!clickType.shift) {
                            if (finalInteger1 != 30 && finalInteger1 != 60) {
                                config.setTimeLimit(30);
                            } else {
                                if (finalInteger1 == 30) {
                                    config.setTimeLimit(60);
                                } else {
                                    config.setTimeLimit(90);
                                }
                            }
                            config.save();
                            player.getItemCooldownManager().set(finalItem4, 10);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openSettingsGui(player);
                        } else {
                            AnvilInputGui timeLimitGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                            .setCallback(() -> {
                                                int value = finalInteger1;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                }

                                                config.setTimeLimit(value);
                                                config.save();
                                                player.getItemCooldownManager().set(finalItem4, 10);
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

        loreList = new ArrayList<>();
        name = "gamedifficulty";
        item = Items.CREEPER_HEAD;
        Difficulty difficulty = config.getGameDifficulty();

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (difficulty == Difficulty.EASY) {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("options.difficulty.easy").formatted(Formatting.GREEN), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard")).formatted(Formatting.GRAY));
        } else if (difficulty == Difficulty.NORMAL) {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal").formatted(Formatting.YELLOW), Text.translatable("options.difficulty.hard")).formatted(Formatting.GRAY));
        } else {
            loreList.add(Text.translatable("manhunt.lore.triple", Text.translatable("options.difficulty.easy"), Text.translatable("options.difficulty.normal"), Text.translatable("options.difficulty.hard").formatted(Formatting.RED)).formatted(Formatting.GRAY));
        }

        Item finalItem5 = item;
        settingsGui.setSlot(4, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .hideFlags()
                .setCallback(() -> {
                    if (!player.getItemCooldownManager().isCoolingDown(finalItem5)) {
                        if (difficulty == Difficulty.EASY) {
                            config.setGameDifficulty(Difficulty.NORMAL);
                        } else if (difficulty == Difficulty.NORMAL) {
                            config.setGameDifficulty(Difficulty.HARD);
                        } else {
                            config.setGameDifficulty(Difficulty.EASY);
                        }
                        player.getItemCooldownManager().set(finalItem5, 10);
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                        openSettingsGui(player);
                    }
                })
        );

        loreList = new ArrayList<>();
        name = "worldborder";
        item = Items.GRASS_BLOCK;
        integer = config.getWorldBorder();

        loreList.add(Text.translatable("manhunt.lore." + name).formatted(Formatting.GRAY));
        if (integer == 0) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getWorldBorder())).formatted(Formatting.RED)).formatted(Formatting.GRAY));
        } else if (integer != 2816 && integer != 5888 && integer != 59999968) {
            loreList.add(Text.translatable("manhunt.lore.single", Text.literal(String.valueOf(config.getWorldBorder())).formatted(Formatting.GREEN)).formatted(Formatting.GRAY));
        } else {
            if (integer == 2816) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("1st ring").formatted(Formatting.RED), Text.literal("2nd ring"), Text.literal("Maximum")).formatted(Formatting.GRAY));
            } else if (integer == 5888) {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("1st ring"), Text.literal("2nd ring").formatted(Formatting.YELLOW), Text.literal("Maximum")).formatted(Formatting.GRAY));
            } else {
                loreList.add(Text.translatable("manhunt.lore.triple", Text.literal("1st ring"), Text.literal("2nd ring"), Text.literal("Maximum").formatted(Formatting.GREEN)).formatted(Formatting.GRAY));
            }
        }
        loreList.add(Text.translatable("manhunt.lore.click.shift").formatted(Formatting.GOLD));

        Item finalItem6 = item;
        int finalInteger2 = integer;
        settingsGui.setSlot(5, new GuiElementBuilder(item)
                .setName(Text.translatable("manhunt." + name))
                .setLore(loreList)
                .hideFlags()
                .setCallback((clickType) -> {
                    if (!player.getItemCooldownManager().isCoolingDown(finalItem6)) {
                        if (!clickType.shift) {
                            if (finalInteger2 != 2816 && finalInteger2 != 5888) {
                                config.setWorldBorder(2816);
                            } else {
                                if (finalInteger2 == 2816) {
                                    config.setWorldBorder(5888);
                                } else {
                                    config.setWorldBorder(59999968);
                                }
                            }
                            config.save();
                            player.getItemCooldownManager().set(finalItem6, 10);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 1.0F, player.getWorld().random.nextLong()));
                            openSettingsGui(player);
                        } else {
                            AnvilInputGui worldBorderGui = new AnvilInputGui(player, false) {
                                @Override
                                public void onInput(String input) {
                                    this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                            .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                            .setCallback(() -> {
                                                int value = finalInteger2;
                                                try {
                                                    value = Integer.parseInt(input);
                                                } catch (NumberFormatException e) {
                                                    player.sendMessage(Text.translatable("manhunt.invalidinput").formatted(Formatting.RED));
                                                }

                                                config.setWorldBorder(value);
                                                config.save();
                                                player.getItemCooldownManager().set(finalItem6, 10);
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

        settingsGui.open();
    }

    private static void openTeamColorGui(ServerPlayerEntity player, ClickType clickType, Item item, Boolean bool) {
        if (!player.getItemCooldownManager().isCoolingDown(item)) {
            if (clickType.shift) {
                var teamColorGui = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);

                teamColorGui.setSlot(3, new GuiElementBuilder(Items.RECOVERY_COMPASS)
                        .setName(Text.translatable("manhunt.hunterscolor").formatted(config.getHuntersColor()))
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
                                    .setName(Text.translatable("manhunt.goback"))
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

                teamColorGui.setSlot(5, new GuiElementBuilder(Items.BLAZE_POWDER)
                        .setName(Text.translatable("manhunt.runnerscolor").formatted(config.getRunnersColor()))
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
                                    .setName(Text.translatable("manhunt.goback"))
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
                        .setName(Text.translatable("manhunt.goback"))
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