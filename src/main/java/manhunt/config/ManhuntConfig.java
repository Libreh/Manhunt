package manhunt.config;

import com.google.gson.*;
import com.mojang.text2speech.Narrator;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class ManhuntConfig {
    public static final ManhuntConfig config = new ManhuntConfig();
    private final File confFile = new File("./config/manhunt.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ManhuntConfig() {
    }

    private boolean setMotd = true;
    private int rolePreset = 1;
    private boolean teamColor = true;
    private Formatting huntersColor = Formatting.RED;
    private Formatting runnersColor = Formatting.GREEN;
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
    private boolean automaticCompass = true;
    private boolean nightVision = true;
    private int friendlyFire = 2;
    private boolean runnerPreferences = true;
    private boolean bedExplosions = true;
    private boolean lavaPvpInNether = true;
    private String startTitle = "Manhunt";
    private String startSubtitle = "Good luck and have fun";
    private String pausedTitle = "Paused";
    private boolean vanilla = true;
    private Difficulty difficulty = Difficulty.EASY;
    private int worldBorder = 59999968;
    private int spawnRadius = 5;
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

    public boolean isAutomaticCompass() {
        return automaticCompass;
    }
    public void setAutomaticCompass(boolean automaticCompass) {
        this.automaticCompass = automaticCompass;
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

            @Nullable JsonObject manhuntSettings = jo.getAsJsonObject("manhuntSettings");
            if ((je = manhuntSettings.get("setMotd")) != null) setMotd = je.getAsBoolean();
            if ((je = manhuntSettings.get("rolePreset")) != null) rolePreset = je.getAsInt();
            if ((je = manhuntSettings.get("teamColor")) != null) teamColor = je.getAsBoolean();
            if ((je = manhuntSettings.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = manhuntSettings.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = manhuntSettings.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = manhuntSettings.get("runnersGlow")) != null) runnersGlow = je.getAsBoolean();
            if ((je = manhuntSettings.get("huntOnDeath")) != null) huntOnDeath = je.getAsBoolean();
            if ((je = manhuntSettings.get("runnersCanPause")) != null) runnersCanPause = je.getAsBoolean();
            if ((je = manhuntSettings.get("leavePauseTime")) != null) leavePauseTime = je.getAsInt();
            if ((je = manhuntSettings.get("spectateOnWin")) != null) spectateOnWin = je.getAsBoolean();
            @Nullable JsonObject globalPreferences = jo.getAsJsonObject("globalPreferences");
            if ((je = globalPreferences.get("customTitles")) != null) customTitles = je.getAsBoolean();
            if ((je = globalPreferences.get("customSounds")) != null) customSounds = je.getAsBoolean();
            if ((je = globalPreferences.get("customParticles")) != null) customParticles = je.getAsBoolean();
            if ((je = globalPreferences.get("automaticCompass")) != null) automaticCompass = je.getAsBoolean();
            if ((je = globalPreferences.get("nightVision")) != null) nightVision = je.getAsBoolean();
            if ((je = globalPreferences.get("friendlyFire")) != null) friendlyFire = je.getAsInt();
            if ((je = globalPreferences.get("bedExplosions")) != null) bedExplosions = je.getAsBoolean();
            if ((je = globalPreferences.get("lavaPvpInNether")) != null) lavaPvpInNether = je.getAsBoolean();
            if ((je = globalPreferences.get("runnerPreferences")) != null) runnerPreferences = je.getAsBoolean();
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
            if ((je = vanilla.get("spectatorsGenerateChunks")) != null) this.spectatorsGenerateChunks = je.getAsBoolean();
            @Nullable JsonObject chunky = modIntegrations.getAsJsonObject("chunky");
            if ((je = chunky.get("enabled")) != null) this.chunky = je.getAsBoolean();
            if ((je = chunky.get("overworld")) != null) overworld = je.getAsInt();
            if ((je = chunky.get("the_nether")) != null) theNether = je.getAsInt();
            if ((je = chunky.get("the_end")) != null) theEnd = je.getAsInt();
        } catch (FileNotFoundException ex) {
            try {
                FileUtils.delete(confFile);
            } catch (Exception ignored) {}
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
            JsonObject manhuntSettings = new JsonObject();
            manhuntSettings.add("setMotd", new JsonPrimitive(setMotd));
            manhuntSettings.add("rolePreset", new JsonPrimitive(rolePreset));
            manhuntSettings.add("teamColor", new JsonPrimitive(teamColor));
            manhuntSettings.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            manhuntSettings.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            manhuntSettings.add("runnerHeadStart", new JsonPrimitive(runnerHeadStart));
            manhuntSettings.add("runnersGlow", new JsonPrimitive(runnersGlow));
            manhuntSettings.add("huntOnDeath", new JsonPrimitive(huntOnDeath));
            manhuntSettings.add("runnersCanPause", new JsonPrimitive(runnersCanPauseDefault));
            manhuntSettings.add("leavePauseTime", new JsonPrimitive(leavePauseTime));
            manhuntSettings.add("timeLimit", new JsonPrimitive(timeLimit));
            manhuntSettings.add("spectateOnWin", new JsonPrimitive(spectateOnWin));
            JsonObject globalPreferences = new JsonObject();
            globalPreferences.add("customTitles", new JsonPrimitive(customTitles));
            globalPreferences.add("customSounds", new JsonPrimitive(customSounds));
            globalPreferences.add("customParticles", new JsonPrimitive(customParticles));
            globalPreferences.add("automaticCompass", new JsonPrimitive(automaticCompass));
            globalPreferences.add("nightVision", new JsonPrimitive(nightVision));
            globalPreferences.add("friendlyFire", new JsonPrimitive(friendlyFire));
            globalPreferences.add("runnerPreferences", new JsonPrimitive(runnerPreferences));
            globalPreferences.add("bedExplosions", new JsonPrimitive(bedExplosions));
            globalPreferences.add("lavaPvpInNether", new JsonPrimitive(lavaPvpInNether));
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
            jo.add("manhuntSettings", manhuntSettings);
            jo.add("globalPreferences", globalPreferences);
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
    private final boolean automaticCompassDefault = automaticCompass;
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
    public boolean isAutomaticCompassDefault() {
        return automaticCompassDefault;
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