package manhunt;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import manhunt.commands.DoNotDisturbCommand;
import manhunt.commands.JukeboxCommand;
import manhunt.commands.PingSoundCommand;
import manhunt.config.ManhuntConfig;
import manhunt.game.ManhuntGame;
import manhunt.game.ManhuntState;
import manhunt.util.DeleteWorld;
import mrnavastar.sqlib.DataContainer;
import mrnavastar.sqlib.Table;
import mrnavastar.sqlib.database.MySQLDatabase;
import mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
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
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import nota.Nota;
import nota.model.Playlist;
import nota.model.Song;
import nota.player.RadioSongPlayer;
import nota.utils.NBSDecoder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static manhunt.config.ManhuntConfig.*;
import static manhunt.game.ManhuntState.*;

public class Manhunt implements ModInitializer {
	public static final String MOD_ID = "manhunt";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier LOBBY_WORLD_ID = new Identifier("manhunt", "lobby");
	public static RegistryKey<World> lobbyRegistryKey = RegistryKey.of(RegistryKeys.WORLD, LOBBY_WORLD_ID);
	public static List<ServerPlayerEntity> allPlayers;
	public static List<ServerPlayerEntity> allRunners;
	public static List<String> songs = new ArrayList<>();
	public static String africa = "africa";
	public static String bohemianRapsody = "bohemianRapsody";
	public static String californiaDreamin = "californiaDreamin";
	public static String callMeMaybe = "callMeMaybe";
	public static String countingStars = "countingStars";
	public static String djGotUsFallinInLove = "djGotUsFallinInLove";
	public static String dontStopMeNow = "dontStopMeNow";
	public static String duelOfTheFates = "duelOfTheFates";
	public static String dynamite = "dynamite";
	public static String elevatorMusic = "elevatorMusic";
	public static String everythingIsAwesome = "everythingIsAwesome";
	public static String flightOfTheBumblebee = "flightOfTheBumblebee";
	public static String fnafSong = "fnafSong";
	public static String hesAPirate = "hesAPirate";
	public static String heySoulSister = "heySoulSister";
	public static String iGotAFeeling = "iGotAFeeling";
	public static String indianaJones = "indianaJones";
	public static String inTheHallOfTheMountainKing = "inTheHallOfTheMountainKing";
	public static String jurassicPark = "jurassicPark";
	public static String localForecast = "localForecast";
	public static String madWorld = "madWorld";
	public static String mrBlueSky = "mrBlueSky";
	public static String neverGonnaGiveYouUp = "neverGonnaGiveYouUp";
	public static String neverLetMeDownAgain = "neverLetMeDownAgain";
	public static String nyanCat = "nyanCat";
	public static String paradise = "paradise";
	public static String payphone = "payphone";
	public static String rude = "rude";
	public static String smellsLikeTeenSpirit = "smellsLikeTeenSpirit";
	public static String soChill = "soChill";
	public static String somebodyThatIUsedToKnow = "somebodyThatIUsedToKnow";
	public static String spookyScarySkeletons = "spookyScarySkeletons";
	public static String starWars = "starWars";
	public static String takeMeToChurch = "takeMeToChurch";
	public static String takeOnMe = "takeOnMe";
	public static String theSpectre = "theSpectre";
	public static String thriller = "thriller";
	public static String thunderstruck = "thunderstruck";
	public static String vivaLaVida = "vivaLaVida";
	public static String waitingForLove = "waitingForLove";
	private long lastDelay = System.currentTimeMillis();
	private boolean holding;

	@Override
	public void onInitialize() {
		ManhuntConfig.load();

		LOGGER.info("Manhunt initialized");

		DeleteWorld.invoke();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			JukeboxCommand.register(dispatcher);
			DoNotDisturbCommand.register(dispatcher);
			PingSoundCommand.register(dispatcher);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			server.getPlayerManager().setWhitelistEnabled(true);

			ManhuntGame.state(PREGAME, server);

			if (!pregenerationEnabled) {
				server.getPlayerManager().setWhitelistEnabled(false);
			}

			if (pregenerationEnabled) {
				ManhuntGame.state(PREPARING, server);

				server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky cancel");
				server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky confirm");
				server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky start overworld square 0 0 512 512");
				server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky start the_nether square 0 0 256 256");
				server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "chunky start the_end square 0 0 128 128");
			}

