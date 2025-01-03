package me.libreh.manhunt.config;

import com.google.gson.*;
import me.libreh.manhunt.Manhunt;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static me.libreh.manhunt.utils.Constants.PER_PLAYER;
import static me.libreh.manhunt.utils.Constants.RUNNERS_PREFERENCE;

public class ManhuntConfig {
    private ManhuntConfig() {}

    public static final ManhuntConfig CONFIG = new ManhuntConfig();
    private final File confFile = new File("./config/manhunt.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static String presetMode = "free_select";
    private static boolean teamColor = true;
    private static Formatting huntersColor = Formatting.RED;
    private static Formatting runnersColor = Formatting.GREEN;
    private static int preloadDistance = 5;
    private static int headStartSec = 0;
    private static int timeLimitMin = 180;

    private static String customSounds = PER_PLAYER;
    private static String customTitles = PER_PLAYER;
    private static String friendlyFire = PER_PLAYER;
    private static String bedExplosionsPvP = RUNNERS_PREFERENCE;
    private static String netherLavaPvP = RUNNERS_PREFERENCE;
    private static String announceSeed = PER_PLAYER;
    private static String announceDuration = PER_PLAYER;

    private static boolean vanillaIntegration = true;
    private static Difficulty difficulty = Difficulty.NORMAL;
    private static int worldBorderSize = 59999968;
    private static int spawnRadius = 10;
    private static boolean spectatorsGenerateChunks = false;

    private static List<String> filesToReset = Arrays.asList(
            "advancements", "data/idcounts.dat", "data/map_*.dat", "data/raids.dat",
            "data/random_sequences.dat", "data/scoreboard.dat", "DIM*", "dimension",
            "entities", "playerdat", "poi", "region", "stats", "level.dat", "level.dat_old"
    );

    public String getPresetMode() {
        return presetMode;
    }
    public boolean isTeamColor() {
        return teamColor;
    }
    public Formatting getHuntersColor() {
        return huntersColor;
    }
    public Formatting getRunnersColor() {
        return runnersColor;
    }
    public int getPreloadDistance() {
        return preloadDistance;
    }
    public int getHeadStartSec() {
        return headStartSec;
    }
    public int getTimeLimitMin() {
        return timeLimitMin;
    }

    public String getCustomSounds() {
        return customSounds;
    }
    public String getCustomTitles() {
        return customTitles;
    }
    public String getFriendlyFire() {
        return friendlyFire;
    }
    public String getBedExplosionsPvP() {
        return bedExplosionsPvP;
    }
    public String getNetherLavaPvP() {
        return netherLavaPvP;
    }
    public String getAnnounceSeed() {
        return announceSeed;
    }
    public String getAnnounceDuration() {
        return announceDuration;
    }

    public boolean isVanilla() {
        return vanillaIntegration;
    }
    public Difficulty getDifficulty() {
        return difficulty;
    }
    public int getWorldBorder() {
        return worldBorderSize;
    }
    public int getSpawnRadius() {
        return spawnRadius;
    }
    public boolean isSpectatorsGenerateChunks() {
        return spectatorsGenerateChunks;
    }

    public List<String> getFilesToReset() {
        return filesToReset;
    }

    public void setPresetMode(String teamPreset) {
        ManhuntConfig.presetMode = teamPreset;
    }
    public void setTeamColor(boolean teamColor) {
        ManhuntConfig.teamColor = teamColor;
    }
    public void setHuntersColor(Formatting huntersColor) {
        ManhuntConfig.huntersColor = huntersColor;
    }
    public void setRunnersColor(Formatting runnersColor) {
        ManhuntConfig.runnersColor = runnersColor;
    }
    public void setPreloadDistance(int preloadDistance) {
        ManhuntConfig.preloadDistance = preloadDistance;
    }
    public void setHeadStartSec(int headStart) {
        ManhuntConfig.headStartSec = headStart;
    }
    public void setTimeLimitMin(int timeLimit) {
        ManhuntConfig.timeLimitMin = timeLimit;
    }

    public void setCustomSounds(String customSounds) {
        ManhuntConfig.customSounds = customSounds;
    }
    public void setCustomTitles(String customTitles) {
        ManhuntConfig.customTitles = customTitles;
    }
    public void setFriendlyFire(String friendlyFire) {
        ManhuntConfig.friendlyFire = friendlyFire;
    }
    public void setBedExplosionsPvP(String bedExplosionsPvP) {
        ManhuntConfig.bedExplosionsPvP = bedExplosionsPvP;
    }
    public void setNetherLavaPvP(String netherLavaPvP) {
        ManhuntConfig.netherLavaPvP = netherLavaPvP;
    }
    public void setAnnounceSeed(String announceSeed) {
        ManhuntConfig.announceSeed = announceSeed;
    }
    public void setAnnounceDuration(String announceDuration) {
        ManhuntConfig.announceDuration = announceDuration;
    }

    public void setVanilla(boolean vanilla) {
        ManhuntConfig.vanillaIntegration = vanilla;
    }
    public void setDifficulty(Difficulty difficulty) {
        ManhuntConfig.difficulty = difficulty;
    }
    public void setWorldBorder(int worldBorder) {
        ManhuntConfig.worldBorderSize = worldBorder;
    }
    public void setSpawnRadius(int spawnRadius) {
        ManhuntConfig.spawnRadius = spawnRadius;
    }
    public void setSpectatorsGenerateChunks(boolean spectatorsGenerateChunks) {
        ManhuntConfig.spectatorsGenerateChunks = spectatorsGenerateChunks;
    }

    public void load() {
        if (!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            JsonObject gameOptions = jo.getAsJsonObject("gameOptions");
            if ((je = gameOptions.get("presetMode")) != null) presetMode = je.getAsString();
            if ((je = gameOptions.get("teamColor")) != null) teamColor = je.getAsBoolean();
            if ((je = gameOptions.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = gameOptions.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = gameOptions.get("preloadDistance")) != null) preloadDistance = je.getAsInt();
            if ((je = gameOptions.get("headStartSec")) != null) headStartSec = je.getAsInt();
            if ((je = gameOptions.get("timeLimitMin")) != null) timeLimitMin = je.getAsInt();
            JsonObject globalPreferences = jo.getAsJsonObject("globalPreferences");
            if ((je = globalPreferences.get("customSounds")) != null) customSounds = je.getAsString();
            if ((je = globalPreferences.get("customTitles")) != null) customTitles = je.getAsString();
            if ((je = globalPreferences.get("friendlyFire")) != null) friendlyFire = je.getAsString();
            if ((je = globalPreferences.get("bedExplosions")) != null) bedExplosionsPvP = je.getAsString();
            if ((je = globalPreferences.get("netherLavaPvP")) != null) netherLavaPvP = je.getAsString();
            if ((je = globalPreferences.get("announceSeed")) != null) announceSeed = je.getAsString();
            if ((je = globalPreferences.get("announceDuration")) != null) announceDuration = je.getAsString();
            JsonObject modIntegrations = jo.getAsJsonObject("modIntegrations");
            JsonObject vanilla = modIntegrations.getAsJsonObject("vanilla");
            if ((je = vanilla.get("enabled")) != null) ManhuntConfig.vanillaIntegration = je.getAsBoolean();
            if ((je = vanilla.get("difficulty")) != null) difficulty = Difficulty.byName(je.getAsString());
            if ((je = vanilla.get("worldBorder")) != null) worldBorderSize = je.getAsInt();
            if ((je = vanilla.get("spawnRadius")) != null) spawnRadius = je.getAsInt();
            if ((je = jo.get("filesToReset")) != null) filesToReset = Arrays.asList(gson.fromJson(je, String[].class));
        } catch (FileNotFoundException ex) {
            Manhunt.LOGGER.trace("Couldn't load configuration file", ex);
        }

        save();
    }

    public void save() {
        try {
            if (!confFile.exists()) {
                confFile.getParentFile().mkdirs();
                confFile.createNewFile();
            }

            JsonObject jo = new JsonObject();
            JsonObject gameOptions = new JsonObject();
            gameOptions.add("presetMode", new JsonPrimitive(presetMode));
            gameOptions.add("teamColor", new JsonPrimitive(teamColor));
            gameOptions.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            gameOptions.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            gameOptions.add("preloadDistance", new JsonPrimitive(preloadDistance));
            gameOptions.add("headStartSec", new JsonPrimitive(headStartSec));
            gameOptions.add("timeLimitMin", new JsonPrimitive(timeLimitMin));
            JsonObject globalPreferences = new JsonObject();
            globalPreferences.add("customSounds", new JsonPrimitive(customSounds));
            globalPreferences.add("customTitles", new JsonPrimitive(customTitles));
            globalPreferences.add("friendlyFire", new JsonPrimitive(friendlyFire));
            globalPreferences.add("bedExplosions", new JsonPrimitive(bedExplosionsPvP));
            globalPreferences.add("netherLavaPvP", new JsonPrimitive(netherLavaPvP));
            globalPreferences.add("announceSeed", new JsonPrimitive(announceSeed));
            globalPreferences.add("announceDuration", new JsonPrimitive(announceDuration));
            JsonObject modIntegrations = new JsonObject();
            JsonObject vanilla = new JsonObject();
            vanilla.add("enabled", new JsonPrimitive(vanillaIntegration));
            vanilla.add("difficulty", new JsonPrimitive(difficulty.getName()));
            vanilla.add("worldBorderSize", new JsonPrimitive(worldBorderSize));
            vanilla.add("spawnRadius", new JsonPrimitive(spawnRadius));
            vanilla.add("spectatorsGenerateChunks", new JsonPrimitive(spectatorsGenerateChunks));
            jo.add("gameOptions", gameOptions);
            jo.add("globalPreferences", globalPreferences);
            modIntegrations.add("vanilla", vanilla);
            jo.add("modIntegrations", modIntegrations);
            jo.add("filesToReset", gson.toJsonTree(filesToReset));

            PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
            printwriter.print(gson.toJson(jo));
            printwriter.close();
        } catch (IOException ex) {
            Manhunt.LOGGER.trace("Couldn't save configuration file", ex);
        }
    }

    public static final int preloadDistanceDefault = preloadDistance;
    public static final String presetModeDefault = presetMode;
    public static final boolean teamColorDefault = teamColor;
    public static final Formatting huntersColorDefault = huntersColor;
    public static final Formatting runnersColorDefault = runnersColor;
    public static final int headStartSecDefault = headStartSec;
    public static final int timeLimitMinDefault = timeLimitMin;
    
    public static final String customSoundsDefault = customSounds;
    public static final String customTitlesDefault = customTitles;
    public static final String friendlyFireDefault = friendlyFire;
    public static final String bedExplosionsPvPDefault = bedExplosionsPvP;
    public static final String netherLavaPvPDefault = netherLavaPvP;
    public static final String announceSeedDefault = announceSeed;
    public static final String announceDurationDefault = announceDuration;
    
    public static final boolean vanillaDefault = vanillaIntegration;
    public static final Difficulty difficultyDefault = difficulty;
    public static final int worldBorderDefault = worldBorderSize;
    public static final int spawnRadiusDefault = spawnRadius;
    public static final boolean spectatorsGenerateChunksDefault = spectatorsGenerateChunks;
}