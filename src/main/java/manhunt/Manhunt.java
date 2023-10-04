package manhunt;

import manhunt.commands.JukeboxCommand;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static String everythingIsAwesome = "everythingIsAwesome";
	public static String flightOfTheBumblebee = "flightOfTheBumblebee";
	public static String fnafSong = "fnafSong";
	public static String hesAPirate = "hesAPirate";
	public static String heySoulSister = "heySoulSister";
	public static String iGotAFeeling = "iGotAFeeling";
	public static String indianaJones = "indianaJones";
	public static String jurassicPark = "jurassicPark";
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
	public ScoreboardObjective timeObjective = null;
	private boolean beforeSound = true;
	private boolean afterSound = true;
	private long lastDelay = System.currentTimeMillis();
	private boolean holding;

	@Override
	public void onInitialize() {
		ManhuntConfig.load();

		if (!databaseName.isEmpty() && !databaseAddress.isEmpty() && !databasePort.isEmpty() && !databaseUser.isEmpty() && !databasePassword.isEmpty()) {
			Manhunt.table();
		}

		LOGGER.info("Manhunt initialized");

		DeleteWorld.invoke();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			JukeboxCommand.register(dispatcher);
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
			server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
			server.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
			server.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
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

			for (ScoreboardObjective objective : server.getScoreboard().getObjectives()) {
				if (objective.getName().equals("time")) {
					server.getScoreboard().removeScoreboardObjective(objective);
					this.timeObjective = objective;
				}
			}

			server.getScoreboard().addScoreboardObjective(new ScoreboardObjective(server.getScoreboard(), "time", ScoreboardCriterion.DUMMY, Text.of("time"), ScoreboardCriterion.RenderType.INTEGER));

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
			songs.add(everythingIsAwesome);
			songs.add(flightOfTheBumblebee);
			songs.add(fnafSong);
			songs.add(hesAPirate);
			songs.add(heySoulSister);
			songs.add(iGotAFeeling);
			songs.add(indianaJones);
			songs.add(jurassicPark);
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

					if (player.getZ() < 0 && timeObjective != null) {
						int ticks = player.getScoreboard().getPlayerScore(player.getName().getString(), timeObjective).getScore();
						if (beforeSound && player.getZ() < -5) {
							player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS, 1f, 1f);
							beforeSound = false;
						}
						if (afterSound && player.getZ() < -5) {
							player.getScoreboard().getPlayerScore(player.getName().getString(), timeObjective).setScore(player.getScoreboard().getPlayerScore(player.getName().getString(), timeObjective).getScore() + 1);
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
						if (player.getZ() < -5)
							player.sendMessage(Text.literal("Current time: " + sec_string + ":" + ms_string), true);
						if (player.getZ() < -24 && player.getZ() > -27) {
							if (player.getX() < -6) {
								if (player.getY() >= 70 && player.getY() < 72) {
									player.sendMessage(Text.literal("Current time: " + sec_string + ":" + ms_string).formatted(Formatting.GREEN), true);
									player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS, 1f, 2f);
								}
							}
						}
						if (player.getY() < 61) {
							player.sendMessage(Text.literal("Current time: " + sec_string + ":" + ms_string).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS, 1f, 0.5f);
							beforeSound = true;
							afterSound = true;
						}
						if (player.getZ() < -27 && player.getY() < 68) {
							player.sendMessage(Text.literal("Current time: " + sec_string + ":" + ms_string).formatted(Formatting.RED), true);
							resetPlayer(player, player.getServer().getWorld(lobbyRegistryKey));
							player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE.value(), SoundCategory.PLAYERS, 1f, 0.5f);
							beforeSound = true;
							afterSound = true;
						}
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
						} else if (ManhuntConfig.automaticCompassUpdate && System.currentTimeMillis() - lastDelay > ((long) 1000)) {
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
							if (getPlayerData(player).getString("currentRole") == null || getPlayerData(player).getString("currentRole").isEmpty()) {
								getPlayerData(player).put("currentRole", "hunter");
								player.getScoreboard().addPlayerToTeam(player.getName().getString(), hunters);
							} else if (getPlayerData(player).getString("currentRole") != null) {
								if (getPlayerData(player).getString("currentRole").equals("hunter")) {
									player.getScoreboard().addPlayerToTeam(player.getName().getString(), hunters);
								} else if (getPlayerData(player).getString("currentRole").equals("runner")) {
									player.getScoreboard().addPlayerToTeam(player.getName().getString(), runners);
								}
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
                                    if (getPlayerData(lobbyPlayer).getString("currentRole").equals("runner")) {
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

                            getPlayerData((ServerPlayerEntity) player).put("currentRole", "hunter");

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

                            getPlayerData((ServerPlayerEntity) player).put("currentRole", "runner");

                            player.getServer().getPlayerManager().broadcast(Text.translatable("manhunt.chat.runner", player.getName().getString()), false);
                        }
                    }
                }
			}

			if (ManhuntGame.state == PLAYING) {
				if (itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker") && !player.isSpectator() && player.isTeamPlayer(world.getScoreboard().getTeam("hunters"))) {
					player.getItemCooldownManager().set(itemStack.getItem(), 20);
					if (!itemStack.getOrCreateNbt().contains("Info")) {
						itemStack.getOrCreateNbt().put("Info", new NbtCompound());
					}
					NbtCompound info = itemStack.getOrCreateNbt().getCompound("Info");

					if (!info.contains("Name" ,NbtElement.STRING_TYPE) && !Manhunt.allRunners.isEmpty()) {
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
					if (message.getSignedContent().contains(playerName)) {
						player.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS, 1f, 1f);
						player.sendMessage(Text.literal("You have been pinged!").formatted(Formatting.GOLD));
					}
				}
			}
		});
	}

	private void spawnLobbyStructure(MinecraftServer server) throws IOException {
		var lobbyDir = FabricLoader.getInstance().getConfigDir().resolve("lobby");
		if (!Files.exists(lobbyDir))
			lobbyDir.toFile().mkdirs();

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
		placeStructure(lobbyWorld, new BlockPos(-21, 57, -54), lobbyParkourNbt);
		placeStructure(lobbyWorld, new BlockPos(-21, 57, -6), lobbyIslandNbt);
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

	public static Table table() {
		MySQLDatabase data = new MySQLDatabase(MOD_ID, databaseName, databaseAddress, databasePort, databaseUser, databasePassword);

        return data.createTable("players")
				.addColumn("playLobbyMusic", SQLDataType.BOOL)
				.addColumn("currentRole", SQLDataType.STRING)
				.finish();
	}

	public static DataContainer getPlayerData(ServerPlayerEntity player) {
		DataContainer playerData = null;
		if (table().get(player.getUuidAsString()) == null) {
			playerData = table().createDataContainer(player.getUuidAsString());
		} else if (table().get(player.getUuidAsString()) != null) {
			playerData = table().get(player.getUuidAsString());
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
		if (timeObjective != null) {
			player.getScoreboard().getPlayerScore(player.getName().getString(), timeObjective).setScore(0);
		}
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
}