package manhunt.world;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import manhunt.ManhuntMod;
import manhunt.mixin.MinecraftServerAccessInterface;
import manhunt.world.interfaces.SimpleRegistryInterface;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static manhunt.ManhuntMod.config;
import static manhunt.game.ManhuntGame.updateGameMode;

public class ManhuntWorldManager {
    private static final Set<ServerWorld> deletionQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> tick());
    }

    public static void enqueueWorldDeletion(ServerWorld world) {
        MinecraftServer server = world.getServer();
        server.submit(() -> {
            deletionQueue.add(world);
        });
    }

    private static void tick() {
        if (!deletionQueue.isEmpty()) {
            deletionQueue.removeIf(ManhuntWorldManager::tickDeleteWorld);
        }
    }

    private static boolean tickDeleteWorld(ServerWorld world) {
        if (isWorldUnloaded(world)) {
            delete(world);
            return true;
        } else {
            kickPlayers(world);
            return false;
        }
    }

    private static void kickPlayers(ServerWorld world) {
        MinecraftServer server = world.getServer();

        Scoreboard scoreboard = world.getScoreboard();

        ServerWorld lobbyWorld = server.getWorld(ManhuntMod.lobbyKey);

        lobbyWorld.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.DO_FIRE_TICK).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.DO_INSOMNIA).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.DO_MOB_LOOT).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(0, server);
        lobbyWorld.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        lobbyWorld.getGameRules().get(GameRules.SPAWN_RADIUS).set(0, server);
        lobbyWorld.getGameRules().get(GameRules.FALL_DAMAGE).set(false, server);

        server.setPvpEnabled(false);

        List<ServerPlayerEntity> players = new ArrayList<>(world.getServer().getPlayerManager().getPlayerList());

        if (config.isTeamColor()) {
            server.getScoreboard().getTeam("hunters").setColor(config.getHuntersColor());
            server.getScoreboard().getTeam("runners").setColor(config.getRunnersColor());
        } else {
            server.getScoreboard().getTeam("hunters").setColor(Formatting.RESET);
            server.getScoreboard().getTeam("runners").setColor(Formatting.RESET);
        }

        for (ServerPlayerEntity player : players) {
            player.teleport(server.getWorld(ManhuntMod.lobbyKey), 0.5, 63, 0.5, PositionFlag.ROT, 0, 0);
            player.clearStatusEffects();
            player.getInventory().clear();
            player.setOnFire(false);
            player.setFireTicks(0);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);
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

            updateGameMode(player);

            scoreboard.addScoreHolderToTeam(player.getName().getString(), scoreboard.getTeam("players"));

            if (ManhuntMod.isRunner.get(player)) {
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("runners"));
            }

            if (!player.isTeamPlayer(player.getScoreboard().getTeam("hunters")) && !player.isTeamPlayer(player.getScoreboard().getTeam("runners"))) {
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), player.getScoreboard().getTeam("hunters"));
            }
        }
    }

    private static boolean isWorldUnloaded(ServerWorld world) {
        return world.getPlayers().isEmpty() && world.getChunkManager().getLoadedChunkCount() <= 0;
    }

    private static SimpleRegistry<DimensionOptions> getDimensionsRegistry(MinecraftServer server) {
        DynamicRegistryManager registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();
        return (SimpleRegistry<DimensionOptions>) registryManager.get(RegistryKeys.DIMENSION);
    }

    private static void delete(ServerWorld world) {
        MinecraftServer server = world.getServer();
        MinecraftServerAccessInterface serverAccess = (MinecraftServerAccessInterface) server;

        RegistryKey<World> worldRegistryKey = world.getRegistryKey();
        if (serverAccess.getWorlds().remove(worldRegistryKey, world)) {
            ServerWorldEvents.UNLOAD.invoker().onWorldUnload(server, world);
            SimpleRegistry<DimensionOptions> dimensionsRegistry = getDimensionsRegistry(server);
            SimpleRegistryInterface.remove(dimensionsRegistry, worldRegistryKey.getValue());
            LevelStorage.Session session = serverAccess.getSession();
            File worldDirectory = session.getWorldDirectory(worldRegistryKey).toFile();
            cleanFiles(worldDirectory);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void cleanFiles(File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File child : files) {
                if (child.isDirectory()) {
                    cleanFiles(child);
                } else {
                    child.delete();
                }
            }
        }
    }
}