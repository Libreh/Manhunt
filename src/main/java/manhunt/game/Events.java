package manhunt.game;

import manhunt.ManhuntMod;
import manhunt.world.ManhuntWorldModule;
import me.lucko.fabric.api.permissions.v0.Permissions;
import me.mrnavastar.sqlib.DataContainer;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;

import java.io.IOException;
import java.util.LinkedList;

import static manhunt.ManhuntMod.*;

public class Events {
    public static void serverStart(MinecraftServer server) {
        new ManhuntWorldModule().loadWorlds(server);

        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        server.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        server.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
        server.getGameRules().get(GameRules.DO_MOB_LOOT).set(false, server);
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        server.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);
        server.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(0, server);
        server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        server.getGameRules().get(GameRules.SPAWN_RADIUS).set(0, server);
        server.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);

        server.setPvpEnabled(false);

        Scoreboard scoreboard = server.getScoreboard();

        for (Team team : scoreboard.getTeams()) {
            String name = team.getName();

            if (name.equals("players") || name.equals("hunters") || name.equals("runners")) {
                scoreboard.removeTeam(scoreboard.getTeam(name));
            }
        }

        scoreboard.addTeam("players");
        scoreboard.addTeam("hunters");
        scoreboard.addTeam("runners");

        scoreboard.getTeam("players").setCollisionRule(AbstractTeam.CollisionRule.NEVER);

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
                if (doesNotHaveItem(player)) {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putBoolean("Remove", true);
                    nbt.putBoolean("Preferences", true);
                    nbt.putInt("HideFlags", 1);
                    nbt.put("display", new NbtCompound());
                    nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.preferences\",\"italic\": false,\"color\": \"white\"}");
                    nbt.putString("SkullOwner", player.getName().getString());

                    ItemStack preferencesItem = new ItemStack(Items.PLAYER_HEAD);
                    preferencesItem.setNbt(nbt);

                    player.getInventory().setStack(0, preferencesItem);
                }

                if (Permissions.check(player, "manhunt.settings") || (player.hasPermissionLevel(1) || player.hasPermissionLevel(2) || player.hasPermissionLevel(3) || player.hasPermissionLevel(4))) {
                    if (doesNotHaveItem(player)) {
                        NbtCompound nbt = new NbtCompound();
                        nbt.putBoolean("Remove", true);
                        nbt.putBoolean("Settings", true);
                        nbt.putInt("HideFlags", 1);
                        nbt.put("display", new NbtCompound());
                        nbt.getCompound("display").putString("Name", "{\"translate\": \"manhunt.settings\",\"italic\": false,\"color\": \"white\"}");

                        ItemStack settingsItem = new ItemStack(Items.COMMAND_BLOCK);
                        settingsItem.setNbt(nbt);

                        player.getInventory().setStack(8, settingsItem);
                    }
                }
            }
        }

        if (ManhuntMod.getGameState() == GameState.PLAYING) {
            ManhuntMod.setAllRunners(new LinkedList<>());

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player != null) {
                    if (player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                        ManhuntMod.getAllRunners().add(player);
                    }
                }
            }

            if (config.getTimeLimit() != 0) {
                if (server.getWorld(overworldKey).getTime() % (20 * 60 * 60) / (20 * 60) >= config.getTimeLimit()) {
                    setGameState(GameState.POSTGAME);

                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        if (gameTitles.get(player)) {
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("manhunt.title.hunterswon").formatted(Formatting.RED)));
                            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("manhunt.title.timelimit").formatted(Formatting.DARK_RED)));
                        }

                        if (manhuntSounds.get(player)) {
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, player.getPos().getX(), player.getPos().getY(), player.getPos().getZ(), 0.5F, 0.5F, player.getWorld().random.nextLong()));
                        }
                    }
                }
            }
        }
    }

    public static void playerJoin(ServerPlayNetworkHandler handler, MinecraftServer server) {
        ServerPlayerEntity player = handler.getPlayer();

        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            player.teleport(server.getWorld(ManhuntMod.lobbyKey), 0.5, 63, 0.5, PositionFlag.ROT, 0, 0);
            player.getInventory().clear();
            ManhuntGame.updateGameMode(player);
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

            if (server.getWorld(ManhuntMod.overworldKey) == null) return;
            ManhuntGame.setPlayerSpawn(server.getWorld(ManhuntMod.overworldKey), player);
        }

        if (ManhuntMod.getGameState() == GameState.PLAYING || ManhuntMod.getGameState() == GameState.POSTGAME) {
            ManhuntGame.updateGameMode(player);

            player.setSpawnPoint(ManhuntMod.overworldKey, ManhuntMod.playerSpawn.get(player), 0, true, false);

            if (player.isTeamPlayer(player.getScoreboard().getTeam("players"))) {
                player.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), player.getScoreboard().getTeam("players"));
            }

            StatusEffectInstance saturationEffect = player.getStatusEffect(StatusEffects.SATURATION);

           if (saturationEffect == null) return;

           if (saturationEffect.isInfinite()) {
                player.removeStatusEffect(StatusEffects.SATURATION);
           }
        }

        if (!player.isTeamPlayer(player.getScoreboard().getTeam("players"))) {
            player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("players"));
        }

        if (!player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && !player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
            player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));
        }

        ManhuntMod.isRunner.putIfAbsent(player, false);

        ManhuntMod.gameTitles.putIfAbsent(player, true);
        ManhuntMod.manhuntSounds.putIfAbsent(player, true);
        ManhuntMod.nightVision.putIfAbsent(player, false);

        DataContainer dataContainer = ManhuntMod.getTable().get(player.getUuid());

        if (dataContainer == null) return;

        ManhuntMod.gameTitles.put(player, dataContainer.getBool("game_titles"));
        ManhuntMod.manhuntSounds.put(player, dataContainer.getBool("manhunt_sounds"));
        ManhuntMod.nightVision.put(player, dataContainer.getBool("night_vision"));
    }

    public static void playerLeave(ServerPlayNetworkHandler handler) {
        ServerPlayerEntity player = handler.getPlayer();

        ManhuntMod.isRunner.put(player, false);

        DataContainer dataContainer = ManhuntMod.getTable().getOrCreateDataContainer(player.getUuid());

        dataContainer.put("game_titles", ManhuntMod.gameTitles.get(player));
        dataContainer.put("manhunt_sounds", ManhuntMod.manhuntSounds.get(player));
        dataContainer.put("night_vision", ManhuntMod.nightVision.get(player));
    }

    public static TypedActionResult<ItemStack> useItem(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (ManhuntMod.getGameState() == GameState.PREGAME) {
            if (itemStack.getNbt().getBoolean("Preferences")) {
                ManhuntGame.openPreferencesGui((ServerPlayerEntity) player);
            }

            if (itemStack.getNbt().getBoolean("Settings")) {
                ManhuntGame.openSettingsGui((ServerPlayerEntity) player);
            }
        }

        if (ManhuntMod.getGameState() == GameState.PLAYING) {
            if (config.isTrackerCompass() && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Tracker") && !player.isSpectator() && player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && !player.getItemCooldownManager().isCoolingDown(itemStack.getItem())) {
                player.getItemCooldownManager().set(itemStack.getItem(), 20);
                if (!itemStack.getNbt().contains("Info")) {
                    itemStack.getNbt().put("Info", new NbtCompound());
                }

                NbtCompound info = itemStack.getNbt().getCompound("Info");

                if (!info.contains("Name", NbtElement.STRING_TYPE) && !ManhuntMod.getAllRunners().isEmpty()) {
                    info.putString("Name", ManhuntMod.getAllRunners().get(0).getName().getString());
                }

                ServerPlayerEntity trackedPlayer = player.getServer().getPlayerManager().getPlayer(info.getString("Name"));

                if (trackedPlayer != null) {
                    player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.MASTER, 0.1f, 1f);
                    ManhuntGame.updateCompass((ServerPlayerEntity) player, itemStack.getNbt(), trackedPlayer);
                }
            }
        }

        return TypedActionResult.pass(itemStack);
    }

    public static void playerRespawn(ServerPlayerEntity player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (player.isTeamPlayer(scoreboard.getTeam("runners")) && ManhuntMod.isRunner.put(player, false)) {
            scoreboard.clearTeam(player.getName().getString());
            scoreboard.addScoreHolderToTeam(player.getName().getString(), scoreboard.getTeam("hunters"));
        }
        ManhuntGame.updateGameMode(player);
    }

    private static void spawnStructure(MinecraftServer server) throws IOException {
        NbtCompound lobbyNbt = NbtIo.readCompressed(ManhuntMod.class.getResourceAsStream("/manhunt/lobby/lobby.nbt"), NbtSizeTracker.ofUnlimitedBytes());

        ServerWorld lobbyWorld = server.getWorld(ManhuntMod.lobbyKey);

        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 0), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, 0), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, -15), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(-15, -15), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 16), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(16, 0), 16, Unit.INSTANCE);
        lobbyWorld.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(0, 16), 16, Unit.INSTANCE);
        placeStructure(lobbyWorld, new BlockPos(-24, 40, -24), lobbyNbt);
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

    private static boolean doesNotHaveItem(PlayerEntity player) {
        boolean bool = false;
        for (ItemStack itemStack : player.getInventory().main) {
            if (itemStack.getItem() == Items.COMPARATOR && itemStack.getNbt() != null && itemStack.getNbt().getBoolean("Remove") && itemStack.getNbt().getBoolean("Settings")) {
                bool = true;
                break;
            }
        }

        if (player.playerScreenHandler.getCursorStack().getNbt() != null && player.playerScreenHandler.getCursorStack().getNbt().getBoolean("Settings")) {
            bool = true;
        } else if (player.getOffHandStack().hasNbt() && player.getOffHandStack().getNbt().getBoolean("Remove") && player.getOffHandStack().getNbt().getBoolean("Settings")) {
            bool = true;
        }
        return !bool;
    }
}
