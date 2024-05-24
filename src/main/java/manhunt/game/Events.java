package manhunt.game;

import manhunt.ManhuntMod;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.mrnavastar.sqlib.DataContainer;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
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
import net.minecraft.world.border.WorldBorder;

import java.io.IOException;
import java.util.LinkedList;

import static manhunt.ManhuntMod.*;
import static manhunt.game.ManhuntGame.endGame;

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
            server.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
            server.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
        } else {
            server.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
            server.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
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

                    ItemStack preferencesItem = new ItemStack(Items.PLAYER_HEAD);
                    preferencesItem.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.preferences").setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)));
                    preferencesItem.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                    preferencesItem.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
                    preferencesItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                    player.getInventory().setStack(0, preferencesItem);
                } else {
                    int amount = 0;
                    for (ItemStack stack : player.getInventory().main) {
                        if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Preferences")) {
                            amount++;
                        }
                    }

                    if (amount > 1) {
                        player.getInventory().clear();
                    }
                }

                if (Permissions.check(player, "manhunt.settings") || (player.hasPermissionLevel(1) || player.hasPermissionLevel(2) || player.hasPermissionLevel(3) || player.hasPermissionLevel(4))) {
                    if (!hasItem(player, Items.COMMAND_BLOCK, "Settings")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);
                        nbt.putBoolean("Settings", true);

                        ItemStack settingsItem = new ItemStack(Items.COMMAND_BLOCK);
                        settingsItem.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("manhunt.settings").setStyle(Style.EMPTY.withColor(Formatting.WHITE).withItalic(false)));
                        settingsItem.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                        settingsItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                        player.getInventory().setStack(8, settingsItem);
                    } else {
                        int amount = 0;
                        for (ItemStack stack : player.getInventory().main) {
                            if (stack.get(DataComponentTypes.CUSTOM_DATA) != null && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Settings")) {
                                amount++;
                            }
                        }

                        if (amount > 1) {
                            player.getInventory().clear();
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
                                player.sendMessage(Text.translatable("manhunt.parkour.time", secSeconds, msSeconds), true);
                                parkourTimer.put(player.getUuid(), parkourTimer.get(player.getUuid()) + 1);
                                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 255, false, false, false));

                                if (player.getZ() < -24 && player.getZ() > -27 && player.getX() < -6 && player.getY() >= 70 && player.getY() < 72) {
                                    player.sendMessage(Text.translatable("manhunt.parkour.time", secSeconds, msSeconds).formatted(Formatting.GREEN), true);
                                    player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1f, 2f);
                                    finishedParkour.put(player.getUuid(), true);
                                }
                            }

                            if (player.getZ() > -4 || player.getY() < 61 || (player.getZ() < -27 && player.getY() < 68)) {
                                player.sendMessage(Text.translatable("manhunt.parkour.time", secSeconds, msSeconds).formatted(Formatting.RED), true);
                                resetPlayer(player, player.getServerWorld());
                                player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.MASTER, 1f, 0.5f);
                            }
                        }
                    }

                    if ((player.getX() < -24 || player.getX() > 24) || (player.getY() < 54 || player.getY() > 74) || (player.getZ() < -64 || player.getZ() > 32)) {
                        resetPlayer(player, player.getServerWorld());
                    }
                }
            }
        }

        if (ManhuntMod.getGameState() == GameState.PLAYING) {
            allRunners = new LinkedList<>();

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                    allRunners.add(player);
                }

                if (player.getWorld().getRegistryKey() == RegistryKey.of(RegistryKeys.WORLD, lobbyKey)) {
                    if (!playerSpawn.containsKey(player.getUuid())) {
                        ManhuntGame.setPlayerSpawn(overworld, player);
                    }

                    player.teleport(overworld, playerSpawn.get(player.getUuid()).getX(), playerSpawn.get(player.getUuid()).getY(), playerSpawn.get(player.getUuid()).getZ(), 0F, 0F);
                    player.setSpawnPoint(overworld.getRegistryKey(), playerSpawn.get(player.getUuid()), 0, true, false);
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
        }
    }

    public static void playerJoin(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;

        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            player.teleport(server.getWorld(RegistryKey.of(RegistryKeys.WORLD, lobbyKey)), 0.5, 63, 0.5, PositionFlag.ROT, 0, 0);
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

            if (player.getScoreboardTeam() == null) {
                player.getScoreboard().addScoreHolderToTeam(player.getNameForScoreboard(), player.getScoreboard().getTeam("hunters"));
            }

            ManhuntGame.setPlayerSpawn(overworld, player);

            WorldBorder worldBorder = overworld.getWorldBorder();

            overworld.getWorldBorder().interpolateSize(worldBorder.getSize(), config.getWorldBorder(), 0);
        }

        if (getGameState() != GameState.PREGAME && !hasPlayed.containsKey(player.getUuid())) {
            hasPlayed.put(player.getUuid(), true);

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
            } else if (getGameState() == GameState.POSTGAME && config.isSpectateWin()) {
                player.changeGameMode(GameMode.SPECTATOR);
            }

            player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));
        }

        if (!gameTitles.containsKey(player.getUuid())) {
            gameTitles.putIfAbsent(player.getUuid(), config.isGameTitlesDefault());
            manhuntSounds.putIfAbsent(player.getUuid(), config.isManhuntSoundsDefault());
            nightVision.putIfAbsent(player.getUuid(), false);
            friendlyFire.putIfAbsent(player.getUuid(), true);

            DataContainer dataContainer = ManhuntMod.getTable().get(player.getUuid());

            if (dataContainer != null) {
                gameTitles.put(player.getUuid(), dataContainer.getBool("game_titles"));
                manhuntSounds.put(player.getUuid(), dataContainer.getBool("manhunt_sounds"));
                nightVision.put(player.getUuid(), dataContainer.getBool("night_vision"));
                friendlyFire.put(player.getUuid(), dataContainer.getBool("friendly_fire"));
            }
        }
    }

    public static void playerLeave(ServerPlayNetworkHandler handler) {
        ServerPlayerEntity player = handler.getPlayer();

        DataContainer dataContainer = ManhuntMod.getTable().getOrCreateDataContainer(player.getUuid());

        dataContainer.put("game_titles", gameTitles.get(player.getUuid()));
        dataContainer.put("manhunt_sounds", manhuntSounds.get(player.getUuid()));
        dataContainer.put("night_vision", nightVision.get(player.getUuid()));
        dataContainer.put("friendly_fire", friendlyFire.get(player.getUuid()));
    }

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Preferences")) {
                ManhuntGame.openPreferencesGui((ServerPlayerEntity) player);
            }

            if (stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Settings")) {
                ManhuntGame.openSettingsGui((ServerPlayerEntity) player);
            }
        }

        if (getGameState() == GameState.PLAYING) {
            if (!config.isAutomaticCompass() && player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && allRunners != null && !allRunners.isEmpty() && !player.isSpectator() && !player.getItemCooldownManager().isCoolingDown(stack.getItem()) && stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getBoolean("Tracker")) {
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
                if (player.getWorld().getRegistryKey() != overworld.getRegistryKey() && stack.getName().toString().contains("_bed")) {
                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0F && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("manhunt.chat.disabled").formatted(Formatting.RED));
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }

            if (!config.isLavaPvpInNether()) {
                if (player.getWorld().getRegistryKey() == nether.getRegistryKey() && stack.getItem() == Items.LAVA_BUCKET) {
                    for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
                        if (player.distanceTo(serverPlayer) <= 9.0F && !player.isTeamPlayer(serverPlayer.getScoreboardTeam())) {
                            player.sendMessage(Text.translatable("manhunt.chat.disabled").formatted(Formatting.RED));
                            return TypedActionResult.fail(stack);
                        }
                    }
                }
            }
        }

        return TypedActionResult.pass(stack);
    }

    public static void playerRespawn(ServerPlayerEntity player) {
        Scoreboard scoreboard = player.getScoreboard();

        if (player.isTeamPlayer(player.getScoreboard().getTeam("runners")) && getGameState() != GameState.POSTGAME) {
            if (config.isRunnersHuntOnDeath()) {
                scoreboard.clearTeam(player.getNameForScoreboard());
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("hunters"));
            } else {
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
