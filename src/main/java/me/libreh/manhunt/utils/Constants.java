package me.libreh.manhunt.utils;

import me.libreh.manhunt.Manhunt;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.*;

public class Constants {
    public static final Path GAME_DIR = FabricLoader.getInstance().getGameDir();
    public static final Path WORLD_DIR = GAME_DIR.resolve("world");
    public static final RegistryKey<World> LOBBY_REGISTRY_KEY = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(Manhunt.MOD_ID, "lobby"));
    public static final Vec3d LOBBY_SPAWN = new Vec3d(0.5, 0, 0.5);

    public static final Set<UUID> JOIN_LIST = new HashSet<>();
    public static final Set<UUID> START_LIST = new HashSet<>();
    public static final Set<UUID> PLAY_LIST = new HashSet<>();
    public static final Set<UUID> PAUSE_LEAVE_LIST = new HashSet<>();
    public static final Set<UUID> RESET_LIST = new HashSet<>();

    public static final HashMap<UUID, Integer> SPAM_PREVENTION = new HashMap<>();
    public static final HashMap<UUID, Integer> PARKOUR_TIMER = new HashMap<>();
    public static final HashMap<UUID, Boolean> STARTED_PARKOUR = new HashMap<>();
    public static final HashMap<UUID, Boolean> FINISHED_PARKOUR = new HashMap<>();
    public static final List<UUID> READY_LIST = new ArrayList<>();

    public static final HashMap<UUID, Vec3d> SPAWN_POS = new HashMap<>();
    public static final HashMap<UUID, BlockPos> OVERWORLD_POSITION = new HashMap<>();
    public static final HashMap<UUID, BlockPos> NETHER_POSITION = new HashMap<>();
    public static final HashMap<UUID, BlockPos> END_POSITION = new HashMap<>();

    public static final HashMap<UUID, Vec3d> SAVED_POS = new HashMap<>();
    public static final HashMap<UUID, Float> SAVED_YAW = new HashMap<>();
    public static final HashMap<UUID, Float> SAVED_PITCH = new HashMap<>();
    public static final HashMap<UUID, Integer> SAVED_AIR = new HashMap<>();
    public static final HashMap<UUID, NbtCompound> SAVED_HUNGER = new HashMap<>();
    public static final HashMap<UUID, Collection<StatusEffectInstance>> SAVED_EFFECTS = new HashMap<>();

    public static final String PER_PLAYER = "per_player";
    public static final String RUNNERS_PREFERENCE = "runners_preference";
}
