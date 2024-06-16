package manhunt.config;

import com.google.gson.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.Difficulty;

import java.io.*;

import static manhunt.ManhuntMod.LOGGER;

public class ManhuntConfig {
    public static final ManhuntConfig INSTANCE = new ManhuntConfig();
    private final File confFile = new File("./config/manhunt.json");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ManhuntConfig() {
    }

    private int teamPreset = 1;
    private boolean preloadChunks = true;
    private boolean automaticCompass = true;
    private boolean teamColor = true;
    private Formatting huntersColor = Formatting.RED;
    private Formatting runnersColor = Formatting.GREEN;
    private boolean teamSuffix = true;
    private int runnerHeadstart = 0;
    private int timeLimit = 0;
    private boolean runnersGlow = false;
    private Difficulty difficulty = Difficulty.EASY;
    private int worldBorder = 59999968;
    private int spawnRadius = 0;
    private boolean spectateOnWin = true;
    private boolean spectatorsGenerateChunks = false;
    private boolean runnersHuntOnDeath = true;
    private boolean runnersCanPause = true;
    private int pauseTimeOnLeave = 2;
    private boolean gameTitles = true;
    private boolean manhuntSounds = true;
    private boolean nightVision = true;
    private int friendlyFire = 1;
    private boolean bedExplosions = false;
    private boolean lavaPvpInNether = false;
    private boolean runnerPreferences = true;

    public int getTeamPreset() {
        return teamPreset;
    }

    public void setTeamPreset(int teamPreset) {
        this.teamPreset = teamPreset;
    }

    public boolean isPreloadChunks() {
        return preloadChunks;
    }

    public void setPreloadChunks(boolean preloadChunks) {
        this.preloadChunks = preloadChunks;
    }

    public boolean isAutomaticCompass() {
        return automaticCompass;
    }

