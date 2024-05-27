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

    private boolean preloadChunks = false;
    private boolean automaticCompass = false;
    private boolean teamColor = true;
    private Formatting huntersColor = Formatting.RED;
    private Formatting runnersColor = Formatting.GREEN;
    private int runnerHeadstart = 0;
    private int timeLimit = 0;
    private boolean runnerGlow = false;
    private Difficulty gameDifficulty = Difficulty.EASY;
    private int worldBorder = 59999968;
    private int spawnRadius = 10;
    private boolean spectateWin = true;
    private int friendlyFire = 1;
    private boolean bedExplosions = false;
    private boolean lavaPvpInNether = false;
    private boolean spectatorsGenerateChunks = false;
    private boolean runnersHuntOnDeath = true;
    private boolean runnerCanPause = true;
    private int pauseTimeOnLeave = 2;
    private boolean gameTitles = true;
    private boolean manhuntSounds = true;
    private boolean nightVision = true;

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

    public boolean isRunnerGlow() {
        return runnerGlow;
    }

    public void setRunnerGlow(boolean runnersGlow) {
        this.runnerGlow = runnersGlow;
    }

    public Difficulty getGameDifficulty() {
        return gameDifficulty;
    }

    public void setGameDifficulty(Difficulty gameDifficulty) {
        this.gameDifficulty = gameDifficulty;
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

    public boolean isSpectateWin() {
        return spectateWin;
    }

    public void setSpectateWin(boolean spectateWin) {
        this.spectateWin = spectateWin;
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

    public boolean isRunnerCanPause() {
        return runnerCanPause;
    }

    public void setRunnerCanPause(boolean runnerCanPause) {
        this.runnerCanPause = runnerCanPause;
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

            JsonObject gameOptions = jo.getAsJsonObject("gameOptions");
            if ((je = gameOptions.get("automaticCompass")) != null) automaticCompass = je.getAsBoolean();
            if ((je = gameOptions.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = gameOptions.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = gameOptions.get("runnerHeadstart")) != null) runnerHeadstart = je.getAsInt();
            if ((je = gameOptions.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = gameOptions.get("runnersGlow")) != null) runnerGlow = je.getAsBoolean();
            if ((je = gameOptions.get("gameDifficulty")) != null) gameDifficulty = Difficulty.byName(je.getAsString());
            if ((je = gameOptions.get("worldBorder")) != null) worldBorder = je.getAsInt();
            if ((je = gameOptions.get("spawnRadius")) != null) spawnRadius = je.getAsInt();
            if ((je = gameOptions.get("spectateWin")) != null) spectateWin = je.getAsBoolean();
            if ((je = gameOptions.get("friendlyFire")) != null) friendlyFire = je.getAsInt();
            if ((je = gameOptions.get("bedExplosions")) != null) bedExplosions = je.getAsBoolean();
            if ((je = gameOptions.get("lavaPvpInNether")) != null) lavaPvpInNether = je.getAsBoolean();
            if ((je = gameOptions.get("spectatorsGenerateChunks")) != null) spectatorsGenerateChunks = je.getAsBoolean();
            if ((je = gameOptions.get("runnersHuntOnDeath")) != null) runnersHuntOnDeath = je.getAsBoolean();
            if ((je = gameOptions.get("runnerCanPause")) != null) runnerCanPause = je.getAsBoolean();
            if ((je = gameOptions.get("pauseTimeOnLeave")) != null) pauseTimeOnLeave = je.getAsInt();
            if ((je = gameOptions.get("gameTitles")) != null) gameTitles = je.getAsBoolean();
            if ((je = gameOptions.get("manhuntSounds")) != null) manhuntSounds = je.getAsBoolean();
            if ((je = gameOptions.get("nightVision")) != null) nightVision = je.getAsBoolean();
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
            JsonObject gameOptions = new JsonObject();
            gameOptions.add("automaticCompass", new JsonPrimitive(automaticCompass));
            gameOptions.add("teamColor", new JsonPrimitive(teamColor));
            gameOptions.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            gameOptions.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            gameOptions.add("runnerHeadstart", new JsonPrimitive(runnerHeadstart));
            gameOptions.add("timeLimit", new JsonPrimitive(timeLimit));
            gameOptions.add("runnersGlow", new JsonPrimitive(runnerGlow));
            gameOptions.add("gameDifficulty", new JsonPrimitive(gameDifficulty.getName()));
            gameOptions.add("worldBorder", new JsonPrimitive(worldBorder));
            gameOptions.add("spawnRadius", new JsonPrimitive(spawnRadius));
            gameOptions.add("spectateWin", new JsonPrimitive(spectateWin));
            gameOptions.add("friendlyFire", new JsonPrimitive(friendlyFire));
            gameOptions.add("bedExplosions", new JsonPrimitive(bedExplosions));
            gameOptions.add("lavaPvpInNether", new JsonPrimitive(lavaPvpInNether));
            gameOptions.add("spectatorsGenerateChunks", new JsonPrimitive(lavaPvpInNether));
            gameOptions.add("runnersHuntOnDeath", new JsonPrimitive(runnersHuntOnDeath));
            gameOptions.add("runnerCanPause", new JsonPrimitive(runnersCanPauseDefault));
            gameOptions.add("pauseTimeOnLeave", new JsonPrimitive(pauseTimeOnLeave));
            gameOptions.add("gameTitles", new JsonPrimitive(gameTitles));
            gameOptions.add("manhuntSounds", new JsonPrimitive(manhuntSounds));
            gameOptions.add("nightVision", new JsonPrimitive(nightVision));
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
            jo.add("gameOptions", gameOptions);
            jo.add("languageKeys", languageKeys);

            PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
            printwriter.print(gson.toJson(jo));
            printwriter.close();
        } catch (IOException ex) {
            LOGGER.trace("Couldn't save configuration file", ex);
        }
    }

    private final boolean preloadChunksDefault = preloadChunks;
    private final boolean automaticCompassDefault = automaticCompass;
    private final boolean teamColorDefault = teamColor;
    private final Formatting huntersColorDefault = huntersColor;
    private final Formatting runnersColorDefault = runnersColor;
    private final int runnerHeadstartDefault = runnerHeadstart;
    private final int timeLimitDefault = timeLimit;
    private final boolean runnersGlowDefault = runnerGlow;
    private final Difficulty gameDifficultyDefault = gameDifficulty;
    private final int worldBorderDefault = worldBorder;
    private final int spawnRadiusDefault = spawnRadius;
    private final boolean spectateWinDefault = spectateWin;
    private final int friendlyFireDefault = friendlyFire;
    private final boolean bedExplosionsDefault = bedExplosions;
    private final boolean lavaPvpInNetherDefault = lavaPvpInNether;
    private final boolean spectatorsGenerateChunksDefault = spectatorsGenerateChunks;
    private final boolean runnersHuntOnDeathDefault = runnersHuntOnDeath;
    private final boolean runnersCanPauseDefault = runnerCanPause;
    private final int pauseTimeOnLeaveDefault = pauseTimeOnLeave;
    private final boolean gameTitlesDefault = gameTitles;
    private final boolean manhuntSoundsDefault = manhuntSounds;
    private final boolean nightVisionDefault = nightVision;

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

    public int getFriendlyFireDefault() {
        return friendlyFireDefault;
    }

    public boolean isBedExplosionsDefault() {
        return bedExplosionsDefault;
    }

    public boolean isLavaPvpInNetherDefault() {
        return lavaPvpInNetherDefault;
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
}