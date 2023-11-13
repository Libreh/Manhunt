package manhunt;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.commands.*;
import mrnavastar.sqlib.DataContainer;
import mrnavastar.sqlib.Table;
import mrnavastar.sqlib.database.Database;
import mrnavastar.sqlib.database.MySQLDatabase;
import mrnavastar.sqlib.database.SQLiteDatabase;
import mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import nota.Nota;
import nota.model.Song;
import nota.player.RadioSongPlayer;
import nota.utils.NBSDecoder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static manhunt.Config.*;
import static manhunt.GameState.PLAYING;
import static manhunt.GameState.PREGAME;

public class Manhunt implements ModInitializer {
	public static final String MOD_ID = "manhunt";
	public static final Identifier LOBBY_WORLD_ID = new Identifier(MOD_ID, "lobby");
	public static RegistryKey<World> lobbyRegistryKey = RegistryKey.of(RegistryKeys.WORLD, LOBBY_WORLD_ID);
	public static List<ServerPlayerEntity> allPlayers;
	public static List<ServerPlayerEntity> allRunners;
	public static List<String> songs = new ArrayList<>();
	public static HashMap<UUID, Boolean> isReady = new HashMap<>();
	public static HashMap<UUID, Boolean> muteLobbyMusic = new HashMap<>();
	public static HashMap<UUID, Boolean> doNotDisturb = new HashMap<>();
	public static HashMap<UUID, String> pingSound = new HashMap<>();
	public static HashMap<UUID, Boolean> gameLeader = new HashMap<>();
	public static HashMap<UUID, String> currentRole = new HashMap<>();
	public static HashMap<UUID, Integer> parkourTimer = new HashMap<>();
	public static HashMap<UUID, Boolean> startedParkour = new HashMap<>();
	public static HashMap<UUID, Boolean> finishedParkour = new HashMap<>();
	private long lastDelay = System.currentTimeMillis();
	private boolean holding;
	private static boolean paused;
	public static boolean isPaused() {
		return paused;
	}
	public static void setPaused(boolean paused) {
		Manhunt.paused = paused;
	}
	public static Database database;
	public static Table table;

	@Override
	public void onInitialize() {
		load();

		if (databaseType.equals("SQLITE") && !sqliteDirectory.equals("/path/to/folder")) {
			if (!new File(sqliteDirectory).exists()) {
				log(Level.FATAL, "Halting initialization! " + sqliteDirectory + " does not exist!");
				System.exit(0);
			}

			database = new SQLiteDatabase("database", databaseName, sqliteDirectory);

		} else if (databaseType.equals("MYSQL") && !mysqlUser.equals("username") && !mysqlPassword.equals("password")) {
			database = new MySQLDatabase("database", databaseName, mysqlAddress, String.valueOf(mysqlPort), mysqlUser, mysqlPassword);
		} else {
			log(Level.FATAL, "Halting initialization! You need to change some settings in the InvSync config");
			System.exit(0);
		}

		table = database.createTable("playerdata")
				.addColumn("mutelobbymusic", SQLDataType.BOOL)
				.addColumn("donotdisturb", SQLDataType.BOOL)
				.addColumn("pingsound", SQLDataType.STRING)
				.addColumn("gameleader", SQLDataType.BOOL)
				.finish();

		try {
			DeleteWorld.invoke();
		} catch (IOException ignored) {
		}

		log(Level.INFO, "Manhunt mod initialized");

		setPaused(false);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			DoNotDisturbCommand.register(dispatcher);
			DurationCommand.register(dispatcher);
			HunterCommand.register(dispatcher);
			PauseCommand.register(dispatcher);
			PingCommand.register(dispatcher);
			PingSoundCommand.register(dispatcher);
			RunnerCommand.register(dispatcher);
			StartCommand.register(dispatcher);
			TmCoordsCommand.register(dispatcher);
			UnpauseCommand.register(dispatcher);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			gameState(PREGAME, server);

			var difficulty = switch (worldDifficulty) {
				case "Normal" -> Difficulty.NORMAL;
				case "Hard" -> Difficulty.HARD;
				default -> Difficulty.EASY;
			};
			server.setDifficulty(difficulty, true);
			server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
			server.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
			server.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
			server.getGameRules().get(GameRules.DO_MOB_LOOT).set(false, server);
			server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
			server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
			server.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);
			server.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(0, server);
			server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
			server.getGameRules().get(GameRules.SPAWN_RADIUS).set(0, server);

			server.setPvpEnabled(false);

			server.getScoreboard().addTeam("players");
			server.getScoreboard().addTeam("hunters");
			server.getScoreboard().addTeam("runners");

			server.getScoreboard().getTeam("players").setCollisionRule(AbstractTeam.CollisionRule.NEVER);

			try {
				spawnStructure(server);
			} catch (IOException e) {
				log(Level.FATAL, "Failed to spawn Manhunt mod lobby");
			}