    public void setAutomaticCompass(boolean automaticCompass) {
        this.automaticCompass = automaticCompass;
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

    public boolean isTeamSuffix() {
        return teamSuffix;
    }

    public void setTeamSuffix(boolean teamSuffix) {
        this.teamSuffix = teamSuffix;
    }

    public int getRunnerHeadstart() {
        return runnerHeadstart;
    }

    public void setRunnerHeadstart(int runnersHeadstart) {
        this.runnerHeadstart = runnersHeadstart;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isRunnersGlow() {
        return runnersGlow;
    }

    public void setRunnersGlow(boolean runnersGlow) {
        this.runnersGlow = runnersGlow;
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

    public boolean isSpectateOnWin() {
        return spectateOnWin;
    }

    public void setSpectateOnWin(boolean spectateOnWin) {
        this.spectateOnWin = spectateOnWin;
    }

    public boolean isSpectatorsGenerateChunks() {
        return spectatorsGenerateChunks;
    }

    public void setSpectatorsGenerateChunks(boolean spectatorsGenerateChunks) {
        this.spectatorsGenerateChunks = spectatorsGenerateChunks;
    }

    public boolean isRunnersHuntOnDeath() {
        return runnersHuntOnDeath;
    }

    public void setRunnersHuntOnDeath(boolean runnersHuntOnDeath) {
        this.runnersHuntOnDeath = runnersHuntOnDeath;
    }

    public boolean isRunnersCanPause() {
        return runnersCanPause;
    }

    public void setRunnersCanPause(boolean runnersCanPause) {
        this.runnersCanPause = runnersCanPause;
    }

    public int getPauseTimeOnLeave() {
        return pauseTimeOnLeave;
    }

    public void setPauseTimeOnLeave(int pauseTimeOnLeave) {
        this.pauseTimeOnLeave = pauseTimeOnLeave;
    }

    public boolean isGameTitles() {
        return gameTitles;
    }

    public void setGameTitles(boolean gameTitles) {
        this.gameTitles = gameTitles;
    }

    public boolean isManhuntSounds() {
        return manhuntSounds;
    }

    public void setManhuntSounds(boolean manhuntSounds) {
        this.manhuntSounds = manhuntSounds;
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

    public boolean isRunnerPreferences() {
        return runnerPreferences;
    }

    public void setRunnerPreferences(boolean runnerPreferences) {
        this.runnerPreferences = runnerPreferences;
    }

    private String gameStartTitle = "MANHUNT";
    private String gameStartSubtitle = "Good luck have fun";
    private String hunterWinTitle = "HUNTERS WON";
    private String runnerDiedSubtitle = "Runner was slain";
    private String timeLimitSubtitle = "Time limit was reached";
    private String runnerWinTitle = "RUNNERS WON";
    private String enderDragonDiedSubtitle = "Ender Dragon was slain";
    private String gamePausedTitle = "GAME PAUSED";
    private String gamePausedSubtitle = "Back in no time surely";
    private String gameUnpausedTitle = "GAME UNPAUSED";
    private String gameUnpausedSubtitle = "I suggest you run";

    public String getGameStartTitle() {
        return gameStartTitle;
    }

    public void setGameStartTitle(String gameStartTitle) {
        this.gameStartTitle = gameStartTitle;
    }

    public String getGameStartSubtitle() {
        return gameStartSubtitle;
    }

    public void setGameStartSubtitle(String gameStartSubtitle) {
        this.gameStartSubtitle = gameStartSubtitle;
    }

    public String getHunterWinTitle() {
        return hunterWinTitle;
    }

    public void setHunterWinTitle(String hunterWinTitle) {
        this.hunterWinTitle = hunterWinTitle;
    }

    public String getRunnerDiedSubtitle() {
        return runnerDiedSubtitle;
    }

    public void setRunnerDiedSubtitle(String runnerDiedSubtitle) {
        this.runnerDiedSubtitle = runnerDiedSubtitle;
    }

    public String getTimeLimitSubtitle() {
        return timeLimitSubtitle;
    }

    public void setTimeLimitSubtitle(String timeLimitSubtitle) {
        this.timeLimitSubtitle = timeLimitSubtitle;
    }

    public String getRunnerWinTitle() {
        return runnerWinTitle;
    }

    public void setRunnerWinTitle(String runnerWinTitle) {
        this.runnerWinTitle = runnerWinTitle;
    }

    public String getEnderDragonDiedSubtitle() {
        return enderDragonDiedSubtitle;
    }

    public void setEnderDragonDiedSubtitle(String enderDragonDiedSubtitle) {
        this.enderDragonDiedSubtitle = enderDragonDiedSubtitle;
    }

    public String getGamePausedTitle() {
        return gamePausedTitle;
    }

    public void setGamePausedTitle(String gamePausedTitle) {
        this.gamePausedTitle = gamePausedTitle;
    }

    public String getGamePausedSubtitle() {
        return gamePausedSubtitle;
    }

    public void setGamePausedSubtitle(String gamePausedSubtitle) {
        this.gamePausedSubtitle = gamePausedSubtitle;
    }

    public String getGameUnpausedTitle() {
        return gameUnpausedTitle;
    }

    public void setGameUnpausedTitle(String gameUnpausedTitle) {
        this.gameUnpausedTitle = gameUnpausedTitle;
    }

    public String getGameUnpausedSubtitle() {
        return gameUnpausedSubtitle;
    }

    public void setGameUnpausedSubtitle(String gameUnpausedSubtitle) {
        this.gameUnpausedSubtitle = gameUnpausedSubtitle;
    }

    public void load() {
        if (!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            JsonObject gameSettings = jo.getAsJsonObject("gameSettings");
            if ((je = gameSettings.get("teamPreset")) != null) teamPreset = je.getAsInt();
            if ((je = gameSettings.get("preloadChunks")) != null) preloadChunks = je.getAsBoolean();
            if ((je = gameSettings.get("automaticCompass")) != null) automaticCompass = je.getAsBoolean();
            if ((je = gameSettings.get("teamColor")) != null) teamColor = je.getAsBoolean();
            if ((je = gameSettings.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = gameSettings.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = gameSettings.get("teamSuffix")) != null) teamSuffix = je.getAsBoolean();
            if ((je = gameSettings.get("runnerHeadstart")) != null) runnerHeadstart = je.getAsInt();
            if ((je = gameSettings.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = gameSettings.get("runnersGlow")) != null) runnersGlow = je.getAsBoolean();
            if ((je = gameSettings.get("difficulty")) != null) difficulty = Difficulty.byName(je.getAsString());
            if ((je = gameSettings.get("worldBorder")) != null) worldBorder = je.getAsInt();
            if ((je = gameSettings.get("spawnRadius")) != null) spawnRadius = je.getAsInt();
            if ((je = gameSettings.get("spectateOnWin")) != null) spectateOnWin = je.getAsBoolean();
            if ((je = gameSettings.get("spectatorsGenerateChunks")) != null) spectatorsGenerateChunks = je.getAsBoolean();
            if ((je = gameSettings.get("runnersHuntOnDeath")) != null) runnersHuntOnDeath = je.getAsBoolean();
            if ((je = gameSettings.get("runnersCanPause")) != null) runnersCanPause = je.getAsBoolean();
            if ((je = gameSettings.get("pauseTimeOnLeave")) != null) pauseTimeOnLeave = je.getAsInt();
            if ((je = gameSettings.get("gameTitles")) != null) gameTitles = je.getAsBoolean();
            if ((je = gameSettings.get("manhuntSounds")) != null) manhuntSounds = je.getAsBoolean();
            if ((je = gameSettings.get("nightVision")) != null) nightVision = je.getAsBoolean();
            if ((je = gameSettings.get("friendlyFire")) != null) friendlyFire = je.getAsInt();
            if ((je = gameSettings.get("bedExplosions")) != null) bedExplosions = je.getAsBoolean();
            if ((je = gameSettings.get("lavaPvpInNether")) != null) lavaPvpInNether = je.getAsBoolean();
            if ((je = gameSettings.get("runnerPreferences")) != null) runnerPreferences = je.getAsBoolean();
            JsonObject languageKeys = jo.getAsJsonObject("languageKeys");
            if ((je = languageKeys.get("gameStartTitle")) != null) gameStartTitle = je.getAsString();
            if ((je = languageKeys.get("gameStartSubtitle")) != null) gameStartSubtitle = je.getAsString();
            if ((je = languageKeys.get("hunterWinTitle")) != null) hunterWinTitle = je.getAsString();
            if ((je = languageKeys.get("runnerDiedSubtitle")) != null) runnerDiedSubtitle = je.getAsString();
            if ((je = languageKeys.get("timeLimitSubtitle")) != null) timeLimitSubtitle = je.getAsString();
            if ((je = languageKeys.get("runnerWinTitle")) != null) runnerWinTitle = je.getAsString();
            if ((je = languageKeys.get("enderDragonDiedSubtitle")) != null) enderDragonDiedSubtitle = je.getAsString();
            if ((je = languageKeys.get("gamePausedTitle")) != null) gamePausedTitle = je.getAsString();
            if ((je = languageKeys.get("gamePausedSubtitle")) != null) gamePausedSubtitle = je.getAsString();
            if ((je = languageKeys.get("gameUnpausedTitle")) != null) gameUnpausedTitle = je.getAsString();
            if ((je = languageKeys.get("gameUnpausedSubtitle")) != null) gameUnpausedSubtitle = je.getAsString();
        } catch (FileNotFoundException ex) {
            LOGGER.trace("Couldn't load configuration file", ex);
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
            JsonObject gameSettings = new JsonObject();
            gameSettings.add("teamPreset", new JsonPrimitive(teamPreset));
            gameSettings.add("preloadChunks", new JsonPrimitive(preloadChunks));
            gameSettings.add("automaticCompass", new JsonPrimitive(automaticCompass));
            gameSettings.add("teamColor", new JsonPrimitive(teamColor));
            gameSettings.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            gameSettings.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            gameSettings.add("teamSuffix", new JsonPrimitive(teamSuffix));
            gameSettings.add("runnerHeadstart", new JsonPrimitive(runnerHeadstart));
            gameSettings.add("timeLimit", new JsonPrimitive(timeLimit));
            gameSettings.add("runnersGlow", new JsonPrimitive(runnersGlow));
            gameSettings.add("difficulty", new JsonPrimitive(difficulty.getName()));
            gameSettings.add("worldBorder", new JsonPrimitive(worldBorder));
            gameSettings.add("spawnRadius", new JsonPrimitive(spawnRadius));
            gameSettings.add("spectateWin", new JsonPrimitive(spectateOnWin));
            gameSettings.add("spectatorsGenerateChunks", new JsonPrimitive(lavaPvpInNether));
            gameSettings.add("runnersHuntOnDeath", new JsonPrimitive(runnersHuntOnDeath));
            gameSettings.add("runnersCanPause", new JsonPrimitive(runnersCanPauseDefault));
            gameSettings.add("pauseTimeOnLeave", new JsonPrimitive(pauseTimeOnLeave));
            gameSettings.add("gameTitles", new JsonPrimitive(gameTitles));
            gameSettings.add("manhuntSounds", new JsonPrimitive(manhuntSounds));
            gameSettings.add("nightVision", new JsonPrimitive(nightVision));
            gameSettings.add("friendlyFire", new JsonPrimitive(friendlyFire));
            gameSettings.add("bedExplosions", new JsonPrimitive(bedExplosions));
            gameSettings.add("lavaPvpInNether", new JsonPrimitive(lavaPvpInNether));
            gameSettings.add("runnerPreferences", new JsonPrimitive(runnerPreferences));
            JsonObject languageKeys = new JsonObject();
            languageKeys.add("gameStartTitle", new JsonPrimitive(gameStartTitle));
            languageKeys.add("gameStartSubtitle", new JsonPrimitive(gameStartSubtitle));
            languageKeys.add("hunterWinTitle", new JsonPrimitive(hunterWinTitle));
            languageKeys.add("runnerDiedSubtitle", new JsonPrimitive(runnerDiedSubtitle));
            languageKeys.add("timeLimitSubtitle", new JsonPrimitive(timeLimitSubtitle));
            languageKeys.add("runnerWinTitle", new JsonPrimitive(runnerWinTitle));
            languageKeys.add("enderDragonDiedSubtitle", new JsonPrimitive(enderDragonDiedSubtitle));
            languageKeys.add("gamePausedTitle", new JsonPrimitive(gamePausedTitle));
            languageKeys.add("gamePausedSubtitle", new JsonPrimitive(gamePausedSubtitle));
            languageKeys.add("gameUnpausedTitle", new JsonPrimitive(gameUnpausedTitle));
            languageKeys.add("gameUnpausedSubtitle", new JsonPrimitive(gameUnpausedSubtitle));
            jo.add("gameSettings", gameSettings);
            jo.add("languageKeys", languageKeys);

            PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
            printwriter.print(gson.toJson(jo));
            printwriter.close();
        } catch (IOException ex) {
            LOGGER.trace("Couldn't save configuration file", ex);
        }
    }

    private final int teamPresetDefault = teamPreset;
    private final boolean preloadChunksDefault = preloadChunks;
    private final boolean automaticCompassDefault = automaticCompass;
    private final boolean teamColorDefault = teamColor;
    private final Formatting huntersColorDefault = huntersColor;
    private final Formatting runnersColorDefault = runnersColor;
    private final boolean teamSuffixDefault = teamSuffix;
    private final int runnerHeadstartDefault = runnerHeadstart;
    private final int timeLimitDefault = timeLimit;
    private final boolean runnersGlowDefault = runnersGlow;
    private final Difficulty gameDifficultyDefault = difficulty;
    private final int worldBorderDefault = worldBorder;
    private final int spawnRadiusDefault = spawnRadius;
    private final boolean spectateWinDefault = spectateOnWin;
    private final boolean spectatorsGenerateChunksDefault = spectatorsGenerateChunks;
    private final boolean runnersHuntOnDeathDefault = runnersHuntOnDeath;
    private final boolean runnersCanPauseDefault = runnersCanPause;
    private final int pauseTimeOnLeaveDefault = pauseTimeOnLeave;
    private final boolean gameTitlesDefault = gameTitles;
    private final boolean manhuntSoundsDefault = manhuntSounds;
    private final boolean nightVisionDefault = nightVision;
    private final int friendlyFireDefault = friendlyFire;
    private final boolean bedExplosionsDefault = bedExplosions;
    private final boolean lavaPvpInNetherDefault = lavaPvpInNether;
    private final boolean runnerPreferencesDefault = runnerPreferences;

    public int getTeamPresetDefault() {
        return teamPresetDefault;
    }

    public boolean isPreloadChunksDefault() {
        return preloadChunksDefault;
    }

    public boolean isAutomaticCompassDefault() {
        return automaticCompassDefault;
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

    public boolean isTeamSuffixDefault() {
        return teamSuffixDefault;
    }

    public int getRunnerHeadstartDefault() {
        return runnerHeadstartDefault;
    }

    public int getTimeLimitDefault() {
        return timeLimitDefault;
    }

    public boolean isRunnersGlowDefault() {
        return runnersGlowDefault;
    }

    public Difficulty getGameDifficultyDefault() {
        return gameDifficultyDefault;
    }

    public int getWorldBorderDefault() {
        return worldBorderDefault;
    }

    public int getSpawnRadiusDefault() {
        return spawnRadiusDefault;
    }

    public boolean isSpectateWinDefault() {
        return spectateWinDefault;
    }

    public boolean isSpectatorsGenerateChunksDefault() {
        return spectatorsGenerateChunksDefault;
    }

    public boolean isRunnersHuntOnDeathDefault() {
        return runnersHuntOnDeathDefault;
    }

    public boolean isRunnerCanPauseDefault() {
        return runnersCanPauseDefault;
    }

    public int getPauseTimeOnLeaveDefault() {
        return pauseTimeOnLeaveDefault;
    }

    public boolean isGameTitlesDefault() {
        return gameTitlesDefault;
    }

    public boolean isManhuntSoundsDefault() {
        return manhuntSoundsDefault;
    }

    public boolean isNightVisionDefault() {
        return nightVisionDefault;
    }

    public int getFriendlyFireDefault() {
        return friendlyFireDefault;
    }

    public boolean isBedExplosionsDefault() {
        return bedExplosionsDefault;
    }

    public boolean isLavaPvpInNetherDefault() {
        return lavaPvpInNetherDefault;
    }

    public boolean isRunnerPreferencesDefault() {
        return runnerPreferencesDefault;
    }
}