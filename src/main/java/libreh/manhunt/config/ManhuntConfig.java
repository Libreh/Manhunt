package libreh.manhunt.config;

import com.google.gson.*;
import com.mojang.text2speech.Narrator;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class ManhuntConfig {
    public static final ManhuntConfig CONFIG = new ManhuntConfig();
    private final File confFile = new File("./config/manhunt.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ManhuntConfig() {
    }

    private boolean setMotd = true;
    private int rolePreset = 1;
    private boolean teamColor = true;
    private Formatting huntersColor = Formatting.RED;
    private Formatting runnersColor = Formatting.GREEN;
    private boolean waitForRunner = true;
    private int hunterReleaseTime = 20;
    private int runnerHeadStart = 1;
    private boolean runnersGlow = false;
    private boolean huntOnDeath = true;
    private boolean runnersCanPause = true;
    private int leavePauseTime = 5;
    private int timeLimit = 240;
    private boolean spectateOnWin = true;
    private boolean customTitles = true;
    private boolean customSounds = true;
    private boolean customParticles = true;
    private int trackerType = 1;
    private boolean nightVision = true;
    private int friendlyFire = 2;
    private boolean runnerPreferences = true;
    private boolean bedExplosions = true;
    private boolean lavaPvpInNether = true;
    private String startTitle = "Manhunt";
    private String startSubtitle = "Good luck and have fun";
    private String pausedTitle = "Paused";
    private boolean vanilla = true;
    private Difficulty difficulty = Difficulty.NORMAL;
    private int worldBorder = 59999968;
    private int spawnRadius = 10;
    private boolean spectatorsGenerateChunks = false;
    private boolean chunky = true;
    private int overworld = 8000;
    private int theNether = 1000;
    private int theEnd = 0;

    public boolean isSetMotd() {
        return setMotd;
    }

    public void setSetMotd(boolean setMotd) {
        this.setMotd = setMotd;
    }

    public int getRolePreset() {
        return rolePreset;
    }

    public void setRolePreset(int rolePreset) {
        this.rolePreset = rolePreset;
    }

    public boolean isTeamColor() {
        return teamColor;
    }

    public void setTeamColor(boolean teamColor) {
        this.teamColor = teamColor;
    }

    public Formatting getHuntersColor() {
        return huntersColor;
    }

    public void setHuntersColor(Formatting huntersColor) {
        this.huntersColor = huntersColor;
    }

    public Formatting getRunnersColor() {
        return runnersColor;
    }

    public void setRunnersColor(Formatting runnersColor) {
        this.runnersColor = runnersColor;
    }

    public boolean isWaitForRunner() {
        return waitForRunner;
    }

    public void setWaitForRunner(boolean waitForRunner) {
        this.waitForRunner = waitForRunner;
    }

    public int getHunterReleaseTime() {
        return hunterReleaseTime;
    }

    public void setHunterReleaseTime(int hunterReleaseTime) {
        this.hunterReleaseTime = hunterReleaseTime;
    }

    public int getRunnerHeadStart() {
        return runnerHeadStart;
    }

    public void setRunnerHeadStart(int runnerHeadStart) {
        this.runnerHeadStart = runnerHeadStart;
    }

    public boolean isRunnersGlow() {
        return runnersGlow;
    }

    public void setRunnersGlow(boolean runnersGlow) {
        this.runnersGlow = runnersGlow;
    }

    public boolean isHuntOnDeath() {
        return huntOnDeath;
    }

    public void setHuntOnDeath(boolean huntOnDeath) {
        this.huntOnDeath = huntOnDeath;
    }

    public boolean isRunnersCanPause() {
        return runnersCanPause;
    }

    public void setRunnersCanPause(boolean runnersCanPause) {
        this.runnersCanPause = runnersCanPause;
    }

    public int getLeavePauseTime() {
        return leavePauseTime;
    }

    public void setLeavePauseTime(int leavePauseTime) {
        this.leavePauseTime = leavePauseTime;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isSpectateOnWin() {
        return spectateOnWin;
    }

    public void setSpectateOnWin(boolean spectateOnWin) {
        this.spectateOnWin = spectateOnWin;
    }

    public boolean isCustomTitles() {
        return customTitles;
    }

    public void setCustomTitles(boolean customTitles) {
        this.customTitles = customTitles;
    }

    public boolean isCustomSounds() {
        return customSounds;
    }

    public void setCustomSounds(boolean customSounds) {
        this.customSounds = customSounds;
    }

    public int getTrackerType() {
        return trackerType;
    }

    public void setTrackerType(int trackerType) {
        this.trackerType = trackerType;
    }

    public boolean isNightVision() {
        return nightVision;
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
    }

    public int getFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(int friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public boolean isCustomParticles() {
        return customParticles;
    }

    public void setCustomParticles(boolean customParticles) {
        this.customParticles = customParticles;
    }

    public boolean isRunnerPreferences() {
        return runnerPreferences;
    }

    public void setRunnerPreferences(boolean runnerPreferences) {
        this.runnerPreferences = runnerPreferences;
    }

    public boolean isBedExplosions() {
        return bedExplosions;
    }

    public void setBedExplosions(boolean bedExplosions) {
        this.bedExplosions = bedExplosions;
    }

    public boolean isLavaPvpInNether() {
        return lavaPvpInNether;
    }

    public void setLavaPvpInNether(boolean lavaPvpInNether) {
        this.lavaPvpInNether = lavaPvpInNether;
    }

    public String getStartTitle() {
        return startTitle;
    }

    public void setStartTitle(String startTitle) {
        this.startTitle = startTitle;
    }

    public String getStartSubtitle() {
        return startSubtitle;
    }

    public void setStartSubtitle(String startSubtitle) {
        this.startSubtitle = startSubtitle;
    }

    public String getPausedTitle() {
        return pausedTitle;
    }

    public void setPausedTitle(String pausedTitle) {
        this.pausedTitle = pausedTitle;
    }

    public boolean isVanilla() {
        return vanilla;
    }

    public void setVanilla(boolean vanilla) {
        this.vanilla = vanilla;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getWorldBorder() {
        return worldBorder;
    }

    public void setWorldBorder(int worldBorder) {
        this.worldBorder = worldBorder;
    }

    public int getSpawnRadius() {
        return spawnRadius;
    }

    public void setSpawnRadius(int spawnRadius) {
        this.spawnRadius = spawnRadius;
    }

    public boolean isSpectatorsGenerateChunks() {
        return spectatorsGenerateChunks;
    }

    public void setSpectatorsGenerateChunks(boolean spectatorsGenerateChunks) {
        this.spectatorsGenerateChunks = spectatorsGenerateChunks;
    }

    public boolean isChunky() {
        return chunky;
    }

    public void setChunky(boolean chunky) {
        this.chunky = chunky;
    }

    public int getTheNether() {
        return theNether;
    }

    public void setTheNether(int theNether) {
        this.theNether = theNether;
    }

    public int getOverworld() {
        return overworld;
    }

    public void setOverworld(int overworld) {
        this.overworld = overworld;
    }

    public int getTheEnd() {
        return theEnd;
    }

    public void setTheEnd(int theEnd) {
        this.theEnd = theEnd;
    }

    public void load() {
        if (!confFile.exists() || confFile.length() == 0) save();
        try {
            @Nullable JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            @Nullable JsonObject gameOptions = jo.getAsJsonObject("gameOptions");
            if (gameOptions == null) gameOptions = jo.getAsJsonObject("manhuntSettings");
            if ((je = gameOptions.get("setMotd")) != null) setMotd = je.getAsBoolean();
            if ((je = gameOptions.get("rolePreset")) != null) rolePreset = je.getAsInt();
            if ((je = gameOptions.get("teamColor")) != null) teamColor = je.getAsBoolean();
            if ((je = gameOptions.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = gameOptions.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = gameOptions.get("waitForRunner")) != null) waitForRunner = je.getAsBoolean();
            if ((je = gameOptions.get("hunterReleaseTime")) != null) hunterReleaseTime = je.getAsInt();
            if ((je = gameOptions.get("runnerHeadStart")) != null) runnerHeadStart = je.getAsInt();
            if ((je = gameOptions.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = gameOptions.get("runnersGlow")) != null) runnersGlow = je.getAsBoolean();
            if ((je = gameOptions.get("huntOnDeath")) != null) huntOnDeath = je.getAsBoolean();
            if ((je = gameOptions.get("runnersCanPause")) != null) runnersCanPause = je.getAsBoolean();
            if ((je = gameOptions.get("leavePauseTime")) != null) leavePauseTime = je.getAsInt();
            if ((je = gameOptions.get("spectateOnWin")) != null) spectateOnWin = je.getAsBoolean();
            @Nullable JsonObject globalSettings = jo.getAsJsonObject("globalSettings");
            if (globalSettings == null) globalSettings = jo.getAsJsonObject("globalPreferences");
            if ((je = globalSettings.get("customTitles")) != null) customTitles = je.getAsBoolean();
            if ((je = globalSettings.get("customSounds")) != null) customSounds = je.getAsBoolean();
            if ((je = globalSettings.get("customParticles")) != null) customParticles = je.getAsBoolean();
            if ((je = globalSettings.get("trackerType")) != null) trackerType = je.getAsInt();
            if ((je = globalSettings.get("nightVision")) != null) nightVision = je.getAsBoolean();
            if ((je = globalSettings.get("friendlyFire")) != null) friendlyFire = je.getAsInt();
            if ((je = globalSettings.get("bedExplosions")) != null) bedExplosions = je.getAsBoolean();
            if ((je = globalSettings.get("lavaPvpInNether")) != null) lavaPvpInNether = je.getAsBoolean();
            if ((je = globalSettings.get("runnerPreferences")) != null) runnerPreferences = je.getAsBoolean();
            @Nullable JsonObject titleTexts = jo.getAsJsonObject("titleTexts");
            if ((je = titleTexts.get("startTitle")) != null) startTitle = je.getAsString();
            if ((je = titleTexts.get("startSubtitle")) != null) startSubtitle = je.getAsString();
            if ((je = titleTexts.get("pausedTitle")) != null) pausedTitle = je.getAsString();
            @Nullable JsonObject modIntegrations = jo.getAsJsonObject("modIntegrations");
            @Nullable JsonObject vanilla = modIntegrations.getAsJsonObject("vanilla");
            if ((je = vanilla.get("enabled")) != null) this.vanilla = je.getAsBoolean();
            if ((je = vanilla.get("difficulty")) != null) this.difficulty = Difficulty.byName(je.getAsString());
            if ((je = vanilla.get("worldBorder")) != null) worldBorder = je.getAsInt();
            if ((je = vanilla.get("spawnRadius")) != null) this.spawnRadius = je.getAsInt();
            if ((je = vanilla.get("spectatorsGenerateChunks")) != null)
                this.spectatorsGenerateChunks = je.getAsBoolean();
            @Nullable JsonObject chunky = modIntegrations.getAsJsonObject("chunky");
            if ((je = chunky.get("enabled")) != null) this.chunky = je.getAsBoolean();
            if ((je = chunky.get("overworld")) != null) overworld = je.getAsInt();
            if ((je = chunky.get("the_nether")) != null) theNether = je.getAsInt();
            if ((je = chunky.get("the_end")) != null) theEnd = je.getAsInt();
        } catch (FileNotFoundException ex) {
            try {
                FileUtils.delete(confFile);
            } catch (Exception ignored) {
            }
            save();
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
            gameOptions.add("setMotd", new JsonPrimitive(setMotd));
            gameOptions.add("rolePreset", new JsonPrimitive(rolePreset));
            gameOptions.add("teamColor", new JsonPrimitive(teamColor));
            gameOptions.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            gameOptions.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            gameOptions.add("waitForRunner", new JsonPrimitive(waitForRunner));
            gameOptions.add("hunterReleaseTime", new JsonPrimitive(hunterReleaseTime));
            gameOptions.add("runnerHeadStart", new JsonPrimitive(runnerHeadStart));
            gameOptions.add("runnersGlow", new JsonPrimitive(runnersGlow));
            gameOptions.add("huntOnDeath", new JsonPrimitive(huntOnDeath));
            gameOptions.add("runnersCanPause", new JsonPrimitive(runnersCanPause));
            gameOptions.add("leavePauseTime", new JsonPrimitive(leavePauseTime));
            gameOptions.add("timeLimit", new JsonPrimitive(timeLimit));
            gameOptions.add("spectateOnWin", new JsonPrimitive(spectateOnWin));
            JsonObject globalSettings = new JsonObject();
            globalSettings.add("customTitles", new JsonPrimitive(customTitles));
            globalSettings.add("customSounds", new JsonPrimitive(customSounds));
            globalSettings.add("customParticles", new JsonPrimitive(customParticles));
            globalSettings.add("trackerType", new JsonPrimitive(trackerType));
            globalSettings.add("nightVision", new JsonPrimitive(nightVision));
            globalSettings.add("friendlyFire", new JsonPrimitive(friendlyFire));
            globalSettings.add("runnerPreferences", new JsonPrimitive(runnerPreferences));
            globalSettings.add("bedExplosions", new JsonPrimitive(bedExplosions));
            globalSettings.add("lavaPvpInNether", new JsonPrimitive(lavaPvpInNether));
            JsonObject titleTexts = new JsonObject();
            titleTexts.add("startTitle", new JsonPrimitive(startTitle));
            titleTexts.add("startSubtitle", new JsonPrimitive(startSubtitle));
            titleTexts.add("pausedTitle", new JsonPrimitive(pausedTitle));
            JsonObject modIntegrations = new JsonObject();
            JsonObject vanilla = new JsonObject();
            vanilla.add("enabled", new JsonPrimitive(this.vanilla));
            vanilla.add("difficulty", new JsonPrimitive(difficulty.getName()));
            vanilla.add("worldBorder", new JsonPrimitive(worldBorder));
            vanilla.add("spawnRadius", new JsonPrimitive(spawnRadius));
            vanilla.add("spectatorsGenerateChunks", new JsonPrimitive(spectatorsGenerateChunks));
            JsonObject chunky = new JsonObject();
            chunky.add("enabled", new JsonPrimitive(this.chunky));
            chunky.add("overworld", new JsonPrimitive(overworld));
            chunky.add("the_nether", new JsonPrimitive(theNether));
            chunky.add("the_end", new JsonPrimitive(theEnd));
            jo.add("manhuntSettings", gameOptions);
            jo.add("globalPreferences", globalSettings);
            jo.add("titleTexts", titleTexts);
            jo.add("modIntegrations", modIntegrations);
            modIntegrations.add("vanilla", vanilla);
            modIntegrations.add("chunky", chunky);

            PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
            printwriter.print(gson.toJson(jo));
            printwriter.close();
        } catch (IOException ex) {
            Narrator.LOGGER.trace("Couldn't save configuration file", ex);
        }
    }

    private final boolean setMotdDefault = setMotd;
    private final int rolePresetDefault = rolePreset;
    private final boolean teamColorDefault = teamColor;
    private final Formatting huntersColorDefault = huntersColor;
    private final Formatting runnersColorDefault = runnersColor;
    private final boolean waitForRunnerDefault = waitForRunner;
    private final int hunterReleaseTimeDefault = hunterReleaseTime;
    private final int runnerHeadStartDefault = runnerHeadStart;
    private final boolean runnersGlowDefault = runnersGlow;
    private final boolean huntOnDeathDefault = huntOnDeath;
    private final boolean runnersCanPauseDefault = runnersCanPause;
    private final int leavePauseTimeDefault = leavePauseTime;
    private final int timeLimitDefault = timeLimit;
    private final boolean spectateOnWinDefault = spectateOnWin;
    private final boolean customTitlesDefault = customTitles;
    private final boolean customSoundsDefault = customSounds;
    private final boolean customParticlesDefault = customParticles;
    private final boolean nightVisionDefault = nightVision;
    private final int trackerTypeDefault = trackerType;
    private final int friendlyFireDefault = friendlyFire;
    private final boolean runnerPreferencesDefault = runnerPreferences;
    private final boolean bedExplosionsDefault = bedExplosions;
    private final boolean lavaPvpInNetherDefault = lavaPvpInNether;
    private final String startTitleDefault = startTitle;
    private final String startSubtitleDefault = startSubtitle;
    private final String pausedTitleDefault = pausedTitle;
    private final boolean vanillaDefault = vanilla;
    private final Difficulty difficultyDefault = difficulty;
    private final int worldBorderDefault = worldBorder;
    private final int spawnRadiusDefault = spawnRadius;
    private final boolean spectatorsGenerateChunksDefault = spectatorsGenerateChunks;
    private final boolean chunkyDefault = chunky;
    private final int overworldDefault = overworld;
    private final int theNetherDefault = theNether;
    private final int theEndDefault = theEnd;

    public boolean isSetMotdDefault() {
        return setMotdDefault;
    }

    public int getRolePresetDefault() {
        return rolePresetDefault;
    }

    public boolean isTeamColorDefault() {
        return teamColorDefault;
    }

    public Formatting getHuntersColorDefault() {
        return huntersColorDefault;
    }

    public Formatting getRunnersColorDefault() {
        return runnersColorDefault;
    }

    public boolean isWaitForRunnerDefault() {
        return waitForRunnerDefault;
    }

    public int getHunterReleaseTimeDefault() {
        return hunterReleaseTimeDefault;
    }

    public int getRunnerHeadStartDefault() {
        return runnerHeadStartDefault;
    }

    public boolean isRunnersGlowDefault() {
        return runnersGlowDefault;
    }

    public boolean isHuntOnDeathDefault() {
        return huntOnDeathDefault;
    }

    public boolean isRunnersCanPauseDefault() {
        return runnersCanPauseDefault;
    }

    public int getLeavePauseTimeDefault() {
        return leavePauseTimeDefault;
    }

    public int getTimeLimitDefault() {
        return timeLimitDefault;
    }

    public boolean isSpectateOnWinDefault() {
        return spectateOnWinDefault;
    }

    public boolean isCustomTitlesDefault() {
        return customTitlesDefault;
    }

    public boolean isCustomSoundsDefault() {
        return customSoundsDefault;
    }

    public boolean isCustomParticlesDefault() {
        return customParticlesDefault;
    }

    public boolean isNightVisionDefault() {
        return nightVisionDefault;
    }

    public int getTrackerTypeDefault() {
        return trackerTypeDefault;
    }

    public int getFriendlyFireDefault() {
        return friendlyFireDefault;
    }

    public boolean isRunnerPreferencesDefault() {
        return runnerPreferencesDefault;
    }

    public boolean isBedExplosionsDefault() {
        return bedExplosionsDefault;
    }

    public boolean isLavaPvpInNetherDefault() {
        return lavaPvpInNetherDefault;
    }

    public String getStartTitleDefault() {
        return startTitleDefault;
    }

    public String getStartSubtitleDefault() {
        return startSubtitleDefault;
    }

    public String getPausedTitleDefault() {
        return pausedTitleDefault;
    }

    public boolean isVanillaDefault() {
        return vanillaDefault;
    }

    public Difficulty getDifficultyDefault() {
        return difficultyDefault;
    }

    public int getWorldBorderDefault() {
        return worldBorderDefault;
    }

    public int getSpawnRadiusDefault() {
        return spawnRadiusDefault;
    }

    public boolean isSpectatorsGenerateChunksDefault() {
        return spectatorsGenerateChunksDefault;
    }

    public boolean isChunkyDefault() {
        return chunkyDefault;
    }

    public int getOverworldDefault() {
        return overworldDefault;
    }

    public int getTheNetherDefault() {
        return theNetherDefault;
    }

    public int getTheEndDefault() {
        return theEndDefault;
    }
}