package manhunt.game;

import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.command.*;
import manhunt.config.Configs;
import manhunt.config.model.ConfigModel;
import manhunt.mixin.MinecraftServerAccessInterface;
import manhunt.mixin.ServerWorldInterface;
import manhunt.util.MessageUtil;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.*;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

import static manhunt.Manhunt.*;
import static manhunt.game.ManhuntState.*;

public class ManhuntGame {
    public static final Identifier LOBBY_WORLD_ID = new Identifier(MOD_ID, "lobby");
    public static final Identifier OVERWORLD_ID = new Identifier("manhunt", "overworld");
    public static final Identifier THE_NETHER_ID = new Identifier("manhunt", "the_nether");
    public static final Identifier THE_END_ID = new Identifier("manhunt", "the_end");
    public static RegistryKey<World> lobbyRegistryKey = RegistryKey.of(RegistryKeys.WORLD, LOBBY_WORLD_ID);
    public static RegistryKey<World> overworldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, OVERWORLD_ID);
    public static RegistryKey<World> theNetherRegistryKey = RegistryKey.of(RegistryKeys.WORLD, THE_NETHER_ID);
    public static RegistryKey<World> theEndRegistryKey = RegistryKey.of(RegistryKeys.WORLD, THE_END_ID);
    public static final ConfigModel.Settings settings = Configs.configHandler.model().settings;
    public static List<ServerPlayerEntity> allRunners;
    public static HashMap<UUID, Boolean> isReady = new HashMap<>();
    private static boolean paused;
    public static boolean isPaused() {
        return paused;
    }
    public static void setPaused(boolean paused) {
        ManhuntGame.paused = paused;
    }
    public final MinecraftServerAccessInterface serverAccessMixin;
    public static BlockPos worldSpawnPos;
    public static Identifier isRunner = new Identifier("manhunt_data_player", "is_runner");
    public static Identifier playerSpawnX = new Identifier("manhunt_data_player", "spawnx");
    public static Identifier playerSpawnY = new Identifier("manhunt_data_player", "spawny");
    public static Identifier playerSpawnZ = new Identifier("manhunt_data_player", "spawnz");
    public static Identifier winnerTitlePreference = new Identifier("manhunt_data_preferences", "winnertitle");
    public static Identifier manhuntSoundsVolumePreference = new Identifier("manhunt_data_preferences", "manhuntsoundsvolume");
    public static Identifier settingsAtStartPreference = new Identifier("manhunt_data_preferences", "settingsatstart");
    public static Identifier durationAtEndPreference = new Identifier("manhunt_data_preferences", "durationatend");
    public static Identifier bedExplosionDamagePreference = new Identifier("manhunt_data_preferences", "bedexplosiondamage");
    public static Identifier lavaPvpInTheNetherPreference = new Identifier("manhunt_data_preferences", "lavapvpinthenether");

    public ManhuntGame(MinecraftServerAccessInterface serverAccessMixin) {
        this.serverAccessMixin = serverAccessMixin;
    }

    public static void commandRegister(CommandDispatcher<ServerCommandSource> dispatcher) {
        PingCommand.register(dispatcher);
        HunterCommand.register(dispatcher);
        RunnerCommand.register(dispatcher);
        OneRunnerCommand.register(dispatcher);
        StartCommand.register(dispatcher);
        SettingsCommand.register(dispatcher);
        DurationCommand.register(dispatcher);
        TogglePauseCommand.register(dispatcher);
        SendTeamCoordsCommand.register(dispatcher);
        ShowTeamCoordsCommand.register(dispatcher);
        ResetCommand.register(dispatcher);
    }

    // Thanks to https://gitlab.com/horrific-tweaks/bingo for the spawnStructure method

    private static void spawnStructure(MinecraftServer server) throws IOException {
        var lobbyIcebergNbt = NbtIo.readCompressed(ManhuntGame.class.getResourceAsStream("/manhunt/lobby/iceberg.nbt"), NbtSizeTracker.ofUnlimitedBytes());

        var lobbyWorld = server.getWorld(lobbyRegistryKey);

        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 0), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, 0), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, -15), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, -15), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 16), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 0), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 16), 16, Unit.INSTANCE);
        placeStructure(lobbyWorld, new BlockPos(-8, 41, -8), lobbyIcebergNbt);
    }

    // Thanks to https://gitlab.com/horrific-tweaks/bingo for the placeStructure method

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

    public static void serverStart(MinecraftServer server) {
        try {
            FileUtils.deleteDirectory(CONFIG_PATH.resolve("lang").toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MessageUtil.copyLanguageFiles();

        settings.worldSeed = RandomSeed.getSeed();
        Configs.configHandler.saveToDisk();

        new ManhuntWorldModule().loadWorlds(server);

        setPaused(false);

        manhuntState(PREGAME, server);

        var difficulty = switch (settings.gameDifficulty) {
            case 2 -> Difficulty.NORMAL;
            case 3 -> Difficulty.HARD;
            default -> Difficulty.EASY;
        };

        server.setDifficulty(difficulty, true);

        var world = server.getWorld(lobbyRegistryKey);

        world.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        world.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        world.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
        world.getGameRules().get(GameRules.DO_MOB_LOOT).set(false, server);
        world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        world.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        world.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);
        world.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(0, server);
        world.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        world.getGameRules().get(GameRules.SPAWN_RADIUS).set(0, server);
        world.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);

        server.setPvpEnabled(false);

        Scoreboard scoreboard = server.getScoreboard();

        for (Team team : scoreboard.getTeams()) {
            if (team.getName().equals("manhunters")) {
                scoreboard.removeTeam(scoreboard.getTeam("manhunters"));
            }
            if (team.getName().equals("hunters")) {
                scoreboard.removeTeam(scoreboard.getTeam("hunters"));
            }
            if (team.getName().equals("runners")) {
                scoreboard.removeTeam(scoreboard.getTeam("runners"));
            }
        }

        scoreboard.addTeam("manhunters");
        scoreboard.getTeam("manhunters").setCollisionRule(AbstractTeam.CollisionRule.NEVER);

        scoreboard.addTeam("hunters");
        scoreboard.addTeam("runners");
        scoreboard.getTeam("hunters").setColor(Formatting.RED);
        scoreboard.getTeam("runners").setColor(Formatting.GREEN);

        try {
            spawnStructure(server);
        } catch (IOException e) {
            LOGGER.fatal("Failed to spawn Manhunt mod lobby");
        }
    }

    public static void serverTick(MinecraftServer server) {
        if (gameState == PREGAME) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (doesNotHaveItem(player, Items.RED_CONCRETE, "NotReady") && doesNotHaveItem(player, Items.GREEN_CONCRETE, "Ready") && settings.setRoles == 1) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Unready\",\"italic\": false,\"color\": \"red\"}");

                    ItemStack notReady = new ItemStack(Items.RED_CONCRETE);
                    notReady.setNbt(nbt);

                    player.getInventory().setStack(0, notReady);
                }

                if (doesNotHaveItem(player, Items.RECOVERY_COMPASS, "Hunter") && settings.setRoles == 1) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Hunter", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Hunter\",\"italic\": false,\"color\": \"aqua\"}");

                    ItemStack chooseHunter = new ItemStack(Items.RECOVERY_COMPASS);
                    chooseHunter.setNbt(nbt);

                    player.getInventory().setStack(3, chooseHunter);
                }

                if (doesNotHaveItem(player, Items.CLOCK, "Runner") && settings.setRoles == 1) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Runner", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Runner\",\"italic\": false,\"color\": \"gold\"}");

                    ItemStack chooseRunner = new ItemStack(Items.CLOCK);
                    chooseRunner.setNbt(nbt);

                    player.getInventory().setStack(5, chooseRunner);
                }

                if (player.hasPermissionLevel(1) || player.hasPermissionLevel(2) || player.hasPermissionLevel(3) || player.hasPermissionLevel(4)) {
                    if (doesNotHaveItem(player, Items.COMPARATOR, "Settings")) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);
                        nbt.putBoolean("Settings", true);
                        nbt.putInt("HideFlags", 1);
                        nbt.put("display", new NbtCompound());
                        nbt.getCompound("display").putString("Name", "{\"translate\": \"Settings\",\"italic\": false,\"color\": \"white\"}");

                        ItemStack showSettings = new ItemStack(Items.COMPARATOR);
                        showSettings.setNbt(nbt);

                        player.getInventory().setStack(7, showSettings);
                    }
                }

                if (doesNotHaveItem(player, Items.REPEATER, "Preferences")) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Preferences", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Preferences\",\"italic\": false,\"color\": \"white\"}");

                    ItemStack showPreferences = new ItemStack(Items.REPEATER);
                    showPreferences.setNbt(nbt);

                    player.getInventory().setStack(8, showPreferences);
                }

                if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && PlayerDataApi.getGlobalDataFor(player, isRunner) == NbtByte.ONE) {
                    PlayerDataApi.setGlobalDataFor(player, isRunner, NbtByte.ZERO);
                }
            }
        }

        if (gameState == PLAYING) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != null) {
                    if (player.getWorld().getRegistryKey().getValue().getNamespace().equals("manhunt")) {
                        allRunners = new LinkedList<>();

                        if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                            allRunners.add(player);
                        }
                    }
                }
            }
            if (settings.timeLimit != 0) {
                if (server.getWorld(overworldRegistryKey).getTime() % (20 * 60 * 60) / (20 * 60) >= settings.timeLimit) {
                    manhuntState(POSTGAME, server);
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        MessageUtil.showTitle(player, "manhunt.title.hunters", "manhunt.title.timelimit");
                        player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.1f, 1f);
                    }
                }
            }
        }
    }

    public static void playerJoin(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();

        if (!(player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && player.isTeamPlayer(player.getScoreboard().getTeam("runners")))) {
            player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));
        }

        if (gameState == PREGAME) {
            server.getPlayerManager().removeFromOperators(player.getGameProfile());
            player.teleport(server.getWorld(lobbyRegistryKey), 0, 63, 5.5, PositionFlag.ROT, 0, 0);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setFireTicks(0);
            player.setOnFire(false);
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

            updateGameMode(player);

            if (!player.isTeamPlayer(player.getScoreboard().getTeam("manhunters"))) {
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("manhunters"));
            }

            if (settings.setRoles == 3) {
                player.getScoreboard().clearTeam(player.getName().getString());
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));
            }
        }

        if (gameState == PLAYING) {
            updateGameMode(player);

            if (player.isTeamPlayer(player.getScoreboard().getTeam("manhunters"))) {
                player.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), player.getScoreboard().getTeam("manhunters"));
            }

            if (player.getWorld() == server.getWorld(lobbyRegistryKey) || player.getWorld() == server.getOverworld()) {
                player.getInventory().clear();
                updateGameMode(player);
                moveToSpawn(server.getWorld(overworldRegistryKey), player);
                player.removeStatusEffect(StatusEffects.SATURATION);
            }
        }

        if (gameState == POSTGAME) {
            player.getInventory().clear();
            updateGameMode(player);
            moveToSpawn(server.getWorld(overworldRegistryKey), player);
            player.removeStatusEffect(StatusEffects.SATURATION);
        }

        if (PlayerDataApi.getGlobalDataFor(player, isRunner) == null) {
            PlayerDataApi.setGlobalDataFor(player, isRunner, NbtByte.ZERO);
        }
        if (PlayerDataApi.getGlobalDataFor(player, winnerTitlePreference) == null) {
            PlayerDataApi.setGlobalDataFor(player, winnerTitlePreference, NbtByte.ONE);
        }
        if (PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolumePreference) == null) {
            PlayerDataApi.setGlobalDataFor(player, manhuntSoundsVolumePreference, NbtInt.of(100));
        }
        if (PlayerDataApi.getGlobalDataFor(player, settingsAtStartPreference) == null) {
            PlayerDataApi.setGlobalDataFor(player, settingsAtStartPreference, NbtByte.ONE);
        }
        if (PlayerDataApi.getGlobalDataFor(player, durationAtEndPreference) == null) {
            PlayerDataApi.setGlobalDataFor(player, durationAtEndPreference, NbtByte.ONE);
        }
        if (PlayerDataApi.getGlobalDataFor(player, bedExplosionDamagePreference) == null) {
            PlayerDataApi.setGlobalDataFor(player, bedExplosionDamagePreference, NbtByte.ONE);
        }
        if (PlayerDataApi.getGlobalDataFor(player, lavaPvpInTheNetherPreference) == null) {
            PlayerDataApi.setGlobalDataFor(player, lavaPvpInTheNetherPreference, NbtByte.ONE);
        }
    }

    public static void playerDisconnect(ServerPlayNetworkHandler handler) {
        PlayerDataApi.setGlobalDataFor(handler.getPlayer(), isRunner, NbtByte.ZERO);
    }

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, Hand hand) {
        var itemStack = player.getStackInHand(hand);

        if (gameState == PREGAME) {
            MinecraftServer server = player.getServer();

            if (!player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
                if (itemStack.getItem() == Items.RED_CONCRETE && itemStack.getNbt().getBoolean("NotReady")) {
                    isReady.put(player.getUuid(), true);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Ready", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Ready\",\"italic\": false,\"color\": \"green\"}");

                    ItemStack item = new ItemStack(Items.LIME_CONCRETE);
                    item.setNbt(nbt);

                    int slotNumber = player.getInventory().getSlotWithStack(itemStack);

                    player.getInventory().setStack(slotNumber, item);

                    player.getItemCooldownManager().set(item.getItem(), 20);

                    player.playSound(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 1.5f);

                    if (isReady.size() == server.getPlayerManager().getPlayerList().size()) {
                        if (player.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                            MessageUtil.sendBroadcast("manhunt.chat.minimum");
                        } else {
                            if (!player.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
                                startGame(server);
                            }
                        }
                    }

                    MessageUtil.sendBroadcast("manhunt.chat.ready", player.getName().getString(), isReady.size(), player.getWorld().getPlayers().size());
                }

                if (itemStack.getItem() == Items.GREEN_CONCRETE && itemStack.getNbt().getBoolean("Ready")) {
                    isReady.put(player.getUuid(), false);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("NotReady", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Unready\",\"italic\": false,\"color\": \"red\"}");

                    ItemStack item = new ItemStack(Items.RED_CONCRETE);
                    item.setNbt(nbt);

                    int slotNumber = player.getInventory().getSlotWithStack(itemStack);

                    player.getInventory().setStack(slotNumber, item);

                    player.playSound(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 0.5f);

                    MessageUtil.sendBroadcast("manhunt.chat.unready", player.getName().getString(), isReady.size(), player.getWorld().getPlayers().size());
                }

                if (itemStack.getItem() == Items.RECOVERY_COMPASS && itemStack.getNbt().getBoolean("Hunter")) {
                    player.getItemCooldownManager().set(itemStack.getItem(), 20);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Runner", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Runner\",\"italic\": false,\"color\": \"green\"}");

                    ItemStack item = new ItemStack(Items.CLOCK);
                    item.setNbt(nbt);

                    int slotNumber = player.getInventory().getSlotWithStack(itemStack);

                    player.getInventory().setStack(slotNumber, item);

                    itemStack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

                    player.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.5f, 1f);

                    player.getScoreboard().clearTeam(player.getName().getString());
                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("manhunters"));
                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));

                    MessageUtil.sendBroadcast("manhunt.chat.hunter", player.getName().getString());
                }

                if (itemStack.getItem() == Items.CLOCK && itemStack.getNbt().getBoolean("Runner")) {
                    player.getItemCooldownManager().set(itemStack.getItem(), 20);

                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Hunter", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"Hunter\",\"italic\": false,\"color\": \"aqua\"}");

                    ItemStack item = new ItemStack(Items.RECOVERY_COMPASS);
                    item.setNbt(nbt);

                    int slotNumber = player.getInventory().getSlotWithStack(itemStack);

                    player.getInventory().setStack(slotNumber, item);

                    itemStack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

                    player.playSound(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.5f, 1f);

                    player.getScoreboard().clearTeam(player.getName().getString());
                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("manhunters"));
                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));

                    MessageUtil.sendBroadcast("manhunt.chat.runner", player.getName().getString());

                    if (PlayerDataApi.getGlobalDataFor((ServerPlayerEntity) player, bedExplosionDamagePreference).equals(NbtByte.ZERO)) {
                        settings.bedExplosionDamage = false;
                        Configs.configHandler.saveToDisk();
                    } else {
                        settings.bedExplosionDamage = true;
                        Configs.configHandler.saveToDisk();
                    }

                    if (PlayerDataApi.getGlobalDataFor((ServerPlayerEntity) player, lavaPvpInTheNetherPreference).equals(NbtByte.ZERO)) {
                        settings.lavaPvpInTheNether = false;
                        Configs.configHandler.saveToDisk();
                    } else {
                        settings.lavaPvpInTheNether = true;
                        Configs.configHandler.saveToDisk();
                    }

                    MessageUtil.sendBroadcast("manhunt.chat.preferences", player.getName().getString());
                }

                if (itemStack.getItem() == Items.COMPARATOR && itemStack.getNbt().getBoolean("Settings")) {
                    settings((ServerPlayerEntity) player);
                }

                if (itemStack.getItem() == Items.REPEATER && itemStack.getNbt().getBoolean("Preferences")) {
                    preferences((ServerPlayerEntity) player);
                }
            }
        }

        if (gameState == PLAYING) {
            if (!settings.compassUpdate && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker") && !player.isSpectator() && player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
                player.getItemCooldownManager().set(itemStack.getItem(), 20);
                if (!itemStack.getNbt().contains("Info")) {
                    itemStack.getNbt().put("Info", new NbtCompound());
                }

                NbtCompound info = itemStack.getNbt().getCompound("Info");

                if (!info.contains("Name", NbtElement.STRING_TYPE) && !allRunners.isEmpty()) {
                    info.putString("Name", allRunners.get(0).getName().getString());
                }

                ServerPlayerEntity trackedPlayer = player.getServer().getPlayerManager().getPlayer(info.getString("Name"));

                if (trackedPlayer != null) {
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.1f, 1f);
                    updateCompass((ServerPlayerEntity) player, itemStack.getNbt(), trackedPlayer);
                }
            }

            if (!settings.lavaPvpInTheNether) {
                if (player.getWorld().getRegistryKey() == theNetherRegistryKey && itemStack.getItem() == Items.LAVA_BUCKET) {
                    if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                        for (String playerName : player.getScoreboard().getTeam("runners").getPlayerList()) {
                            if (player.distanceTo(player.getServer().getPlayerManager().getPlayer(playerName)) <= 9.0F) {
                                return TypedActionResult.fail(itemStack);
                            }
                        }
                    }
                }
            }
        }

        return TypedActionResult.success(itemStack);
    }

    public static void playerRespawn(ServerPlayerEntity newPlayer) {
        Scoreboard scoreboard = newPlayer.getScoreboard();
        if (newPlayer.isTeamPlayer(scoreboard.getTeam("runners"))) {
            PlayerDataApi.setGlobalDataFor(newPlayer, isRunner, NbtByte.ONE);
        }
        if (!newPlayer.isTeamPlayer(scoreboard.getTeam("hunters"))) {
            scoreboard.clearTeam(newPlayer.getName().getString());
            scoreboard.addScoreHolderToTeam(newPlayer.getName().getString(), scoreboard.getTeam("hunters"));
        }
    }

    private static boolean doesNotHaveItem(PlayerEntity player, Item item, String nbtBoolean) {
        boolean bool = false;
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem() == item && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Remove") && itemStack.getNbt().getBoolean(nbtBoolean)) {
                bool = true;
                break;
            }
        }

        if (player.playerScreenHandler.getCursorStack().getNbt() != null && player.playerScreenHandler.getCursorStack().getNbt().getBoolean(nbtBoolean)) {
            bool = true;
        } else if (player.getOffHandStack().hasNbt() && player.getOffHandStack().getNbt().getBoolean("Remove") && player.getOffHandStack().getNbt().getBoolean(nbtBoolean)) {
            bool = true;
        }
        return !bool;
    }

    private static void settings(ServerPlayerEntity player) {
        SimpleGui settings = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
        settings.setTitle(Text.of("Settings"));
        changeSetting(player, settings, "manhunt.item.setroles", "manhunt.lore.setroles", Items.FLETCHING_TABLE, 0, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
        changeSetting(player, settings, "manhunt.item.hunterfreeze", "manhunt.lore.hunterfreeze", Items.ICE, 1, SoundEvents.BLOCK_GLASS_BREAK);
        changeSetting(player, settings, "manhunt.item.timelimit", "manhunt.lore.timelimit", Items.CLOCK, 2, SoundEvents.ENTITY_FISHING_BOBBER_THROW);
        changeSetting(player, settings, "manhunt.item.compassupdate", "manhunt.lore.compassupdate", Items.COMPASS, 3, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK);
        changeSetting(player, settings, "manhunt.item.teamcolor", "manhunt.lore.teamcolor", Items.LEATHER_CHESTPLATE, 4, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);
        changeSetting(player, settings, "manhunt.item.gamedifficulty", "manhunt.lore.gamedifficulty", Items.CREEPER_HEAD, 5, SoundEvents.ENTITY_CREEPER_HURT);
        changeSetting(player, settings, "manhunt.item.bordersize", "manhunt.lore.bordersize", Items.STRUCTURE_VOID, 6, SoundEvents.BLOCK_DEEPSLATE_BREAK);
        changeSetting(player, settings, "manhunt.item.bedexplosiondamage", "manhunt.lore.bedexplosiondamage", Items.RED_BED, 7, SoundEvents.ENTITY_GENERIC_EXPLODE);
        changeSetting(player, settings, "manhunt.item.lavapvpinthenether", "manhunt.lore.lavapvpinthenether", Items.LAVA_BUCKET, 8, SoundEvents.ITEM_BUCKET_FILL_LAVA);
        settings.open();
    }

    private static void preferences(ServerPlayerEntity player) {
        SimpleGui preferences = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        preferences.setTitle(Text.of("Preferences"));
        List<Text> personalLore = new ArrayList<>();
        personalLore.add(MessageUtil.ofVomponent(player, "manhunt.lore.personalpreferences"));
        preferences.setSlot(11, new GuiElementBuilder(Items.PAPER)
                .setName(MessageUtil.ofVomponent(player, "manhunt.item.personalpreferences"))
                .setLore(personalLore)
                .setCallback(() -> {
                    personalPreferences(player);
                    player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 1f, 1f);
                })
        );
        List<Text> runnerLore = new ArrayList<>();
        runnerLore.add(MessageUtil.ofVomponent(player, "manhunt.lore.runnerpreferences"));
        preferences.setSlot(15, new GuiElementBuilder(Items.FEATHER)
                .setName(MessageUtil.ofVomponent(player, "manhunt.item.runnerpreferences"))
                .setLore(runnerLore)
                .setCallback(() -> {
                    runnerPreferences(player);
                    player.playSound(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.MASTER, 0.2f, 1f);
                })
        );
        preferences.open();
    }

    private static void personalPreferences(ServerPlayerEntity player) {
        SimpleGui personalPreferences = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
        personalPreferences.setTitle(Text.of("Preferences"));
        changePreferences(player, personalPreferences, winnerTitlePreference, "manhunt.item.winnertitle", "manhunt.lore.winnertitle", Items.OAK_SIGN, 0, SoundEvents.BLOCK_WOOD_PLACE);
        changePreferences(player, personalPreferences, manhuntSoundsVolumePreference, "manhunt.item.manhuntsoundsvolume", "manhunt.lore.manhuntsoundsvolume", Items.PLAYER_HEAD, 1, SoundEvents.ENTITY_PLAYER_BURP);
        changePreferences(player, personalPreferences, settingsAtStartPreference, "manhunt.item.settingsatstart", "manhunt.lore.settingsatstart", Items.IRON_BLOCK, 2, SoundEvents.BLOCK_METAL_PLACE);
        changePreferences(player, personalPreferences, durationAtEndPreference, "manhunt.item.durationatend", "manhunt.lore.durationatend", Items.CLOCK, 3, SoundEvents.BLOCK_SAND_BREAK);
        personalPreferences.open();
    }

    private static void runnerPreferences(ServerPlayerEntity player) {
        SimpleGui runnerPreferences = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
        runnerPreferences.setTitle(Text.of("Preferences"));
        changePreferences(player, runnerPreferences, bedExplosionDamagePreference, "manhunt.item.bedexplosiondamage", "manhunt.lore.bedexplosiondamage", Items.RED_BED, 0, SoundEvents.ENTITY_GENERIC_EXPLODE);
        changePreferences(player, runnerPreferences, lavaPvpInTheNetherPreference, "manhunt.item.lavapvpinthenether", "manhunt.lore.lavapvpinthenether", Items.LAVA_BUCKET, 1, SoundEvents.ITEM_BUCKET_FILL_LAVA);
        runnerPreferences.open();
    }

    private static void changeSetting(ServerPlayerEntity player, SimpleGui gui, String name, String lore, Item item, int slot, SoundEvent sound) {
        List<ServerPlayerEntity> playerList = player.getServer().getPlayerManager().getPlayerList();

        List<Text> loreList = new ArrayList<>();
        loreList.add(MessageUtil.ofVomponent(player, lore));
        if (name.equals("manhunt.item.setroles")) {
            switch (settings.setRoles) {
                case 1 -> loreList.add(Text.literal("Free Select").formatted(Formatting.GREEN));
                case 2 -> loreList.add(Text.literal("All Hunters").formatted(Formatting.YELLOW));
                default -> loreList.add(Text.literal("All Runners").formatted(Formatting.RED));
            }
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        switch (settings.setRoles) {
                            case 1 -> {
                                settings.setRoles = 2;
                                Configs.configHandler.saveToDisk();
                                MessageUtil.sendBroadcast("manhunt.chat.set.yellow", "Preset Mode", "All Hunters");
                                player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                                for (ServerPlayerEntity serverPlayer : playerList) {
                                    player.getScoreboard().clearTeam(player.getName().getString());
                                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("manhunters"));
                                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));
                                    serverPlayer.getInventory().clear();
                                }
                            }
                            case 2 -> {
                                settings.setRoles = 3;
                                Configs.configHandler.saveToDisk();
                                MessageUtil.sendBroadcast("manhunt.chat.set.red", "Preset Mode", "All Runners");
                                player.playSound(sound, SoundCategory.MASTER, 1f, 1.5f);
                                for (ServerPlayerEntity serverPlayer : playerList) {
                                    player.getScoreboard().clearTeam(player.getName().getString());
                                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("manhunters"));
                                    player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));
                                    serverPlayer.getInventory().clear();
                                }
                            }
                            default -> {
                                settings.setRoles = 1;
                                Configs.configHandler.saveToDisk();
                                MessageUtil.sendBroadcast("manhunt.chat.set.green", "Preset Mode", "Free Select");
                                player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
                            }
                        }
                        changeSetting(player, gui, name, lore, item, slot, sound);
                    })
            );
        }
        if (name.equals("manhunt.item.hunterfreeze")) {
            if (settings.hunterFreeze == 0) {
                loreList.add(Text.literal(settings.hunterFreeze + " seconds (disabled)").formatted(Formatting.RED));
            } else {
                loreList.add(Text.literal(settings.hunterFreeze + " seconds").formatted(Formatting.GREEN));
            }
        }
        if (name.equals("manhunt.item.timelimit")) {
            if (settings.timeLimit == 0) {
                loreList.add(Text.literal(settings.timeLimit + " minutes (disabled)").formatted(Formatting.RED));
            } else {
                loreList.add(Text.literal(settings.timeLimit + " minutes").formatted(Formatting.GREEN));
            }
        }
        if (name.equals("manhunt.item.bordersize")) {
            if (settings.borderSize == 59999968) {
                loreList.add(Text.literal(settings.borderSize + " blocks (maximum)").formatted(Formatting.RED));
            } else {
                loreList.add(Text.literal(settings.borderSize + " blocks").formatted(Formatting.GREEN));
            }
        }
        if (name.equals("manhunt.item.hunterfreeze") || name.equals("manhunt.item.timelimit") || name.equals("manhunt.item.bordersize")) {
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        AnvilInputGui inputGui = new AnvilInputGui(player, false) {
                            @Override
                            public void onInput(String input) {
                                this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                        .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                        .setCallback(() -> {
                                            try {
                                                int value = Integer.parseInt(input);
                                                if (name.equals("manhunt.item.hunterfreeze")) {
                                                    settings.hunterFreeze = value;
                                                    Configs.configHandler.saveToDisk();
                                                    if (settings.hunterFreeze == 0) {
                                                        MessageUtil.sendBroadcast("manhunt.chat.set.red", "Hunter Freeze", settings.hunterFreeze + " seconds (disabled)");
                                                        player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
                                                    } else {
                                                        MessageUtil.sendBroadcast("manhunt.chat.set.green", "Hunter Freeze", settings.hunterFreeze + " seconds");
                                                        player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                                                    }
                                                }
                                                if (name.equals("manhunt.item.timelimit")) {
                                                    settings.timeLimit = value;
                                                    Configs.configHandler.saveToDisk();
                                                    if (settings.timeLimit == 0) {
                                                        MessageUtil.sendBroadcast("manhunt.chat.set.red", "Time Limit", settings.timeLimit + " minutes (disabled)");
                                                        player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
                                                    } else {
                                                        MessageUtil.sendBroadcast("manhunt.chat.set.green", "Time Limit", settings.timeLimit + " minutes");
                                                        player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                                                    }
                                                }
                                                if (name.equals("manhunt.item.bordersize")) {
                                                    settings.borderSize = value;
                                                    Configs.configHandler.saveToDisk();
                                                    if (settings.borderSize == 0 || settings.borderSize >= 59999968) {
                                                        settings.borderSize = 59999968;
                                                        MessageUtil.sendBroadcast("manhunt.chat.set.red", "Border Size", settings.borderSize + " blocks (maximum)");
                                                        player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                                                    } else {
                                                        MessageUtil.sendBroadcast("manhunt.chat.set.green", "Border Size", settings.borderSize + " blocks");
                                                        player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                                                    }
                                                }
                                            } catch (NumberFormatException e) {
                                                MessageUtil.sendMessage(player, "manhunt.chat.invalid");
                                            }
                                            settings(player);
                                        })
                                );
                            }
                        };
                        inputGui.setTitle(MessageUtil.ofVomponent(player, "manhunt.lore.value"));
                        inputGui.setSlot(0, new GuiElementBuilder(Items.PAPER));
                        inputGui.setDefaultInputValue("");
                        inputGui.open();
                        Configs.configHandler.saveToDisk();
                    })
            );
        }
        if (name.equals("manhunt.item.compassupdate")) {
            if (settings.compassUpdate) {
                loreList.add(Text.literal("Automatic").formatted(Formatting.GREEN));
            } else {
                loreList.add(Text.literal("Manual").formatted(Formatting.RED));
            }
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        if (settings.compassUpdate) {
                            settings.compassUpdate = false;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.red", "Compass Update", "Manual");
                            player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
                        } else {
                            settings.compassUpdate = true;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.green", "Compass Update", "Automatic");
                            player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                        }
                        changeSetting(player, gui, name, lore, item, slot, sound);
                        Configs.configHandler.saveToDisk();
                    })
            );
        }

        if (name.equals("manhunt.item.teamcolor")) {
            if (settings.teamColor) {
                loreList.add(Text.literal("Show").formatted(Formatting.GREEN));
            } else {
                loreList.add(Text.literal("Hide").formatted(Formatting.RED));
            }
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        if (settings.teamColor) {
                            settings.teamColor = false;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.red", "Team Color", "Hide");
                            player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
                        } else {
                            settings.teamColor = true;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.green", "Team Color", "Show");
                            player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                        }
                        changeSetting(player, gui, name, lore, item, slot, sound);
                        Configs.configHandler.saveToDisk();
                    })
            );
        }
        if (name.equals("manhunt.item.gamedifficulty")) {
            if (settings.gameDifficulty == 1) {
                loreList.add(Text.literal("Easy").formatted(Formatting.GREEN));
            } else if (settings.gameDifficulty == 2) {
                loreList.add(Text.literal("Normal").formatted(Formatting.YELLOW));
            } else {
                loreList.add(Text.literal("Hard").formatted(Formatting.RED));
            }
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        if (settings.gameDifficulty == 1) {
                            settings.gameDifficulty = 2;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.yellow", "World Difficulty", "Normal");
                            player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                        } else if (settings.gameDifficulty == 2) {
                            settings.gameDifficulty = 3;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.red", "World Difficulty", "Hard");
                            player.playSound(sound, SoundCategory.MASTER, 1f, 0.8f);
                        } else {
                            settings.gameDifficulty = 1;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.green", "World Difficulty", "Easy");
                            player.playSound(sound, SoundCategory.MASTER, 1f, 1.2f);
                        }
                        changeSetting(player, gui, name, lore, item, slot, sound);
                        Configs.configHandler.saveToDisk();
                    })
            );
        }
        if (name.equals("manhunt.item.bedexplosiondamage")) {
            if (settings.bedExplosionDamage) {
                loreList.add(Text.literal("Enabled").formatted(Formatting.GREEN));
            } else {
                loreList.add(Text.literal("Disabled").formatted(Formatting.RED));
            }
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        if (settings.bedExplosionDamage) {
                            settings.bedExplosionDamage = false;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.red", "Bed Explosion Damage", "Disabled");
                            player.playSound(sound, SoundCategory.MASTER, 0.2f, 0.5f);
                        } else {
                            settings.bedExplosionDamage = true;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.green", "Bed Explosion Damage", "Enabled");
                            player.playSound(sound, SoundCategory.MASTER, 0.2f, 1f);
                        }
                        changeSetting(player, gui, name, lore, item, slot, sound);
                        Configs.configHandler.saveToDisk();
                    })
            );
        }
        if (name.equals("manhunt.item.lavapvpinthenether")) {
            if (settings.lavaPvpInTheNether) {
                loreList.add(Text.literal("Enabled").formatted(Formatting.GREEN));
            } else {
                loreList.add(Text.literal("Disabled").formatted(Formatting.RED));
            }
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        if (settings.lavaPvpInTheNether) {
                            settings.lavaPvpInTheNether = false;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.red", "Lava PvP in The Nether", "Disabled");
                            player.playSound(sound, SoundCategory.MASTER, 0.5f, 0.5f);
                        } else {
                            settings.lavaPvpInTheNether = true;
                            Configs.configHandler.saveToDisk();
                            MessageUtil.sendBroadcast("manhunt.chat.set.green", "Lava PvP in The Nether", "Enabled");
                            player.playSound(sound, SoundCategory.MASTER, 0.5f, 1f);
                        }
                        changeSetting(player, gui, name, lore, item, slot, sound);
                        Configs.configHandler.saveToDisk();
                    })
            );
        }
    }

    private static void changePreferences(ServerPlayerEntity player, SimpleGui gui, Identifier preference, String name, String lore, Item item, int slot, SoundEvent sound) {
        List<Text> loreList = new ArrayList<>();
        loreList.add(MessageUtil.ofVomponent(player, lore));
        if (!preference.equals(manhuntSoundsVolumePreference)) {
            if (PlayerDataApi.getGlobalDataFor(player, preference) == NbtByte.ONE) {
                if (!(preference.equals(bedExplosionDamagePreference) || preference.equals(lavaPvpInTheNetherPreference))) {
                    loreList.add(Text.literal("Show").formatted(Formatting.GREEN));
                } else {
                    loreList.add(Text.literal("Enabled").formatted(Formatting.GREEN));
                }
            } else {
                if (!(preference.equals(bedExplosionDamagePreference) || preference.equals(lavaPvpInTheNetherPreference))) {
                    loreList.add(Text.literal("Hide").formatted(Formatting.RED));
                } else {
                    loreList.add(Text.literal("Disabled").formatted(Formatting.RED));
                }
            }
        } else {
            int value = Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(player, preference)));
            if (value == 0) {
                loreList.add(Text.literal("0% (muted)").formatted(Formatting.RED));
            } else {
                loreList.add(Text.literal(PlayerDataApi.getGlobalDataFor(player, preference) + "%").formatted(Formatting.GREEN));
            }
        }
        if (!preference.equals(manhuntSoundsVolumePreference)) {
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        if (PlayerDataApi.getGlobalDataFor(player, preference) == NbtByte.ONE) {
                            PlayerDataApi.setGlobalDataFor(player, preference, NbtByte.ZERO);
                            if (preference.equals(bedExplosionDamagePreference)) {
                                player.playSound(sound, SoundCategory.MASTER, 0.2f, 0.5f);
                            } else {
                                player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
                            }
                        } else if (PlayerDataApi.getGlobalDataFor(player, preference) == NbtByte.ZERO) {
                            PlayerDataApi.setGlobalDataFor(player, preference, NbtByte.ONE);
                            if (preference.equals(bedExplosionDamagePreference)) {
                                player.playSound(sound, SoundCategory.MASTER, 0.2f, 1f);
                            } else {
                                player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
                            }
                        }
                        changePreferences(player, gui, preference, name, lore, item, slot, sound);
                    })
            );
        } else {
            gui.setSlot(slot, new GuiElementBuilder(item)
                    .hideFlags()
                    .setName(MessageUtil.ofVomponent(player, name))
                    .setLore(loreList)
                    .setCallback(() -> {
                        AnvilInputGui inputGui = new AnvilInputGui(player, false) {
                            @Override
                            public void onInput(String input) {
                                this.setSlot(2, new GuiElementBuilder(Items.PAPER)
                                        .setName(Text.literal(input).formatted(Formatting.ITALIC))
                                        .setCallback(() -> {
                                            try {
                                                int value = Integer.parseInt(input);
                                                if (value == 0) {
                                                    player.playSound(sound, SoundCategory.MASTER, 0.2f, 0.5f);
                                                } else if (value >= 100) {
                                                    player.playSound(sound, SoundCategory.MASTER, 0.2f, 2f);
                                                    value = 100;
                                                } else {
                                                    player.playSound(sound, SoundCategory.MASTER, 0.2f, 1f);
                                                }
                                                PlayerDataApi.setGlobalDataFor(player, preference, NbtInt.of(value));
                                            } catch (NumberFormatException e) {
                                                MessageUtil.sendMessage(player, "manhunt.chat.invalid");
                                            }
                                            personalPreferences(player);
                                        })
                                );
                            }
                        };
                        inputGui.setTitle(MessageUtil.ofVomponent(player, "manhunt.lore.value"));
                        inputGui.setSlot(0, new GuiElementBuilder(Items.PAPER));
                        inputGui.setDefaultInputValue("");
                        inputGui.open();
                    })
            );
        }
    }

    public static ManhuntState gameState;

    public static void manhuntState(ManhuntState newState, MinecraftServer server) {
        if (settings.changeMotd) {
            server.setMotd(newState.getColor() + "[" + newState.getMotd() + "]§f Minecraft MANHUNT");
        }
        gameState = newState;
    }

    public static void startGame(MinecraftServer server) {
        server.setFlightEnabled(true);

        manhuntState(PLAYING, server);

        var world = server.getWorld(overworldRegistryKey);

        for (ServerWorld serverWorld : server.getWorlds()) {
            ((ServerWorldInterface) serverWorld).getWorldProperties().setTime(0);
            serverWorld.setTimeOfDay(0);
            serverWorld.resetWeather();
        }

        world.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
        world.getGameRules().get(GameRules.DO_FIRE_TICK).set(true, server);
        world.getGameRules().get(GameRules.DO_INSOMNIA).set(true, server);
        world.getGameRules().get(GameRules.DO_MOB_LOOT).set(true, server);
        world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
        world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
        world.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(true, server);
        world.getGameRules().get(GameRules.FALL_DAMAGE).set(true, server);
        world.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(3, server);
        world.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
        world.getGameRules().get(GameRules.SPAWN_RADIUS).set(5, server);
        world.getGameRules().get(GameRules.FALL_DAMAGE).set(true, server);

        var difficulty = switch (ManhuntGame.settings.gameDifficulty) {
            case 2 -> Difficulty.NORMAL;
            case 3 -> Difficulty.HARD;
            default -> Difficulty.EASY;
        };

        server.setDifficulty(difficulty, true);

        server.setPvpEnabled(true);

        worldSpawnPos = setupSpawn(world);

        if (!settings.teamColor) {
            server.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
            server.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
        }

        if (!settings.teamColor) {
            server.getScoreboard().getTeam("hunters").setColor(Formatting.RED);
            server.getScoreboard().getTeam("runners").setColor(Formatting.GREEN);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            setPlayerSpawnXYZ(server.getWorld(overworldRegistryKey), player);

            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.BOAT_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.ANIMALS_BRED));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.AVIATE_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.BELL_RING));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.CLEAN_ARMOR));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.CLEAN_BANNER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.CLEAN_SHULKER_BOX));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.CLIMB_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.CROUCH_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_ABSORBED));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_BLOCKED_BY_SHIELD));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT_ABSORBED));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT_RESISTED));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_TAKEN));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.DROP));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.EAT_CAKE_SLICE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.ENCHANT_ITEM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.FALL_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.FILL_CAULDRON));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.FISH_CAUGHT));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.FLY_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.HORSE_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INSPECT_DISPENSER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INSPECT_DROPPER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INSPECT_HOPPER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_ANVIL));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_BEACON));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_BREWINGSTAND));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_BLAST_FURNACE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_CAMPFIRE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_CARTOGRAPHY_TABLE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_CRAFTING_TABLE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_FURNACE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_GRINDSTONE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_LECTERN));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_LOOM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_SMITHING_TABLE));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_SMOKER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.INTERACT_WITH_STONECUTTER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.LEAVE_GAME));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.MINECART_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.OPEN_BARREL));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.OPEN_CHEST));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.OPEN_ENDERCHEST));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.OPEN_SHULKER_BOX));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.PIG_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_NOTEBLOCK));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_RECORD));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAYER_KILLS));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.POT_FLOWER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.RAID_TRIGGER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.RAID_WIN));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.SLEEP_IN_BED));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.SNEAK_TIME));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.STRIDER_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.SWIM_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TALKED_TO_VILLAGER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TARGET_HIT));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TOTAL_WORLD_TIME));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TRADED_WITH_VILLAGER));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TRIGGER_TRAPPED_CHEST));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ON_WATER_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
            player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_UNDER_WATER_ONE_CM));
            Stats.MINED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.CRAFTED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.USED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.BROKEN.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.PICKED_UP.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.DROPPED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.KILLED.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.KILLED_BY.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));
            Stats.CUSTOM.forEach(stat -> player.resetStat(stat.getType().getOrCreateStat(stat.getValue())));

            server.getPlayerManager().removeFromOperators(player.getGameProfile());
            double playerX = Double.parseDouble(String.valueOf(PlayerDataApi.getGlobalDataFor(player, playerSpawnX)));
            double playerY = Double.parseDouble(String.valueOf(PlayerDataApi.getGlobalDataFor(player, playerSpawnY)));
            double playerZ = Double.parseDouble(String.valueOf(PlayerDataApi.getGlobalDataFor(player, playerSpawnZ)));
            player.teleport(world, playerX, playerY, playerZ, 0.0F, 0.0F);
            player.setSpawnPoint(world.getRegistryKey(), worldSpawnPos, 0.0F, true, false);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);

            updateGameMode(player);

            if (player.isTeamPlayer(player.getScoreboard().getTeam("manhunters"))) {
                player.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), player.getScoreboard().getTeam("manhunters"));
            }

            if (!(PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolumePreference) == NbtInt.of(0))) {
                float volume = (float) Integer.parseInt(String.valueOf(PlayerDataApi.getGlobalDataFor(player, manhuntSoundsVolumePreference))) / 100;
                if (volume >= 0.2f) {
                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.BLOCKS, volume / 2, 1.5f);
                }
            }

            if (settings.hunterFreeze != 0) {
                if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, settings.hunterFreeze * 20, 255, false, true));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, settings.hunterFreeze * 20, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, settings.hunterFreeze * 20, 248, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (settings.hunterFreeze - 1) * 20, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, settings.hunterFreeze * 20, 255, false, false));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, settings.hunterFreeze * 20, 255, false, false));
                }
            }

            if (PlayerDataApi.getGlobalDataFor(player, winnerTitlePreference) == NbtByte.ONE) {
                MessageUtil.showTitle(player, "manhunt.title.gamemode", "manhunt.title.start");
            }

            if (PlayerDataApi.getGlobalDataFor(player, settingsAtStartPreference) == NbtByte.ONE) {
                showSettings(player);
            }
        }
    }

    public static void updateGameMode(ServerPlayerEntity player) {
        if (gameState == PREGAME) {
            player.changeGameMode(GameMode.ADVENTURE);
        } else if (gameState == PLAYING) {
            player.changeGameMode(GameMode.SURVIVAL);
        } else {
            player.changeGameMode(GameMode.SPECTATOR);
        }
    }

    // Thanks to https://github.com/Ivan-Khar/manhunt-fabricated for the updateCompass method

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

    public static void resetGame(ServerCommandSource source) {
        manhuntState(PREGAME, source.getServer());

        if (!allRunners.isEmpty()) {
            for (ServerPlayerEntity player : allRunners) {
                PlayerDataApi.setGlobalDataFor(player, isRunner, NbtByte.ONE);
            }
        }

        new ManhuntWorldModule().resetWorlds(source.getServer());
    }

    public static void unloadWorld(MinecraftServer server, ServerWorld world) {
        new ManhuntWorldModule().onWorldUnload(server, world);
    }

    private static void moveToSpawn(ServerWorld world, ServerPlayerEntity player) {
        BlockPos blockPos = worldSpawnPos;
        long l;
        long m;
        int i = Math.max(0, SERVER.getSpawnRadius(world));
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
            player.teleport(world, blockPos2.getX(), blockPos2.getY(), blockPos2.getZ(), 0.0F, 0.0F);
            if (!world.isSpaceEmpty(player)) {
                continue;
            }
            break;
        }
    }

    public static void setPlayerSpawnXYZ(ServerWorld world, ServerPlayerEntity player) {
        BlockPos blockPos = worldSpawnPos;
        long l;
        long m;
        int i = Math.max(0, SERVER.getSpawnRadius(world));
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
            PlayerDataApi.setGlobalDataFor(player, playerSpawnX, NbtInt.of(blockPos2.getX()));
            PlayerDataApi.setGlobalDataFor(player, playerSpawnY, NbtInt.of(blockPos2.getY()));
            PlayerDataApi.setGlobalDataFor(player, playerSpawnZ, NbtInt.of(blockPos2.getZ()));
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

    public static void showSettings(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();

        if (settings.setRoles == 1) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Set Roles", "Free Select");
        } else if (settings.setRoles == 2) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.yellow", "Set Roles", "All Hunters");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Set Roles", "All Runners");
        }

        if (settings.hunterFreeze == 0) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Hunter Freeze", "0 seconds (disabled)");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Hunter Freeze", settings.hunterFreeze + " seconds");
        }

        if (settings.timeLimit == 0) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Time Limit", "0 minutes (disabled)");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Time Limit", settings.timeLimit + " minutes");
        }

        if (settings.compassUpdate) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Compass Update", "Automatic");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Compass Update", "Manual");
        }

        if (settings.teamColor) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Team Color", "Show");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Team Color", "Hide");
        }

        if (settings.gameDifficulty == 1) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "World Difficulty", "Easy");
        } else if (settings.gameDifficulty == 2) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.yellow", "World Difficulty", "Normal");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "World Difficulty", "Hard");
        }

        if (settings.borderSize == 59999968) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Border Size", "59999968 blocks (maximum)");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Border Size", settings.borderSize + " blocks");
        }

        if (settings.bedExplosionDamage) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Bed Explosion Damage", "Enabled");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Bed Explosion Damage", "Disabled");
        }

        if (settings.lavaPvpInTheNether) {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.green", "Lava PvP in The Nether", "Enabled");
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.setting.red", "Lava PvP in The Nether", "Disabled");
        }

        int viewDistance = server.getPlayerManager().getViewDistance();

        if (viewDistance >= 10 && viewDistance <= 12) {
            MessageUtil.sendMessage(player, "manhunt.chat.property.green", "View Distance", viewDistance);
        } else if (viewDistance >= 13 && viewDistance <= 18) {
            MessageUtil.sendMessage(player, "manhunt.chat.property.yellow", "View Distance", viewDistance);
        } else {
            MessageUtil.sendMessage(player, "manhunt.chat.property.red", "View Distance", viewDistance);
        }
    }

    public static BlockPos setupSpawn(ServerWorld world) {
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