			var difficulty = switch (worldDifficulty) {
				case "normal" -> Difficulty.NORMAL;
				case "hard" -> Difficulty.HARD;
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

			server.getScoreboard().addTeam("readys");
			server.getScoreboard().addTeam("hunters");
			server.getScoreboard().addTeam("runners");

			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add playerData dummy");
			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add muteMusic dummy");
			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add muteLobbyMusic dummy");
			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add doNotDisturb dummy");
			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add currentRole dummy");
			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add parkourTimer dummy");
			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add hasStarted dummy");
			server.getCommandManager().executeWithPrefix(server.getCommandSource().withSilent(), "scoreboard objectives add isFinished dummy");

			try {
				spawnLobbyStructure(server);
			} catch (IOException e) {
				LOGGER.info("Manhunt failed to spawn lobby");
			}

			songs.add(africa);
			songs.add(bohemianRapsody);
			songs.add(californiaDreamin);
			songs.add(callMeMaybe);
			songs.add(countingStars);
			songs.add(djGotUsFallinInLove);
			songs.add(dontStopMeNow);
			songs.add(duelOfTheFates);
			songs.add(dynamite);
			songs.add(elevatorMusic);
			songs.add(everythingIsAwesome);
			songs.add(flightOfTheBumblebee);
			songs.add(fnafSong);
			songs.add(hesAPirate);
			songs.add(heySoulSister);
			songs.add(iGotAFeeling);
			songs.add(indianaJones);
			songs.add(inTheHallOfTheMountainKing);
			songs.add(jurassicPark);
			songs.add(localForecast);
			songs.add(madWorld);
			songs.add(mrBlueSky);
			songs.add(neverGonnaGiveYouUp);
			songs.add(neverLetMeDownAgain);
			songs.add(nyanCat);
			songs.add(paradise);
			songs.add(payphone);
			songs.add(rude);
			songs.add(smellsLikeTeenSpirit);
			songs.add(soChill);
			songs.add(somebodyThatIUsedToKnow);
			songs.add(spookyScarySkeletons);
			songs.add(starWars);
			songs.add(takeMeToChurch);
			songs.add(takeOnMe);
			songs.add(theSpectre);
			songs.add(thriller);
			songs.add(thunderstruck);
			songs.add(vivaLaVida);
			songs.add(waitingForLove);
		});

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			if (ManhuntGame.state == PREGAME) {
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

					if (!hasItem(Items.RECOVERY_COMPASS, player, "Hunter")) {
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

					if (!hasItem(Items.CLOCK, player, "Runner")) {
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
						int ticks = getPlayerScore(player, "parkourTimer").getScore();
						if (getPlayerScore(player, "hasStarted").getScore() == 0 && getPlayerScore(player, "isFinished").getScore() == 0 && player.getZ() < -4 && !(player.getZ() < -6)) {
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.BLOCKS, 1f, 1f);
							getPlayerScore(player, "hasStarted").setScore(1);
						}
						if (getPlayerScore(player, "hasStarted").getScore() == 1 && player.getZ() < -4) {
							getPlayerScore(player, "parkourTimer").setScore(getPlayerScore(player, "parkourTimer").getScore() + 1);
						}
						int sec = (int) Math.floor(((double) (ticks % (20 * 60)) / 20));
						String sec_string;
						if (sec < 10) {
							sec_string = "0" + sec;
						} else {
							sec_string = String.valueOf(sec);
						}
						int ms = (int) Math.floor(((double) (ticks * 5) % 100));
						String ms_string;
						if (ms < 10) {
							ms_string = "0" + ms;
						} else if (ms > 99) {
							ms_string = "00";
						} else {
							ms_string = String.valueOf(ms);
						}
						if (player.getZ() < -4 && getPlayerScore(player, "isFinished").getScore() == 0)
							player.sendMessage(Text.translatable("manhunt.time.current", sec_string, ms_string), true);
						if (player.getZ() < -24 && player.getZ() > -27) {
							if (player.getX() < -6) {
								if (player.getY() >= 70 && player.getY() < 72 && getPlayerScore(player, "hasStarted").getScore() == 1 && getPlayerScore(player, "isFinished").getScore() == 0) {
									player.sendMessage(Text.translatable("manhunt.time.current", sec_string, ms_string).formatted(Formatting.GREEN), true);
									playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.BLOCKS, 1f, 2f);
									getPlayerScore(player, "isFinished").setScore(1);
								}
							}
						}
						if (getPlayerScore(player, "hasStarted").getScore() == 1 && player.getZ() > -4) {
							player.sendMessage(Text.translatable("manhunt.time.current", sec_string, ms_string).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.BLOCKS, 1f, 0.5f);
						}
						if (player.getY() < 61 && !player.hasPermissionLevel(2)) {
							player.sendMessage(Text.translatable("manhunt.time.current", sec_string, ms_string).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.BLOCKS, 1f, 0.5f);
						}
						if (player.getZ() < -27 && player.getY() < 68 && !player.hasPermissionLevel(2)) {
							player.sendMessage(Text.translatable("manhunt.time.current", sec_string, ms_string).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							playSound(player, SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.BLOCKS, 1f, 0.5f);
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
				}
			}

			if (ManhuntGame.state == PLAYING) {
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
			}
		});

		ServerTickEvents.START_WORLD_TICK.register(world -> {
			if (pregenerationEnabled) {
				if (ManhuntGame.state == PREPARING) {
					if (world.getTime() >= 1000) {
						world.getServer().getPlayerManager().setWhitelistEnabled(false);
						ManhuntGame.state(ManhuntState.PREGAME, world.getServer());
					}
				}
			}

			if (ManhuntGame.state == PLAYING) {
				allPlayers = world.getServer().getPlayerManager().getPlayerList();
				allRunners = new LinkedList<>();

				Team hunters = world.getScoreboard().getTeam("hunters");
				Team runners = world.getScoreboard().getTeam("runners");

				if (showTeamColor) {
					hunters.setColor(Formatting.RED);
					runners.setColor(Formatting.GREEN);
				}

				for (ServerPlayerEntity player : allPlayers) {
					if (player != null) {
						if (player.isTeamPlayer(runners)) {
							allRunners.add(player);
						}
						if (!player.isTeamPlayer(hunters) && !player.isTeamPlayer(runners)) {
							if (getPlayerScore(player, "currentRole").getScore() == 0) {
								player.getScoreboard().addPlayerToTeam(player.getName().getString(), hunters);
							} else {
								player.getScoreboard().addPlayerToTeam(player.getName().getString(), runners);
							}
						}
					}
				}
			}
		});

		UseItemCallback.EVENT.register((player, world, hand) -> {
			var lobbyWorld = world.getServer().getWorld(lobbyRegistryKey);
			var itemStack = player.getStackInHand(hand);

			var readys = world.getScoreboard().getTeam("readys");
			var runners = world.getScoreboard().getTeam("runners");

			if (ManhuntGame.state == PREGAME) {
				if (itemStack.getItem() == Items.RED_CONCRETE) {
					if (itemStack.getNbt().getBoolean("NotReady")) {
						if (!player.getItemCooldownManager().isCoolingDown(itemStack.getItem()) && !player.getItemCooldownManager().isCoolingDown(Items.LIME_CONCRETE)) {
							player.getScoreboard().addPlayerToTeam(player.getName().getString(), readys);

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

							if (lobbyWorld.getScoreboard().getTeam("readys").getPlayerList().size() == lobbyWorld.getPlayers().size()) {
								for (ServerPlayerEntity lobbyPlayer : lobbyWorld.getPlayers()) {
									if (getPlayerScore((ServerPlayerEntity) player, "currentRole").getScore() == 1) {
										lobbyPlayer.getScoreboard().addPlayerToTeam(lobbyPlayer.getName().getString(), runners);
										if (!lobbyWorld.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
											ManhuntGame.start(lobbyWorld.getServer());
										}
									}
									if (lobbyWorld.getScoreboard().getTeam("runners").getPlayerList().isEmpty()) {
										player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.minimum"), false);
									}
								}
							}

							player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.ready", player.getName().getString(), readys.getPlayerList().size(), lobbyWorld.getPlayers().size()), false);
						}
					}
				}

				if (itemStack.getItem() == Items.LIME_CONCRETE) {
					if (itemStack.getNbt().getBoolean("Ready")) {
						if (!player.getItemCooldownManager().isCoolingDown(itemStack.getItem()) && !player.getItemCooldownManager().isCoolingDown(Items.RED_CONCRETE)) {
							lobbyWorld.getScoreboard().removePlayerFromTeam(player.getName().getString(), readys);

							NbtCompound nbt = new NbtCompound();
							nbt.putBoolean("Remove", true);
							nbt.putBoolean("NotReady", true);
							nbt.putInt("HideFlags", 1);
							nbt.put("display", new NbtCompound());
							nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.unready\",\"italic\": false,\"color\": \"white\"}");

							ItemStack item = new ItemStack(Items.RED_CONCRETE);
							item.setNbt(nbt);

							player.getInventory().setStack(0, item);

							player.getItemCooldownManager().set(item.getItem(), 20);

							player.playSound(SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 0.5f);

							player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.unready", player.getName().getString(), readys.getPlayerList().size(), lobbyWorld.getPlayers().size()), false);
						}
					}
				}

				if (itemStack.getItem() == Items.RECOVERY_COMPASS) {
					if (itemStack.getNbt().getBoolean("Hunter")) {
						if (!player.getItemCooldownManager().isCoolingDown(itemStack.getItem()) && !player.getItemCooldownManager().isCoolingDown(Items.CLOCK)) {
							player.getItemCooldownManager().set(itemStack.getItem(), 20);

							NbtCompound nbt = new NbtCompound();
							nbt.putBoolean("Remove", true);
							nbt.putBoolean("Runner", true);
							nbt.putInt("HideFlags", 1);
							nbt.put("display", new NbtCompound());
							nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.runner\",\"italic\": false,\"color\": \"white\"}");

							ItemStack item = new ItemStack(Items.CLOCK);
							item.setNbt(nbt);

							player.getInventory().setStack(5, item);

							itemStack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

							player.playSound(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 0.5f, 1f);

							getPlayerScore((ServerPlayerEntity) player, "currentRole").setScore(0);

							player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.hunter", player.getName().getString()), false);
						}
					}
				}

				if (itemStack.getItem() == Items.CLOCK) {
					if (itemStack.getNbt().getBoolean("Runner")) {
						if (!player.getItemCooldownManager().isCoolingDown(itemStack.getItem()) && !player.getItemCooldownManager().isCoolingDown(Items.RECOVERY_COMPASS)) {
							player.getItemCooldownManager().set(itemStack.getItem(), 20);

							NbtCompound nbt = new NbtCompound();
							nbt.putBoolean("Remove", true);
							nbt.putBoolean("Hunter", true);
							nbt.putInt("HideFlags", 1);
							nbt.put("display", new NbtCompound());
							nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.item.hunter\",\"italic\": false,\"color\": \"white\"}");

							ItemStack item = new ItemStack(Items.RECOVERY_COMPASS);
							item.setNbt(nbt);

							player.getInventory().setStack(3, item);

							itemStack.addEnchantment(Enchantments.VANISHING_CURSE, 1);

							player.playSound(SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 0.5f, 1f);

							getPlayerScore((ServerPlayerEntity) player, "currentRole").setScore(1);

							player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.runner", player.getName().getString()), false);
						}
					}
				}

				if (itemStack.getItem() == Items.COMPARATOR) {
					if (itemStack.getNbt().getBoolean("Settings")) {
						settings((ServerPlayerEntity) player);
					}
				}
			}

			if (ManhuntGame.state == PLAYING) {
				if (itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker") && !player.isSpectator() && player.isTeamPlayer(world.getScoreboard().getTeam("hunters")) && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
					player.getItemCooldownManager().set(itemStack.getItem(), 20);
					if (!itemStack.getOrCreateNbt().contains("Info")) {
						itemStack.getOrCreateNbt().put("Info", new NbtCompound());
					}
					NbtCompound info = itemStack.getOrCreateNbt().getCompound("Info");

					if (!info.contains("Name", NbtElement.STRING_TYPE) && !Manhunt.allRunners.isEmpty()) {
						info.putString("Name", Manhunt.allRunners.get(0).getName().getString());
					}

					ServerPlayerEntity trackedPlayer = world.getServer().getPlayerManager().getPlayer(info.getString("Name"));

					if (trackedPlayer != null) {
						player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.5f, 1f);
						updateCompass((ServerPlayerEntity) player, itemStack.getOrCreateNbt(), trackedPlayer);
					}
				}
			}

			return TypedActionResult.pass(itemStack);
		});

		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			if (pingingEnabled) {
				for (ServerPlayerEntity player : sender.getServer().getPlayerManager().getPlayerList()) {
					var playerName = player.getName().getString();
					if (getPlayerScore(player, "doNotDisturb").getScore() == 0) {
						if (message.getSignedContent().contains(playerName)) {
							String pingSound = getPlayerData(player).getString("pingSound");
							if (pingSound.isEmpty()) {
								player.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, 1f);
							} else {
								SoundEvent event = Registries.SOUND_EVENT.get(Identifier.tryParse(pingSound));
								player.playSound(event, SoundCategory.PLAYERS, 1f, 1f);
							}
							player.sendMessage(Text.translatable("manhunt.pingsound.pinged", Text.translatable(sender.getName().getString())));
						}
					}
				}
			}
		});
	}

	private void spawnLobbyStructure(MinecraftServer server) throws IOException {
		var lobbyDir = FabricLoader.getInstance().getConfigDir().resolve("lobby");
		if (!Files.exists(lobbyDir)) {
			lobbyDir.toFile().mkdirs();
		}

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

	public static DataContainer getPlayerData(ServerPlayerEntity player) {
		MySQLDatabase database = new MySQLDatabase(MOD_ID, databaseName, databaseAddress, databasePort, databaseUser, databasePassword);
		Table table = database.createTable("players")
				.addColumn("muteMusic", SQLDataType.BOOL)
				.addColumn("muteLobbyMusic", SQLDataType.BOOL)
				.addColumn("doNotDisturb", SQLDataType.BOOL)
				.addColumn("pingSound", SQLDataType.STRING)
				.addColumn("lobbyRole", SQLDataType.STRING)
				.finish();
		DataContainer playerData = table.get(player.getUuidAsString());
		if (!(getPlayerScore(player, "playerData").getScore() == 0) && !(getPlayerScore(player, "playerData").getScore() == 1)) {
			if (table.get(player.getUuidAsString()) == null) {
				playerData = table.createDataContainer(player.getUuidAsString());
			}
		}
		return playerData;
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
		getPlayerScore((ServerPlayerEntity) player, "parkourTimer").setScore(0);
		getPlayerScore((ServerPlayerEntity) player, "hasStarted").setScore(0);
		getPlayerScore((ServerPlayerEntity) player, "isFinished").setScore(0);
		player.teleport(world, 0.5, 63, 0, PositionFlag.ROT, 180, 0);
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

		if (showRunnerDimension) {
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

	public static ScoreboardPlayerScore getPlayerScore(ServerPlayerEntity player, String name) {
		return player.getScoreboard().getPlayerScore(player.getName().getString(), player.getScoreboard().getNullableObjective(name));
	}

	private static void playSound(ServerPlayerEntity player, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		player.playSound(sound, category, volume, pitch);
	}

	public static void playLobbyMusic(ServerPlayerEntity player) {
		Song elevatorMusic = NBSDecoder.parse(new File(musicDirectory + "/" + "elevatorMusic.nbs"));
		Song localForecast = NBSDecoder.parse(new File(musicDirectory + "/" + "localForecast.nbs"));
		Song soChill = NBSDecoder.parse(new File(musicDirectory + "/" + "soChill.nbs"));
		Playlist lobbyMusic = new Playlist(soChill, localForecast, elevatorMusic);
		RadioSongPlayer rsp = new RadioSongPlayer(lobbyMusic);
		rsp.addPlayer(player);
		rsp.setPlaying(true);
		player.sendMessage(Text.translatable("manhunt.jukebox.playing", Text.translatable(rsp.getSong().getPath().getAbsoluteFile().getName())));
		player.sendMessage(Text.translatable("manhunt.jukebox.cancel"));
		player.sendMessage(Text.translatable("manhunt.jukebox.permanent"));
		player.sendMessage(Text.translatable("manhunt.mutelobbymusic.disable"));
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
				.setCallback(() -> personalSettings(player))
		);
		List<Text> gameLore = new ArrayList<>();
		gameLore.add(Text.translatable("manhunt.lore.game"));
		settings.setSlot(15, new GuiElementBuilder(Items.REPEATER)
				.setName(Text.translatable("manhunt.item.game"))
				.setLore(gameLore)
				.setCallback(() -> gameSettings(player))
		);
	}

	private static void personalSettings(ServerPlayerEntity player) {
		SimpleGui personalsettings = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
		personalsettings.setTitle(Text.translatable("manhunt.item.personal"));
		setGoBack(player, personalsettings);
		changePersonalSetting(player, personalsettings, "muteMusic", "manhunt.item.mutemusic", "manhunt.lore.mutemusic", Items.MUSIC_DISC_11, 10);
		changePersonalSetting(player, personalsettings, "muteLobbyMusic", "manhunt.item.mutelobbymusic", "manhunt.lore.mutelobbymusic", Items.JUKEBOX, 11);
		changePersonalSetting(player, personalsettings, "doNotDisturb", "manhunt.item.donotdisturb", "manhunt.lore.donotdisturb", Items.BARRIER, 12);
		personalsettings.open();
	}

	private static void gameSettings(ServerPlayerEntity player) {
		if (player.hasPermissionLevel(2) || player.hasPermissionLevel(4)) {
			SimpleGui gamesettings = new SimpleGui(ScreenHandlerType.GENERIC_9X4, player, false);
			gamesettings.setTitle(Text.translatable("manhunt.item.game"));
			if (getPlayerData(player).getString("lobbyRole").equals("leader")) {
				setGoBack(player, gamesettings);
				changeGameSetting(player, gamesettings, "hunterFreeze", "manhunt.item.hunterfreeze", "manhunt.lore.hunterfreeze", Items.ICE, 10);
				changeGameSetting(player, gamesettings, "timeLimit", "manhunt.item.timelimit", "manhunt.lore.timelimit", Items.CLOCK, 11);
				changeGameSetting(player, gamesettings, "compassUpdate", "manhunt.item.compassupdate", "manhunt.lore.compassupdate", Items.COMPASS, 12);
				gamesettings.open();
			} else if (!getPlayerData(player).getString("lobbyRole").equals("leader")) {
				player.sendMessage(Text.translatable("manhunt.chat.player"));
			}
		} else {
			player.sendMessage(Text.translatable("manhunt.chat.player"));
		}
	}

	private static void changePersonalSetting(ServerPlayerEntity player, SimpleGui gui, String setting, String name, String lore, Item item, int slot) {
		if (!player.getItemCooldownManager().isCoolingDown(item)) {
			int value = getPlayerScore(player, setting).getScore();
			List<Text> loreList = new ArrayList<>();
            loreList.add(Text.translatable(lore));
			if (value == 1) {
				loreList.add(Text.literal("On").formatted(Formatting.GREEN));
			} else {
				loreList.add(Text.literal("Off").formatted(Formatting.RED));
			}
			gui.setSlot(slot, new GuiElementBuilder(item)
					.hideFlags()
					.setName(Text.translatable(name))
					.setLore(loreList)
					.setCallback(() -> {
						if (value == 1) {
							getPlayerScore(player, setting).setScore(0);
						} else {
							getPlayerScore(player, setting).setScore(1);
						}
						if (setting.equals("muteMusic") || setting.equals("muteLobbyMusic")) {
							if (value == 1 && getPlayerScore(player, "muteMusic").getScore() == 0 && getPlayerScore(player, "muteLobbyMusic").getScore() == 0) {
								playLobbyMusic(player);
							} else {
								Nota.stopPlaying(player);
							}
						}
						if (setting.equals("doNotDisturb")) {
							if (value == 1) {
								getPlayerScore(player, setting).setScore(0);
							} else {
								getPlayerScore(player, setting).setScore(1);
							}
						}
						changePersonalSetting(player, gui, setting, name, lore, item, slot);
						player.getItemCooldownManager().set(item, 20);
					})
			);
		}
	}

	private static void changeGameSetting(ServerPlayerEntity player, SimpleGui gui, String setting, String name, String lore, Item item, int slot) {
		if (!player.getItemCooldownManager().isCoolingDown(item)) {
			List<Text> loreList = new ArrayList<>();
			loreList.add(Text.translatable(lore));
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
			if (setting.equals("hunterFreeze") || setting.equals("timeLimit")) {
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
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
													if (setting.equals("hunterFreeze")) {
														hunterFreeze = value;
														if (hunterFreeze == 0) {
															player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "hunterFreeze", Text.literal(value + " seconds (disabled)").formatted(Formatting.RED)), false);
														} else {
															player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "hunterFreeze", Text.literal(value + " seconds").formatted(Formatting.GREEN)), false);
														}
													}
													if (setting.equals("timeLimit")) {
														timeLimit = value;
														if (timeLimit == 0) {
															player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "timeLimit", Text.literal(value + " minutes (disabled)").formatted(Formatting.RED)), false);
														} else {
															player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.game", "timeLimit", Text.literal(value + " minutes").formatted(Formatting.GREEN)), false);
														}
													}
													ManhuntConfig.save();
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
						})
				);
			}
			if (setting.equals("compassUpdate")) {
				List<Text> loreListSecond = new ArrayList<>();
				loreListSecond.add(Text.translatable(lore));
				if (compassUpdate.equals("Automatic")) {
					loreListSecond.add(Text.literal("Automatic").formatted(Formatting.GREEN));
				} else {
					loreListSecond.add(Text.literal("Manual").formatted(Formatting.RED));
				}
				gui.setSlot(slot, new GuiElementBuilder(item)
						.hideFlags()
						.setName(Text.translatable(name))
						.setLore(loreListSecond)
						.setCallback(() -> {
							if (compassUpdate.equals("Automatic")) {
								compassUpdate = "Manual";
							} else {
								compassUpdate = "Automatic";
							}
							ManhuntConfig.save();
							changeGameSetting(player, gui, setting, name, lore, item, slot);
							player.getItemCooldownManager().set(item, 20);
						})
				);
			}
		}
	}

	private static void setGoBack(ServerPlayerEntity player, SimpleGui gui) {
		gui.setSlot(8, new GuiElementBuilder(Items.MAGENTA_GLAZED_TERRACOTTA)
				.setName(Text.translatable("manhunt.item.back"))
				.setCallback((index, type, action) -> settings(player))
		);
	}
}