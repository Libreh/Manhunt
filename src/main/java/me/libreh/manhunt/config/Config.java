package me.libreh.manhunt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.libreh.manhunt.Manhunt;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static me.libreh.manhunt.utils.Constants.PER_PLAYER;
import static me.libreh.manhunt.utils.Constants.PER_RUNNER;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    
    private static Config CONFIG;
    
    public static Config getConfig() {
        return CONFIG;
    }

    public GameOptions gameOptions = new GameOptions();
    public GlobalPreferences globalPreferences = new GlobalPreferences();
    public ModIntegrations modIntegrations = new ModIntegrations();
    
    public static class GameOptions {
        public int preloadDistance = 4;
        public String presetMode = "free_select";
        public TeamColor teamColor = new TeamColor();
        public int headStart = 0;
        public int timeLimit = 180;

        public static class TeamColor {
            public boolean enabled = true;
            public Formatting huntersColor = Formatting.RED;
            public Formatting runnersColor = Formatting.GREEN;
        }
    }

    public static class GlobalPreferences {
        public String customSounds = PER_PLAYER;
        public String customTitles = PER_PLAYER;
        public String friendlyFire = PER_PLAYER;
        public String bedExplosionsPvP = PER_RUNNER;
        public String netherLavaPvP = PER_RUNNER;
        public String announceSeed = PER_PLAYER;
        public String announceDuration = PER_PLAYER;
    }

    public static class ModIntegrations {
        public VanillaIntegration vanillaIntegration = new VanillaIntegration();

        public static class VanillaIntegration {
            public boolean enabled = true;
            public Difficulty difficulty = Difficulty.NORMAL;
            public int borderSize = 59999968;
            public int spawnRadius = 10;
            public boolean spectatorsGenerateChunks = false;
        }
    }

    public List<String> filesToReset = Arrays.asList(
            "advancements", "data/idcounts.dat", "data/map_*.dat", "data/raids.dat",
            "data/random_sequences.dat", "data/scoreboard.dat", "DIM*", "dimension",
            "entities", "playerdat", "poi", "region", "stats", "level.dat", "level.dat_old"
    );

    public int defaultOpPermissionLevel = 3;

    public static void loadConfig() {
        Config oldConfig = CONFIG;

        CONFIG = null;
        try {
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "manhunt.json");

            CONFIG = configFile.exists() ? GSON.fromJson(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8), Config.class) : new Config();

            saveConfig();
        } catch (IOException exception) {
            CONFIG = oldConfig;
        }
    }

    public static void saveConfig() {
        try {
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "manhunt.json");

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            writer.write(GSON.toJson(CONFIG));
            writer.close();
        } catch (Exception exception) {
            Manhunt.LOGGER.error("Something went wrong while saving config!", exception);
        }
    }

    public transient final int preloadDistanceDefault = gameOptions.preloadDistance;
    public transient final String presetModeDefault = gameOptions.presetMode;
    public transient final boolean teamColorDefault = gameOptions.teamColor.enabled;
    public transient final Formatting huntersColorDefault = gameOptions.teamColor.huntersColor;
    public transient final Formatting runnersColorDefault = gameOptions.teamColor.runnersColor;
    public transient final int headStartSecDefault = gameOptions.headStart;
    public transient final int timeLimitMinDefault = gameOptions.timeLimit;

    public transient final String customSoundsDefault = globalPreferences.customSounds;
    public transient final String customTitlesDefault = globalPreferences.customTitles;
    public transient final String friendlyFireDefault = globalPreferences.friendlyFire;
    public transient final String bedExplosionsPvPDefault = globalPreferences.bedExplosionsPvP;
    public transient final String netherLavaPvPDefault = globalPreferences.netherLavaPvP;
    public transient final String announceSeedDefault = globalPreferences.announceSeed;
    public transient final String announceDurationDefault = globalPreferences.announceDuration;

    public transient final boolean vanillaDefault = modIntegrations.vanillaIntegration.enabled;
    public transient final Difficulty difficultyDefault = modIntegrations.vanillaIntegration.difficulty;
    public transient final int borderSizeDefault = modIntegrations.vanillaIntegration.borderSize;
    public transient final int spawnRadiusDefault = modIntegrations.vanillaIntegration.spawnRadius;
    public transient final boolean spectatorsGenerateChunksDefault = modIntegrations.vanillaIntegration.spectatorsGenerateChunks;
}