			try (Stream<Path> paths = Files.walk(Paths.get(FabricLoader.getInstance().getGameDir().resolve("nbs").toUri()))) {
				paths
						.filter(Files::isRegularFile)
						.forEach(file -> songs.add(String.valueOf(file.getFileName())));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if (gameState == PREGAME) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					if (!hasItem(Items.RED_CONCRETE, player, "NotReady") && !hasItem(Items.LIME_CONCRETE, player, "Ready")) {
						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("NotReady", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.unready\",\"italic\": false,\"color\": \"white\"}");

						ItemStack itemStack = new ItemStack(Items.RED_CONCRETE);
						itemStack.setNbt(nbt);

						player.getInventory().setStack(0, itemStack);
					}

					if (!hasItem(Items.RECOVERY_COMPASS, player, "Hunter") && setRoles.equals("Free Select")) {
						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("Hunter", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.hunter\",\"italic\": false,\"color\": \"white\"}");

						ItemStack itemStack = new ItemStack(Items.RECOVERY_COMPASS);
						itemStack.setNbt(nbt);

						player.getInventory().setStack(3, itemStack);
					}

					if (!hasItem(Items.CLOCK, player, "Runner") && setRoles.equals("Free Select")) {
						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("Runner", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.runner\",\"italic\": false,\"color\": \"white\"}");

						ItemStack itemStack = new ItemStack(Items.CLOCK);
						itemStack.setNbt(nbt);

						player.getInventory().setStack(5, itemStack);
					}

					if (!hasItem(Items.COMPARATOR, player, "Settings")) {
						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("Settings", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.settings\",\"italic\": false,\"color\": \"white\"}");

						ItemStack itemStack = new ItemStack(Items.COMPARATOR);
						itemStack.setNbt(nbt);

						player.getInventory().setStack(8, itemStack);
					}

					if (player.getZ() < 0 && !player.hasPermissionLevel(2) && !player.hasPermissionLevel(4)) {
						int ticks = parkourTimer.get(player.getUuid());
						if (startedParkour.get(player.getUuid()).equals(false) && finishedParkour.get(player.getUuid()).equals(false) && player.getZ() < -4 && !(player.getZ() < -6)) {
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), 1f);
							startedParkour.put(player.getUuid(), true);
						}
						if (startedParkour.get(player.getUuid()).equals(true) && player.getZ() < -4) {
							parkourTimer.put(player.getUuid(), parkourTimer.get(player.getUuid()) + 1);
							player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, StatusEffectInstance.INFINITE, 255, false, false, false));
						}
						int sec = (int) Math.floor(((double) (ticks % (20 * 60)) / 20));
						String secSeconds;
						if (sec < 10) {
							secSeconds = "0" + sec;
						} else {
							secSeconds = String.valueOf(sec);
						}
						int ms = (int) Math.floor(((double) (ticks * 5) % 100));
						String msSeconds;
						if (ms < 10) {
							msSeconds = "0" + ms;
						} else if (ms > 99) {
							msSeconds = "00";
						} else {
							msSeconds = String.valueOf(ms);
						}
						if (player.getZ() < -4 && finishedParkour.get(player.getUuid()).equals(false))
							player.sendMessage(Text.translatable("manhunt.time.current", secSeconds, msSeconds), true);
						if (player.getZ() < -24 && player.getZ() > -27) {
							if (player.getX() < -6) {
								if (player.getY() >= 70 && player.getY() < 72 && startedParkour.get(player.getUuid()).equals(true) && finishedParkour.get(player.getUuid()).equals(false)) {
									player.sendMessage(Text.translatable("manhunt.time.current", secSeconds, msSeconds).formatted(Formatting.GREEN), true);
									playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), 2f);
									finishedParkour.put(player.getUuid(), true);
								}
							}
						}
						if (startedParkour.get(player.getUuid()).equals(true) && player.getZ() > -4) {
							player.sendMessage(Text.translatable("manhunt.time.current", secSeconds, msSeconds).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), 0.5f);
						}
						if (startedParkour.get(player.getUuid()).equals(true) && player.getY() < 61) {
							player.sendMessage(Text.translatable("manhunt.time.current", secSeconds, msSeconds).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), 0.5f);
						}
						if (startedParkour.get(player.getUuid()).equals(true) && player.getZ() < -27 && player.getY() < 68) {
							player.sendMessage(Text.translatable("manhunt.time.current", secSeconds, msSeconds).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), 0.5f);
						}
					}

					if (player.getX() < -24 || player.getX() > 24 && !player.hasPermissionLevel(2) && !player.hasPermissionLevel(4)) {
						resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
					}
					if (player.getY() < 54 || player.getY() > 74 && !player.hasPermissionLevel(2) && !player.hasPermissionLevel(4)) {
						resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
					}
					if (player.getZ() < -64 || player.getZ() > 32 && !player.hasPermissionLevel(2) && !player.hasPermissionLevel(4)) {
						resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
					}
					if (!player.isTeamPlayer(player.getScoreboard().getTeam("players"))) {
						player.getScoreboard().addPlayerToTeam(player.getName().getString(), player.getScoreboard().getTeam("players"));
					}
					if (player.getWorld() != server.getWorld(lobbyRegistryKey)) {
						player.teleport(server.getWorld(lobbyRegistryKey), 0.5, 63, 0, 0, 0);
					}

					if (!player.getInventory().isEmpty() && !player.getInventory().getStack(3).getItem().equals(Items.RECOVERY_COMPASS) && setRoles.equals("Free Select")) {
						player.getInventory().clear();
					}
					if (!player.getInventory().isEmpty() && !player.getInventory().getStack(5).getItem().equals(Items.CLOCK) && setRoles.equals("Free Select")) {
						player.getInventory().clear();
					}
				}
			}

			if (gameState == PLAYING) {
				for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
					if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters")) && player.isAlive()) {
						if (!hasTracker(player)) {
							NbtCompound nbt = new NbtCompound();
							nbt.putBoolean("Remove", true);
							nbt.putBoolean("Tracker", true);
							nbt.putBoolean("LodestoneTracked", false);
							nbt.putString("LodestoneDimension", "minecraft:overworld");
							nbt.putInt("HideFlags", 1);
							nbt.put("Info", new NbtCompound());
							nbt.put("display", new NbtCompound());
							nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.tracker\",\"italic\": false,\"color\": \"white\"}");

							ItemStack stack = new ItemStack(Items.COMPASS);
							stack.setNbt(nbt);
							stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

							player.giveItemStack(stack);
						} else if (compassUpdate.equals("Automatic") && System.currentTimeMillis() - lastDelay > ((long) 1000)) {
							for (ItemStack itemStack : player.getInventory().main) {
								if (itemStack.getItem().equals(Items.COMPASS) && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker")) {
									ServerPlayerEntity trackedPlayer = server.getPlayerManager().getPlayer(itemStack.getNbt().getCompound("Info").getString("Name"));
									if (trackedPlayer != null) {
										updateCompass(player, itemStack.getNbt(), trackedPlayer);
										player.getItemCooldownManager().set(itemStack.getItem(), 20);
									}
								}
							}
							lastDelay = System.currentTimeMillis();
						}
						if (holdingTracker(player)) {
							holding = true;
							if (player.getMainHandStack().getNbt() != null && player.getMainHandStack().getNbt().getBoolean("Tracker")) {
								NbtCompound info = player.getMainHandStack().getNbt().getCompound("Info");
								if (server.getPlayerManager().getPlayer(info.getString("Name")) != null) {
									showInfo(player, info);
								}
							} else if (player.getOffHandStack().getNbt() != null) {
								NbtCompound info = player.getOffHandStack().getNbt().getCompound("Info");
								if (server.getPlayerManager().getPlayer(info.getString("Name")) != null) {
									showInfo(player, info);
								}
							}
						} else {
							if (holding) {
								player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of("")));
								holding = false;
							}
						}
					}
				}

				if (server.getOverworld().getTime() % (20 * 60 * 60) / (20 * 60) >= timeLimit && timeLimit != 0) {
					gameState(GameState.POSTGAME, server);
					for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
						player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.hunters")));
						player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.config.TIME_LIMIT")));
						player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 0.2f, 0.5f);
					}
				}
			}
		});

		ServerTickEvents.START_WORLD_TICK.register(world -> {
			if (gameState == PLAYING) {
				allPlayers = world.getServer().getPlayerManager().getPlayerList();
				allRunners = new LinkedList<>();

				if (showTeamColor) {
					world.getServer().getScoreboard().getTeam("hunters").setColor(Formatting.RED);;
					world.getServer().getScoreboard().getTeam("runners").setColor(Formatting.GREEN);;
				}

				for (ServerPlayerEntity player : allPlayers) {
					if (player != null) {
						if (player.isTeamPlayer(world.getServer().getScoreboard().getTeam("runners"))) {
							allRunners.add(player);
						}
						if (!player.isTeamPlayer(world.getServer().getScoreboard().getTeam("hunters")) && !player.isTeamPlayer(world.getServer().getScoreboard().getTeam("runners"))) {
							if (currentRole.get(player.getUuid()).equals("hunter")) {
								player.getScoreboard().addPlayerToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));
							} else {
								player.getScoreboard().addPlayerToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));
							}
						}
					}
				}
			}
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			var itemStack = player.getStackInHand(hand);

			if (gameState == PREGAME) {
				if (!player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
					if (itemStack.getItem() == Items.RED_CONCRETE && itemStack.getNbt().getBoolean("NotReady")) {
						isReady.put(player.getUuid(), true);

						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("Ready", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.ready\",\"italic\": false,\"color\": \"white\"}");

						ItemStack item = new ItemStack(Items.LIME_CONCRETE);
						item.setNbt(nbt);

						player.getInventory().setStack(0, item);

						player.getItemCooldownManager().set(item.getItem(), 20);

						player.playSound(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 1.5f);

						if (isReady.size() == player.getServer().getPlayerManager().getPlayerList().size()) {
							for (PlayerEntity lobbyPlayer : player.getServer().getPlayerManager().getPlayerList()) {
								if (currentRole.get(lobbyPlayer.getUuid()).equals("runner")) {
									lobbyPlayer.getScoreboard().addPlayerToTeam(lobbyPlayer.getName().getString(), lobbyPlayer.getScoreboard().getTeam("runners"));
								} else {
									lobbyPlayer.getScoreboard().addPlayerToTeam(lobbyPlayer.getName().getString(), lobbyPlayer.getScoreboard().getTeam("hunters"));
								}
							}
							if (Collections.frequency(currentRole.values(), "runner") == 0) {
								world.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.minimum"), false);
							} else {
								if (Collections.frequency(currentRole.values(), "runner") >= 1) {
									startGame(player.getServer());
								}
							}
						}

						player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.ready", player.getName().getString(), isReady.size(), player.getWorld().getPlayers().size()), false);
					}

					if (itemStack.getItem() == Items.LIME_CONCRETE && itemStack.getNbt().getBoolean("Ready")) {
						isReady.put(player.getUuid(), false);

						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("NotReady", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.unready\",\"italic\": false,\"color\": \"white\"}");

						ItemStack item = new ItemStack(Items.RED_CONCRETE);
						item.setNbt(nbt);

						player.getItemCooldownManager().set(item.getItem(), 20);

						player.getInventory().setStack(0, item);

						player.playSound(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 0.5f);

						player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.unready", player.getName().getString(), isReady.size(), player.getWorld().getPlayers().size()), false);
					}

					if (itemStack.getItem() == Items.RECOVERY_COMPASS && itemStack.getNbt().getBoolean("Hunter") && !player.getItemCooldownManager().isCoolingDown(Items.CLOCK)) {
						player.getItemCooldownManager().set(itemStack.getItem(), 20);

						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("Runner", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.runner\",\"italic\": false,\"color\": \"white\"}");

						ItemStack item = new ItemStack(Items.CLOCK);
						item.setNbt(nbt);

						player.getInventory().setStack(3, item);

						itemStack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

						player.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.5f, 1f);

						currentRole.put(player.getUuid(), "hunter");

						player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.hunter", player.getName().getString()), false);
					}

					if (itemStack.getItem() == Items.CLOCK && itemStack.getNbt().getBoolean("Runner") && !player.getItemCooldownManager().isCoolingDown(Items.RECOVERY_COMPASS)) {
						player.getItemCooldownManager().set(itemStack.getItem(), 20);

						NbtCompound nbt = new NbtCompound();
						nbt.putBoolean("Remove", true);
						nbt.putBoolean("Hunter", true);
						nbt.putInt("HideFlags", 1);
						nbt.put("display", new NbtCompound());
						nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.hunter\",\"italic\": false,\"color\": \"white\"}");

						ItemStack item = new ItemStack(Items.RECOVERY_COMPASS);
						item.setNbt(nbt);

						player.getInventory().setStack(5, item);

						itemStack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

						player.playSound(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.5f, 1f);

						currentRole.put(player.getUuid(), "runner");

						player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.runner", player.getName().getString()), false);
					}

					if (itemStack.getItem() == Items.COMPARATOR && itemStack.getNbt().getBoolean("Settings")) {
						if (itemStack.getNbt().getBoolean("Settings")) {
							settings((ServerPlayerEntity) player);
						}
					}
				}
			}

			if (gameState == PLAYING) {
				if (itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker") && !player.isSpectator() && player.isTeamPlayer(world.getScoreboard().getTeam("hunters")) && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
					player.getItemCooldownManager().set(itemStack.getItem(), 20);
					if (!itemStack.getNbt().contains("Info")) {
						itemStack.getNbt().put("Info", new NbtCompound());
					}
					NbtCompound info = itemStack.getNbt().getCompound("Info");

					if (!info.contains("Name", NbtElement.STRING_TYPE) && !Manhunt.allRunners.isEmpty()) {
						info.putString("Name", Manhunt.allRunners.get(0).getName().getString());
					}

					ServerPlayerEntity trackedPlayer = world.getServer().getPlayerManager().getPlayer(info.getString("Name"));

					if (trackedPlayer != null) {
						player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.5f, 1f);
						updateCompass((ServerPlayerEntity) player, itemStack.getNbt(), trackedPlayer);
					}
				}
			}

			return TypedActionResult.pass(itemStack);
		});

		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			for (ServerPlayerEntity player : sender.getServer().getPlayerManager().getPlayerList()) {
				if (message.getSignedContent().contains(player.getName().getString())) {
					if (!doNotDisturb.get(player.getUuid())) {
						if (pingSound.get(player.getUuid()).isEmpty()) {
							player.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, 1f);
						} else {
							SoundEvent event = Registries.SOUND_EVENT.get(Identifier.tryParse(pingSound.get(player.getUuid())));
							player.playSound(event, SoundCategory.PLAYERS, 1f, 1f);
						}
						player.sendMessage(Text.translatable("manhunt.pingsound.pinged", Text.translatable(sender.getName().getString())));
					}
				}
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();

			muteLobbyMusic.put(player.getUuid(), false);
			doNotDisturb.put(player.getUuid(), false);
			pingSound.put(player.getUuid(), "minecraft:block.bell.use");
			gameLeader.put(player.getUuid(), false);

			DataContainer dataContainer = table.get(player.getUuid());

			if (dataContainer == null) {
				dataContainer = table.createDataContainer(player.getUuid());
				dataContainer.put("mutelobbymusic", muteLobbyMusic.get(player.getUuid()));
				dataContainer.put("donotdisturb", doNotDisturb.get(player.getUuid()));
				dataContainer.put("pingsound", pingSound.get(player.getUuid()));
				dataContainer.put("gameleader", gameLeader.get(player.getUuid()));
			}

			dataContainer = table.get(player.getUuid());

			muteLobbyMusic.put(player.getUuid(), dataContainer.getBool("mutelobbymusic"));
			doNotDisturb.put(player.getUuid(), dataContainer.getBool("donotdisturb"));
			pingSound.put(player.getUuid(), dataContainer.getString("pingsound"));
			gameLeader.put(player.getUuid(), dataContainer.getBool("gameleader"));

			if (gameState == PREGAME) {
				if (!muteLobbyMusic.get(player.getUuid())) {
					Nota.stopPlaying(player);
					playLobbyMusic(player);
				}

				player.changeGameMode(GameMode.ADVENTURE);

				parkourTimer.put(player.getUuid(), 0);
				startedParkour.put(player.getUuid(), false);
				finishedParkour.put(player.getUuid(), false);

				player.getInventory().clear();

				player.setHealth(20);
				player.clearStatusEffects();
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
				player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
				player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.5f, 0.5f);

				if (!setRoles.equals("Free Select")) {
					NbtCompound nbt = new NbtCompound();
					nbt.putBoolean("Remove", true);
					ItemStack itemStack = new ItemStack(Items.BARRIER);
					itemStack.setNbt(nbt);
					for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
						serverPlayer.getInventory().setStack(3, itemStack);
						serverPlayer.getInventory().setStack(5, itemStack);
					}
					if (setRoles.equals("All Runners")) {
						currentRole.put(player.getUuid(), "runner");
						player.getServer().getScoreboard().addPlayerToTeam(player.getName().getString(), player.getServer().getScoreboard().getTeam("runners"));
					}
				}

				if (setRoles.equals("Free Select") || setRoles.equals("All Hunters")) {
					currentRole.put(player.getUuid(), "hunter");
					player.getServer().getScoreboard().addPlayerToTeam(player.getName().getString(), player.getServer().getScoreboard().getTeam("hunters"));
				}

				if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
					gameLeader.put(player.getUuid(), true);
				} else if (!player.hasPermissionLevel(2) && !player.hasPermissionLevel(4)) {
					gameLeader.put(player.getUuid(), false);
				}
			}

			if (gameState == GameState.PLAYING) {
				Team hunters = player.getScoreboard().getTeam("hunters");
				Team runners = player.getScoreboard().getTeam("runners");

				if (!player.isTeamPlayer(hunters) && !player.isTeamPlayer(runners)) {
					if (latePlayers.equals("Join Hunters")) {
						player.getScoreboard().addPlayerToTeam(player.getName().getString(), hunters);
					} else {
						player.changeGameMode(GameMode.SPECTATOR);
					}
				}
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayerEntity player = handler.getPlayer();

			DataContainer dataContainer = table.get(player.getUuid());

			dataContainer.put("mutelobbymusic", muteLobbyMusic.get(player.getUuid()));
			dataContainer.put("donotdisturb", doNotDisturb.get(player.getUuid()));
			dataContainer.put("pingsound", pingSound.get(player.getUuid()));
			dataContainer.put("gameleader", gameLeader.get(player.getUuid()));
		});
	}

	public static void log(Level level, String message) {
		LogManager.getLogger().log(level, "[" + MOD_ID + "] " + message);
	}

	private void spawnStructure(MinecraftServer server) throws IOException {
		var lobbyParkourNbt = NbtIo.readCompressed(getClass().getResourceAsStream("/manhunt/lobby/parkour.nbt"));
		var lobbyIslandNbt = NbtIo.readCompressed(getClass().getResourceAsStream("/manhunt/lobby/island.nbt"));

		var lobbyWorld = server.getWorld(lobbyRegistryKey);

		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 0), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, 0), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, -15), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, -15), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 16), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 0), 16, Unit.INSTANCE);
		lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 16), 16, Unit.INSTANCE);
		placeStructure(lobbyWorld, new BlockPos(-21, 54, -54), lobbyParkourNbt);
		placeStructure(lobbyWorld, new BlockPos(-21, 54, -6), lobbyIslandNbt);
	}

	private void placeStructure(ServerWorld world, BlockPos pos, NbtCompound nbt) {
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

	public static void cycleTrackedPlayers(ServerPlayerEntity player, @Nullable NbtCompound itemStackNbt) {
		if (itemStackNbt != null && itemStackNbt.getBoolean("Tracker") && player.isTeamPlayer(player.getServer().getScoreboard().getTeam("hunters")) && !player.getItemCooldownManager().isCoolingDown(Items.COMPASS)) {
			if (!itemStackNbt.contains("Info")) {
				itemStackNbt.put("Info", new NbtCompound());
			}

			int next;
			int previous = -1;
			NbtCompound info = itemStackNbt.getCompound("Info");

			if (allRunners.isEmpty())
				player.sendMessage(Text.translatable("manhunt.tracker.norunners"));
			else {
				player.getItemCooldownManager().set(player.getMainHandStack().getItem(), 20);

				for (int i = 0; i < allRunners.size(); i++) {
					ServerPlayerEntity gamePlayer = allRunners.get(i);
					if (gamePlayer != null) {
						if (Objects.equals(gamePlayer.getName().getString(), info.getString("Name"))) {
							previous = i;
						}
					}
				}

				if (previous + 1 >= allRunners.size()) {
					next = 0;
				} else {
					next = previous + 1;
				}

				if (previous != next) {
					updateCompass(player, itemStackNbt, allRunners.get(next));
					player.sendMessage(Text.translatable("manhunt.tracker.switchrunner", allRunners.get(next).getName().getString()));
				}
			}
		}
	}

	public static void updateCompass(ServerPlayerEntity player, NbtCompound nbt, ServerPlayerEntity trackedPlayer) {
		nbt.remove("LodestonePos");
		nbt.remove("LodestoneDimension");

		nbt.put("Info", new NbtCompound());
		if (trackedPlayer.getScoreboardTeam() != null && Objects.equals(trackedPlayer.getScoreboardTeam().getName(), "runners")) {
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
			info.putString("Name", trackedPlayer.getEntityName());
			info.putString("Dimension", playerTag.getString("Dimension"));
		}
	}

	private static boolean hasItem(Item item, PlayerEntity player, String nbtBoolean) {
		boolean bool = false;
		for (ItemStack itemStack : player.getInventory().main) {
			if (itemStack.getItem().equals(item) && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Remove") && itemStack.getNbt().getBoolean(nbtBoolean)) {
				bool = true;
				break;
			}
		}

		if (player.playerScreenHandler.getCursorStack().getNbt() != null && player.playerScreenHandler.getCursorStack().getNbt().getBoolean(nbtBoolean)) {
			bool = true;
		} else if (player.getOffHandStack().hasNbt() && player.getOffHandStack().getNbt().getBoolean("Remove") && player.getOffHandStack().getNbt().getBoolean(nbtBoolean)) {
			bool = true;
		}
		return bool;
	}

	private void resetPlayer(PlayerEntity player, ServerWorld world) {
		parkourTimer.put(player.getUuid(), 0);
		startedParkour.put(player.getUuid(), false);
		startedParkour.put(player.getUuid(), false);
		player.getInventory().clear();
		updateGameMode((ServerPlayerEntity) player);
		player.teleport(world, 0.5, 63, 0, PositionFlag.ROT, 180, 0);
		player.clearStatusEffects();
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
	}

	private void showInfo(ServerPlayerEntity player, NbtCompound info) {
		String dim = info.getString("Dimension");
		String dimension = "";
		if (!info.contains("Dimension")) {
			dimension = "manhunt.world.unknown";
		} else if (Objects.equals(dim, "minecraft:overworld")) {
			dimension = "manhunt.world.overworld";
		} else if (Objects.equals(dim, "minecraft:the_nether")) {
			dimension = "manhunt.world.nether";
		} else if (Objects.equals(dim, "minecraft:the_end")) {
			dimension = "manhunt.world.end";
		}

		if (showDimensionInfo) {
			player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.translatable("manhunt.target.dimension", info.getString("Name"), Text.translatable(dimension))));
		} else {
			player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.translatable("manhunt.target.text", info.getString("Name"))));
		}
	}

	private boolean hasTracker(ServerPlayerEntity player) {
		boolean bool = false;
		for (ItemStack itemStack : player.getInventory().main) {
			if (itemStack.getItem().equals(Items.COMPASS) && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Remove") && itemStack.getNbt().getBoolean("Tracker")) {
				bool = true;
				break;
			}
		}

		if (player.playerScreenHandler.getCursorStack().getNbt() != null && player.playerScreenHandler.getCursorStack().getNbt().getBoolean("Tracker")) {
			bool = true;
		} else if (player.getOffHandStack().getNbt() != null && player.getOffHandStack().getNbt().getBoolean("Remove") && player.getOffHandStack().getNbt().getBoolean("Tracker")) {
			bool = true;
		}
		return bool;
	}

	private boolean holdingTracker(ServerPlayerEntity player) {
		boolean bool = false;
		if (player.getMainHandStack().getNbt() != null && player.getMainHandStack().getNbt().getBoolean("Tracker") && player.getMainHandStack().getNbt().getCompound("Info").contains("Name")) {
			bool = true;
		} else if (player.getOffHandStack().getNbt() != null && player.getOffHandStack().getNbt().getBoolean("Tracker") && player.getOffHandStack().getNbt().getCompound("Info").contains("Name")) {
			bool = true;
		}
		return bool;
	}

	private static void playSound(ServerPlayerEntity player, SoundEvent sound, float pitch) {
		player.playSound(sound, SoundCategory.BLOCKS, (float) 1.0, pitch);
	}

	public static void playLobbyMusic(ServerPlayerEntity player) {
		Song soChill = NBSDecoder.parse(new File(FabricLoader.getInstance().getGameDir().resolve("nbs") + "/" + "soChill.nbs"));
		RadioSongPlayer rsp = new RadioSongPlayer(soChill);
		rsp.setVolume(Byte.parseByte("20"));
		rsp.addPlayer(player);
		rsp.setPlaying(true);
		player.sendMessage(Text.translatable("manhunt.jukebox.mutelobbymusic"));
		player.sendMessage(Text.translatable("manhunt.jukebox.volume"));
	}

	private static void settings(ServerPlayerEntity player) {
		SimpleGui settings = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
		settings.setTitle(Text.translatable("manhunt.item.settings"));
		settings.open();
		List<Text> personalLore = new ArrayList<>();
		personalLore.add(Text.translatable("manhunt.lore.personal"));
		settings.setSlot(11, new GuiElementBuilder(Items.PAPER)
				.setName(Text.translatable("manhunt.item.personal"))
				.setLore(personalLore)
				.setCallback(() -> {
					personalSettings(player);
					player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.MASTER, 1f, 1f);
				})
		);
		List<Text> roleLore = new ArrayList<>();
		roleLore.add(Text.translatable("manhunt.lore.role"));
		settings.setSlot(13, new GuiElementBuilder(Items.WRITABLE_BOOK)
				.setName(Text.translatable("manhunt.item.role"))
				.setLore(roleLore)
				.setCallback(() -> {
					roleSelector(player);
					player.playSound(SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.MASTER, 1f, 1f);
				})
		);
		List<Text> gameLore = new ArrayList<>();
		gameLore.add(Text.translatable("manhunt.lore.game"));
		settings.setSlot(15, new GuiElementBuilder(Items.REPEATER)
				.setName(Text.translatable("manhunt.item.game"))
				.setLore(gameLore)
				.setCallback(() -> {
					gameSettings(player);
					player.playSound(SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.MASTER, 0.2f, 1f);
				})
		);
	}

	private static void personalSettings(ServerPlayerEntity player) {
		SimpleGui personalsettings = new SimpleGui(ScreenHandlerType.GENERIC_9X1, player, false);
		personalsettings.setTitle(Text.translatable("manhunt.item.personal"));
		setGoBack(player, personalsettings);
		changePersonalSetting(player, personalsettings, muteLobbyMusic, "manhunt.item.mutelobbymusic", "manhunt.lore.mutelobbymusic", Items.JUKEBOX, 0, SoundEvents.ENTITY_ITEM_PICKUP);
		changePersonalSetting(player, personalsettings, doNotDisturb, "manhunt.item.donotdisturb", "manhunt.lore.donotdisturb", Items.MUSIC_DISC_11, 1, SoundEvents.BLOCK_IRON_DOOR_CLOSE);
		personalsettings.open();
	}

	private static void roleSelector(ServerPlayerEntity player) {
		if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
			SimpleGui roleselector = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false);
			roleselector.setTitle(Text.translatable("manhunt.item.role"));
			if (gameLeader.get(player.getUuid())) {
				if (player.getServer().getCurrentPlayerCount() == 1) {
					changeRoleSelection(player, roleselector, 1, 0);
				}
				if (player.getServer().getCurrentPlayerCount() == 2) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
				}
				if (player.getServer().getCurrentPlayerCount() == 3) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
				}
				if (player.getServer().getCurrentPlayerCount() == 4) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
				}
				if (player.getServer().getCurrentPlayerCount() == 5) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
				}
				if (player.getServer().getCurrentPlayerCount() == 6) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
				}
				if (player.getServer().getCurrentPlayerCount() == 7) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
				}
				if (player.getServer().getCurrentPlayerCount() == 8) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
				}
				if (player.getServer().getCurrentPlayerCount() == 9) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
				}
				if (player.getServer().getCurrentPlayerCount() == 10) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
				}
				if (player.getServer().getCurrentPlayerCount() == 11) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
				}
				if (player.getServer().getCurrentPlayerCount() == 12) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
				}
				if (player.getServer().getCurrentPlayerCount() == 13) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
				}
				if (player.getServer().getCurrentPlayerCount() == 14) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
				}
				if (player.getServer().getCurrentPlayerCount() == 15) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
				}
				if (player.getServer().getCurrentPlayerCount() == 16) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
				}
				if (player.getServer().getCurrentPlayerCount() == 17) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
				}
				if (player.getServer().getCurrentPlayerCount() == 18) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
				}
				if (player.getServer().getCurrentPlayerCount() == 19) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
				}
				if (player.getServer().getCurrentPlayerCount() == 20) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
				}
				if (player.getServer().getCurrentPlayerCount() == 21) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
				}
				if (player.getServer().getCurrentPlayerCount() == 22) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
				}
				if (player.getServer().getCurrentPlayerCount() == 23) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
				}
				if (player.getServer().getCurrentPlayerCount() == 24) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
				}
				if (player.getServer().getCurrentPlayerCount() == 25) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
				}
				if (player.getServer().getCurrentPlayerCount() == 26) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
				}
				if (player.getServer().getCurrentPlayerCount() == 27) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
				}
				if (player.getServer().getCurrentPlayerCount() == 28) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
				}
				if (player.getServer().getCurrentPlayerCount() == 29) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
				}
				if (player.getServer().getCurrentPlayerCount() == 30) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
				}
				if (player.getServer().getCurrentPlayerCount() == 31) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
				}
				if (player.getServer().getCurrentPlayerCount() == 32) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
				}
				if (player.getServer().getCurrentPlayerCount() == 33) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
				}
				if (player.getServer().getCurrentPlayerCount() == 34) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
					changeRoleSelection(player, roleselector, 34, 41);
				}
				if (player.getServer().getCurrentPlayerCount() == 35) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
					changeRoleSelection(player, roleselector, 34, 41);
					changeRoleSelection(player, roleselector, 35, 42);
				}
				if (player.getServer().getCurrentPlayerCount() == 36) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
					changeRoleSelection(player, roleselector, 34, 41);
					changeRoleSelection(player, roleselector, 35, 42);
					changeRoleSelection(player, roleselector, 36, 43);
				}
				if (player.getServer().getCurrentPlayerCount() == 37) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
					changeRoleSelection(player, roleselector, 34, 41);
					changeRoleSelection(player, roleselector, 35, 42);
					changeRoleSelection(player, roleselector, 36, 43);
					changeRoleSelection(player, roleselector, 37, 44);
				}
				if (player.getServer().getCurrentPlayerCount() == 38) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
					changeRoleSelection(player, roleselector, 34, 41);
					changeRoleSelection(player, roleselector, 35, 42);
					changeRoleSelection(player, roleselector, 36, 43);
					changeRoleSelection(player, roleselector, 37, 44);
					changeRoleSelection(player, roleselector, 38, 45);
				}
				if (player.getServer().getCurrentPlayerCount() == 39) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
					changeRoleSelection(player, roleselector, 34, 41);
					changeRoleSelection(player, roleselector, 35, 42);
					changeRoleSelection(player, roleselector, 36, 43);
					changeRoleSelection(player, roleselector, 37, 44);
					changeRoleSelection(player, roleselector, 38, 45);
					changeRoleSelection(player, roleselector, 39, 46);
				}
				if (player.getServer().getCurrentPlayerCount() == 40) {
					changeRoleSelection(player, roleselector, 1, 0);
					changeRoleSelection(player, roleselector, 2, 1);
					changeRoleSelection(player, roleselector, 3, 2);
					changeRoleSelection(player, roleselector, 4, 3);
					changeRoleSelection(player, roleselector, 5, 4);
					changeRoleSelection(player, roleselector, 6, 5);
					changeRoleSelection(player, roleselector, 7, 6);
					changeRoleSelection(player, roleselector, 8, 7);
					changeRoleSelection(player, roleselector, 9, 10);
					changeRoleSelection(player, roleselector, 10, 11);
					changeRoleSelection(player, roleselector, 11, 12);
					changeRoleSelection(player, roleselector, 12, 13);
					changeRoleSelection(player, roleselector, 13, 14);
					changeRoleSelection(player, roleselector, 14, 15);
					changeRoleSelection(player, roleselector, 15, 16);
					changeRoleSelection(player, roleselector, 16, 17);
					changeRoleSelection(player, roleselector, 17, 20);
					changeRoleSelection(player, roleselector, 18, 21);
					changeRoleSelection(player, roleselector, 19, 22);
					changeRoleSelection(player, roleselector, 20, 23);
					changeRoleSelection(player, roleselector, 21, 24);
					changeRoleSelection(player, roleselector, 22, 25);
					changeRoleSelection(player, roleselector, 23, 26);
					changeRoleSelection(player, roleselector, 24, 27);
					changeRoleSelection(player, roleselector, 25, 30);
					changeRoleSelection(player, roleselector, 26, 31);
					changeRoleSelection(player, roleselector, 27, 32);
					changeRoleSelection(player, roleselector, 28, 33);
					changeRoleSelection(player, roleselector, 29, 34);
					changeRoleSelection(player, roleselector, 30, 35);
					changeRoleSelection(player, roleselector, 31, 36);
					changeRoleSelection(player, roleselector, 32, 37);
					changeRoleSelection(player, roleselector, 33, 40);
					changeRoleSelection(player, roleselector, 34, 41);
					changeRoleSelection(player, roleselector, 35, 42);
					changeRoleSelection(player, roleselector, 36, 43);
					changeRoleSelection(player, roleselector, 37, 44);
					changeRoleSelection(player, roleselector, 38, 45);
					changeRoleSelection(player, roleselector, 39, 46);
					changeRoleSelection(player, roleselector, 40, 47);
				}
				setGoBack(player, roleselector);
				roleselector.open();
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.player"));
			}
		}
	}

	private static void gameSettings(ServerPlayerEntity player) {
		if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
			SimpleGui gameSettings = new SimpleGui(ScreenHandlerType.GENERIC_9X2, player, false);
			gameSettings.setTitle(Text.translatable("manhunt.item.game"));
			if (gameLeader.get(player.getUuid())) {
				changeGameSetting(player, gameSettings, "presetSelection", "manhunt.item.presetselection", "manhunt.lore.presetselection", Items.COMMAND_BLOCK, 0, SoundEvents.BLOCK_IRON_DOOR_OPEN);
				changeGameSetting(player, gameSettings, "setRoles", "manhunt.item.setroles", "manhunt.lore.setroles", Items.FLETCHING_TABLE, 1, SoundEvents.ENTITY_VILLAGER_WORK_FLETCHER);
				changeGameSetting(player, gameSettings, "hunterFreeze", "manhunt.item.hunterfreeze", "manhunt.lore.hunterfreeze", Items.ICE, 2, SoundEvents.BLOCK_GLASS_BREAK);
				changeGameSetting(player, gameSettings, "timeLimit", "manhunt.item.timelimit", "manhunt.lore.timelimit", Items.CLOCK, 3, SoundEvents.ENTITY_FISHING_BOBBER_THROW);
				changeGameSetting(player, gameSettings, "compassUpdate", "manhunt.item.compassupdate", "manhunt.lore.compassupdate", Items.COMPASS, 4, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK);
				changeGameSetting(player, gameSettings, "showDimensionInfo", "manhunt.item.showdimensioninfo", "manhunt.lore.showdimensioninfo", Items.BOOK, 5, SoundEvents.ITEM_FLINTANDSTEEL_USE);
				changeGameSetting(player, gameSettings, "latePlayers", "manhunt.item.lateplayers", "manhunt.lore.lateplayers", Items.PLAYER_HEAD, 6, SoundEvents.ENTITY_PLAYER_BURP);
				changeGameSetting(player, gameSettings, "showTeamColor", "manhunt.item.showteamcolor", "manhunt.lore.showteamcolor", Items.LEATHER_CHESTPLATE, 9, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER);
				changeGameSetting(player, gameSettings, "disableBedExplosions", "manhunt.item.disablebedexplosions", "manhunt.lore.disablebedexplosions", Items.RED_BED, 10, SoundEvents.ENTITY_GENERIC_EXPLODE);
				changeGameSetting(player, gameSettings, "worldDifficulty", "manhunt.item.worlddifficulty", "manhunt.lore.worlddifficulty", Items.CREEPER_HEAD, 11, SoundEvents.ENTITY_CREEPER_HURT);
				changeGameSetting(player, gameSettings, "borderSize", "manhunt.item.bordersize", "manhunt.lore.bordersize", Items.STRUCTURE_VOID, 12, SoundEvents.BLOCK_DEEPSLATE_BREAK);
				changeGameSetting(player, gameSettings, "showGameTitles", "manhunt.item.showgametitles", "manhunt.lore.showgametitles", Items.OAK_SIGN, 13, SoundEvents.BLOCK_WOOD_BREAK);
				setGoBack(player, gameSettings);
				gameSettings.open();
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.player"));
			}
		}
	}

	private static void changePersonalSetting(ServerPlayerEntity player, SimpleGui gui, HashMap setting, String name, String lore, Item item, int slot, SoundEvent sound) {
		if (!player.getItemCooldownManager().isCoolingDown(item)) {
			boolean value = (boolean) setting.get(player.getUuid());
			List<Text> loreList = new ArrayList<>();
			loreList.add(Text.translatable(lore));
			if (value) {
				loreList.add(Text.literal("On").formatted(Formatting.GREEN));
			} else {
				loreList.add(Text.literal("Off").formatted(Formatting.RED));
			}
			gui.setSlot(slot, new GuiElementBuilder(item)
					.hideFlags()
					.setName(Text.translatable(name))
					.setLore(loreList)
					.setCallback(() -> {
						if (value) {
							setting.put(player.getUuid(), false);
							player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
							if (setting.equals(muteLobbyMusic)) {
								Nota.stopPlaying(player);
								playLobbyMusic(player);
							}
						} else {
							setting.put(player.getUuid(), true);
							player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
							if (setting.equals(muteLobbyMusic)) {
								Nota.stopPlaying(player);
							}
						}
						changePersonalSetting(player, gui, setting, name, lore, item, slot, sound);
						player.getItemCooldownManager().set(item, 20);
					})
			);
		}
	}

	private static void changeRoleSelection(ServerPlayerEntity player, SimpleGui gui, int count, int slot) {
		if (!player.getItemCooldownManager().isCoolingDown(Items.PLAYER_HEAD)) {
			ServerPlayerEntity listPlayer = player.getServer().getPlayerManager().getPlayerList().get(count - 1);
			String value = currentRole.get(listPlayer.getUuid());
			List<Text> loreList = new ArrayList<>();
			if (value.equals("hunter")) {
				loreList.add(Text.literal("Hunter").formatted(Formatting.RED));
			} else {
				loreList.add(Text.literal("Runner").formatted(Formatting.GREEN));
			}
			gui.setSlot(slot, new GuiElementBuilder(Items.PLAYER_HEAD)
					.setSkullOwner(listPlayer.getGameProfile(), player.getServer())
					.setName(Text.translatable(listPlayer.getName().getString()))
					.setLore(loreList)
					.setCallback(() -> {
						if (value.equals("runner")) {
							currentRole.put(listPlayer.getUuid(), "hunter");
							player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 1f, 0.5f);
							player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.role", listPlayer.getName().getString(), Text.literal("Hunter").formatted(Formatting.RED)), false);
						} else {
							currentRole.put(listPlayer.getUuid(), "runner");
							player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 1f, 1f);
							player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.role", listPlayer.getName().getString(), Text.literal("Runner").formatted(Formatting.GREEN)), false);
						}
						changeRoleSelection(player, gui, count, slot);
						player.getItemCooldownManager().set(Items.PLAYER_HEAD, 20);
					})
			);
		}
	}

	private static void changeGameSetting(ServerPlayerEntity player, SimpleGui gui, String setting, String name, String lore, Item item, int slot, SoundEvent sound) {
		if (!player.getItemCooldownManager().isCoolingDown(item)) {
			List<Text> loreList = new ArrayList<>();
			loreList.add(Text.translatable(lore));
			if (setting.equals("presetSelection")) {
				if (presetSelection.equals("Casual")) {
					loreList.add(Text.literal("Casual").formatted(Formatting.GREEN));
				} else {
					loreList.add(Text.literal("Competitive").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (presetSelection.equals("Casual")) {
									presetSelection = "Competitive";
									setRoles = "Free Select";
									hunterFreeze = 10;
									timeLimit = 0;
									compassUpdate = "Automatic";
									showDimensionInfo = true;
									latePlayers = "Join Hunters";
									showTeamColor = true;
									disableBedExplosions = true;
									worldDifficulty = "Easy";
									borderSize = 59999968;
									showGameTitles = true;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Preset Selection", Text.literal("Competitive").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
								} else {
									presetSelection = "Casual";
									setRoles = "Free Select";
									hunterFreeze = 0;
									timeLimit = 180;
									compassUpdate = "Manual";
									showDimensionInfo = false;
									latePlayers = "Join Hunters";
									showTeamColor = false;
									disableBedExplosions = false;
									worldDifficulty = "Hard";
									borderSize = 59999968;
									showGameTitles = false;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Preset Selection", Text.literal("Casual").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("setRoles")) {
				switch (setRoles) {
					case "Free Select" -> loreList.add(Text.literal(setRoles).formatted(Formatting.GREEN));
					case "All Hunters" -> loreList.add(Text.literal(setRoles).formatted(Formatting.YELLOW));
					default -> loreList.add(Text.literal(setRoles).formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								NbtCompound nbt = new NbtCompound();
								nbt.putBoolean("Remove", true);
								ItemStack itemStack = new ItemStack(Items.BARRIER);
								itemStack.setNbt(nbt);
								for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
									serverPlayer.getInventory().setStack(3, itemStack);
									serverPlayer.getInventory().setStack(5, itemStack);
								}
								switch (setRoles) {
									case "Free Select" -> {
										setRoles = "All Hunters";
										player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Preset Mode", Text.literal("All Hunters").formatted(Formatting.YELLOW)), false);
										player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
										for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
											currentRole.put(serverPlayer.getUuid(), "hunter");
										}
									}
									case "All Hunters" -> {
										setRoles = "All Runners";
										player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Preset Mode", Text.literal("All Runners").formatted(Formatting.RED)), false);
										player.playSound(sound, SoundCategory.MASTER, 1f, 1.5f);
										for (ServerPlayerEntity serverPlayer : player.getServer().getPlayerManager().getPlayerList()) {
											currentRole.put(serverPlayer.getUuid(), "runner");
										}
									}
									default -> {
										setRoles = "Free Select";
										player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Preset Mode", Text.literal("Free Select").formatted(Formatting.GREEN)), false);
										player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
									}
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("hunterFreeze")) {
				if (hunterFreeze == 0) {
					loreList.add(Text.literal(hunterFreeze + " seconds (disabled)").formatted(Formatting.RED));
				} else {
					loreList.add(Text.literal(hunterFreeze + " seconds").formatted(Formatting.GREEN));
				}
			}
			if (setting.equals("timeLimit")) {
				if (timeLimit == 0) {
					loreList.add(Text.literal(timeLimit + " minutes (disabled)").formatted(Formatting.RED));
				} else {
					loreList.add(Text.literal(timeLimit + " minutes").formatted(Formatting.GREEN));
				}
			}
			if (setting.equals("borderSize")) {
				if (borderSize == 59999968) {
					loreList.add(Text.literal(borderSize + " blocks (maximum)").formatted(Formatting.RED));
				} else {
					loreList.add(Text.literal(borderSize + " blocks").formatted(Formatting.GREEN));
				}
			}
			if (setting.equals("hunterFreeze") || setting.equals("timeLimit") || setting.equals("borderSize")) {
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								AnvilInputGui inputGui = new AnvilInputGui(player, false) {
									@Override
									public void onInput(String input) {
										this.setSlot(2, new GuiElementBuilder(Items.PAPER)
												.setName(Text.literal(input).formatted(Formatting.ITALIC))
												.setCallback(() -> {
													try {
														int value = Integer.parseInt(input);
														if (setting.equals("hunterFreeze")) {
															hunterFreeze = value;
															if (hunterFreeze == 0) {
																player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Hunter Freeze", Text.literal(value + " seconds (disabled)").formatted(Formatting.RED)), false);
																player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
															} else {
																player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Hunter Freeze", Text.literal(value + " seconds").formatted(Formatting.GREEN)), false);
																player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
															}
														}
														if (setting.equals("timeLimit")) {
															timeLimit = value;
															if (timeLimit == 0) {
																player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Time Limit", Text.literal(value + " minutes (disabled)").formatted(Formatting.RED)), false);
																player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
															} else {
																player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Time Limit", Text.literal(value + " minutes").formatted(Formatting.GREEN)), false);
																player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
															}
														}
														if (setting.equals("borderSize")) {
															borderSize = value;
															if (borderSize == 0 || borderSize >= 59999968) {
																borderSize = 59999968;
																player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Border Size", Text.literal(borderSize + " blocks (maximum)").formatted(Formatting.RED)), false);
																player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
															} else {
																player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Border Size", Text.literal(borderSize + " blocks").formatted(Formatting.GREEN)), false);
																player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
															}
														}
													} catch (NumberFormatException e) {
														player.sendMessage(Text.translatable("manhunt.chat.invalid"));
													}
													gameSettings(player);
													player.getItemCooldownManager().set(item, 20);
												})
										);
									}
								};
								inputGui.setTitle(Text.translatable("manhunt.lore.value"));
								inputGui.setSlot(0, new GuiElementBuilder(Items.PAPER));
								inputGui.setDefaultInputValue("");
								inputGui.open();
								save();
							}
						})
				);
			}
			if (setting.equals("compassUpdate")) {
				if (compassUpdate.equals("Automatic")) {
					loreList.add(Text.literal("Automatic").formatted(Formatting.GREEN));
				} else {
					loreList.add(Text.literal("Manual").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (compassUpdate.equals("Automatic")) {
									compassUpdate = "Manual";
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Compass Update", Text.literal("Manual").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
								} else {
									compassUpdate = "Automatic";
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Compass Update", Text.literal("Automatic").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("showDimensionInfo")) {
				if (showDimensionInfo) {
					loreList.add(Text.literal("Show").formatted(Formatting.GREEN));
				} else {
					loreList.add(Text.literal("Hide").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (showDimensionInfo) {
									showDimensionInfo = false;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Dimension Info", Text.literal("Hide").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
								} else {
									showDimensionInfo = true;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Dimension Info", Text.literal("Show").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("latePlayers")) {
				List<Text> loreListSecond = new ArrayList<>();
				loreListSecond.add(Text.translatable(lore));
				if (latePlayers.equals("Join Hunters")) {
					loreListSecond.add(Text.literal("Join Hunters").formatted(Formatting.GREEN));
				} else {
					loreListSecond.add(Text.literal("Join Spectators").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreListSecond)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (latePlayers.equals("Join Hunters")) {
									latePlayers = "Join Spectators";
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Late Players", Text.literal("Join Spectators").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
								} else {
									latePlayers = "Join Hunters";
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Late Players", Text.literal("Join Hunters").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("showTeamColor")) {
				if (showTeamColor) {
					loreList.add(Text.literal("Show").formatted(Formatting.GREEN));
				} else {
					loreList.add(Text.literal("Hide").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (showTeamColor) {
									showTeamColor = false;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Team Color", Text.literal("Hide").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
								} else {
									showTeamColor = true;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Team Color", Text.literal("Show").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("disableBedExplosions")) {
				if (disableBedExplosions) {
					loreList.add(Text.literal("Enabled").formatted(Formatting.GREEN));
				} else {
					loreList.add(Text.literal("Disabled").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (disableBedExplosions) {
									disableBedExplosions = false;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Bed Explosions", Text.literal("Disable").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 0.2f, 0.5f);
								} else {
									disableBedExplosions = true;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Bed Explosions", Text.literal("Enable").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 0.2f, 1f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("worldDifficulty")) {
				if (worldDifficulty.equals("easy")) {
					loreList.add(Text.literal("Easy").formatted(Formatting.GREEN));
				} else if (worldDifficulty.equals("normal")) {
					loreList.add(Text.literal("Normal").formatted(Formatting.GOLD));
				} else {
					loreList.add(Text.literal("Hard").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (worldDifficulty.equals("easy")) {
									worldDifficulty = "normal";
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "World Difficulty", Text.literal("Normal").formatted(Formatting.GOLD)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
								} else if (worldDifficulty.equals("normal")) {
									worldDifficulty = "hard";
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "World Difficulty", Text.literal("Hard").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 0.8f);
								} else {
									worldDifficulty = "easy";
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "World Difficulty", Text.literal("Easy").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1.2f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
			if (setting.equals("showGameTitles")) {
				if (showGameTitles) {
					loreList.add(Text.literal("Show").formatted(Formatting.GREEN));
				} else {
					loreList.add(Text.literal("Hide").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreList)
						.setCallback(() -> {
							if (!player.getItemCooldownManager().isCoolingDown(item)) {
								if (showGameTitles) {
									showGameTitles = false;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Game Titles", Text.literal("Hide").formatted(Formatting.RED)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 0.5f);
								} else {
									showGameTitles = true;
									player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "Game Titles", Text.literal("Show").formatted(Formatting.GREEN)), false);
									player.playSound(sound, SoundCategory.MASTER, 1f, 1f);
								}
								changeGameSetting(player, gui, setting, name, lore, item, slot, sound);
								player.getItemCooldownManager().set(item, 20);
								save();
							}
						})
				);
			}
		}
	}

	private static void setGoBack(ServerPlayerEntity player, SimpleGui gui) {
		gui.setSlot(8, new GuiElementBuilder(Items.MAGENTA_GLAZED_TERRACOTTA)
				.setName(Text.translatable("manhunt.item.back"))
				.setCallback((index, type, action) -> {
					player.playSound(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.MASTER, 1f, 1f);
					settings(player);
				})
		);
	}

	public static GameState gameState;

	public static void gameState(GameState gameState, MinecraftServer server) {
		server.setMotd(gameState.getColor() + "[" + gameState.getMotd() + "]§f Minecraft MANHUNT");
		Manhunt.gameState = gameState;
	}

	public static void startGame(MinecraftServer server) {
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "team remove players");
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky cancel");
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky confirm");

		server.setFlightEnabled(false);
		server.getPlayerManager().setWhitelistEnabled(false);

		gameState(PLAYING, server);

		var world = server.getOverworld();
		world.setTimeOfDay(0);

		server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
		server.getGameRules().get(GameRules.DO_FIRE_TICK).set(true, server);
		server.getGameRules().get(GameRules.DO_INSOMNIA).set(true, server);
		server.getGameRules().get(GameRules.DO_MOB_LOOT).set(true, server);
		server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
		server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(true, server);
		server.getGameRules().get(GameRules.FALL_DAMAGE).set(true, server);
		server.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(3, server);
		server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
		server.getGameRules().get(GameRules.SPAWN_RADIUS).set(10, server);

		server.setPvpEnabled(true);

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.teleport(world, world.getSpawnPos().getX(), world.getSpawnPos().getY(), world.getSpawnPos().getZ(), 0, 0);
			player.clearStatusEffects();
			player.getInventory().clear();
			player.setFireTicks(0);
			player.setOnFire(false);
			player.setHealth(20);
			player.getHungerManager().setFoodLevel(20);
			player.getHungerManager().setSaturationLevel(5);
			player.getHungerManager().setExhaustion(0);

			for (AdvancementEntry advancement : server.getAdvancementLoader().getAdvancements()) {
				AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
				for(String criteria : progress.getObtainedCriteria()) {
					player.getAdvancementTracker().revokeCriterion(advancement, criteria);
				}
			}

			updateGameMode(player);

			if (showGameTitles) {
				player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.gamemode")));
				player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.start")));
			}
			player.networkHandler.sendPacket(
					new PlaySoundS2CPacket(
							SoundEvents.BLOCK_NOTE_BLOCK_PLING,
							SoundCategory.BLOCKS,
							player.getX(),
							player.getY(),
							player.getZ(),
							0.5f, 2f, 0
					)
			);

			if (player.isTeamPlayer(player.getScoreboard().getTeam("hunters"))) {
				if (hunterFreeze != 0) {
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, hunterFreeze * 20, 255, false, true));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, hunterFreeze * 20, 255, false, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, hunterFreeze * 20, 248, false, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (hunterFreeze - 1) * 20, 255, false, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, hunterFreeze * 20, 255, false, false));
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, hunterFreeze * 20, 255, false, false));
				}
			}
			Nota.stopPlaying(player);

			if (hunterFreeze == 0) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.hunterfreeze"), Text.literal(hunterFreeze + " seconds (disabled)").formatted(Formatting.RED)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.hunterfreeze"), Text.literal(hunterFreeze + " seconds").formatted(Formatting.GREEN)), false);
			}
			if (timeLimit == 0) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.timelimit"), Text.literal(timeLimit + " minutes (disabled)").formatted(Formatting.RED)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.timelimit"), Text.literal(timeLimit + " minutes").formatted(Formatting.GREEN)), false);
			}
			if (compassUpdate.equals("Automatic")) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.compassupdate"), Text.literal("Automatic").formatted(Formatting.GREEN)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.compassupdate"), Text.literal("Manual").formatted(Formatting.RED)), false);
			}
			if (showDimensionInfo) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.showdimensioninfo"), Text.literal("Show").formatted(Formatting.GREEN)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.showdimensioninfo"), Text.literal("Hide").formatted(Formatting.RED)), false);
			}
			if (latePlayers.equals("Join Hunters")) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.lateplayers"), Text.literal("Join Hunters").formatted(Formatting.GREEN)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.lateplayers"), Text.literal("Join Spectators").formatted(Formatting.RED)), false);
			}
			if (showTeamColor) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.showteamcolor"), Text.literal("Show").formatted(Formatting.GREEN)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.showteamcolor"), Text.literal("Hide").formatted(Formatting.RED)), false);
			}
			if (disableBedExplosions) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.disablebedexplosions"), Text.literal("Enabled").formatted(Formatting.GREEN)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.disablebedexplosions"), Text.literal("Disabled").formatted(Formatting.RED)), false);
			}
			if (worldDifficulty.equals("easy")) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.worlddifficulty"), Text.literal("Easy").formatted(Formatting.GREEN)), false);
			} else if (worldDifficulty.equals("normal")) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.worlddifficulty"), Text.literal("Normal").formatted(Formatting.GOLD)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.worlddifficulty"), Text.literal("Hard").formatted(Formatting.RED)), false);
			}
			if (borderSize == 59999968) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.bordersize"), Text.literal(borderSize + " blocks (maximum)").formatted(Formatting.RED)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.bordersize"), Text.literal(borderSize + " blocks").formatted(Formatting.GREEN)), false);
			}
			if (showGameTitles) {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.showgametitles"), Text.literal("Show").formatted(Formatting.GREEN)), false);
			} else {
				player.sendMessage(Text.translatable("manhunt.chat.game", Text.translatable("manhunt.item.showgametitles"), Text.literal("Hide").formatted(Formatting.RED)), false);
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
}