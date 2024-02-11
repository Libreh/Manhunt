package manhunt.game;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import manhunt.Manhunt;
import manhunt.mixin.MinecraftServerAccessInterface;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Thanks to https://github.com/sakurawald/fuji-fabric

public class ManhuntWorldManager {
    private static final Set<ServerWorld> deletionQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> tick());
    }

    public static void enqueueWorldDeletion(ServerWorld world) {
        MinecraftServer server = Manhunt.SERVER;
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
        if (world.getPlayers().isEmpty()) {
            return;
        }

        MinecraftServer server = Manhunt.SERVER;

        List<ServerPlayerEntity> players = new ArrayList<>(world.getPlayers());

        for (ServerPlayerEntity player : players) {
            player.teleport(server.getWorld(ManhuntGame.lobbyRegistryKey), 0, 63, 5.5, 0.0F, 0.0F);
            player.getInventory().clear();
            ManhuntGame.updateGameMode(player);
            player.clearStatusEffects();
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SATURATION, StatusEffectInstance.INFINITE, 255, false, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, StatusEffectInstance.INFINITE, 255, false, false, false));
            player.setFireTicks(0);
            player.setOnFire(false);
            player.setHealth(20);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5);
            player.getHungerManager().setExhaustion(0);
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);

            ManhuntGame.currentRole.putIfAbsent(player.getUuid(), "hunter");

            if (player.isTeamPlayer(server.getScoreboard().getTeam("hunters"))) {
                server.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), server.getScoreboard().getTeam("hunters"));
            }

            if (player.isTeamPlayer(server.getScoreboard().getTeam("runners"))) {
                server.getScoreboard().removeScoreHolderFromTeam(player.getName().getString(), server.getScoreboard().getTeam("runners"));
            }

            if (!player.isTeamPlayer(server.getScoreboard().getTeam("players"))) {
                player.getScoreboard().addScoreHolderToTeam(player.getName().getString(), server.getScoreboard().getTeam("players"));
            }

            if (!(ManhuntGame.settings.setRoles == 1)) {
                NbtCompound nbt = new NbtCompound();
                nbt.putBoolean("Remove", true);
                ItemStack itemStack = new ItemStack(Items.BARRIER);
                itemStack.setNbt(nbt);
                player.getInventory().setStack(3, itemStack);
                player.getInventory().setStack(5, itemStack);
            }

            if (ManhuntGame.settings.setRoles == 3) {
                ManhuntGame.currentRole.put(player.getUuid(), "runner");
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