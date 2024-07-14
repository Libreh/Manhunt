package manhunt.config;

import com.google.gson.*;
import net.minecraft.util.Formatting;

import java.io.*;

import static manhunt.ManhuntMod.LOGGER;

public class ManhuntConfig {
    public static final ManhuntConfig INSTANCE = new ManhuntConfig();
    private final File confFile = new File("./config/manhunt.json");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ManhuntConfig() {
    }

    private boolean setMotd = true;
    private boolean automaticCompass = false;
    private int runnerHeadstart = 0;
    private int timeLimit = 240;
    private int worldBorder = 59999968;
    private int teamPreset = 1;
    private boolean teamColor = true;
    private Formatting huntersColor = Formatting.RED;
    private Formatting runnersColor = Formatting.GREEN;
    private boolean nametagColor = false;
    private boolean runnersGlow = false;
    private boolean runnersHuntOnDeath = true;
    private boolean runnersCanPause = true;
    private int runnerLeavingPauseTime = 5;
    private boolean spectateOnWin = false;
    private boolean gameTitles = true;
    private boolean manhuntSounds = true;
    private boolean nightVision = true;
    private int friendlyFire = 2;
    private boolean bedExplosions = true;
    private boolean lavaPvpInNether = true;
    private boolean runnerPreferences = true;
    private boolean chunky = true;
    private int overworld = 8000;
    private int nether = 1000;
    private int end = 0;

    public boolean isSetMotd() {
        return setMotd;
    }

    public void setSetMotd(boolean setMotd) {
        this.setMotd = setMotd;
    }

    public int getTeamPreset() {
        return teamPreset;
    }

    public void setTeamPreset(int teamPreset) {
        this.teamPreset = teamPreset;
    }

    public boolean isChunky() {
        return chunky;
    }

    public void setChunky(boolean chunky) {
        this.chunky = chunky;
    }

    public int getNether() {
        return nether;
    }

    public void setNether(int nether) {
        this.nether = nether;
    }

    public int getOverworld() {
        return overworld;
    }

    public void setOverworld(int overworld) {
        this.overworld = overworld;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
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

    public boolean isNametagColor() {
        return nametagColor;
    }

    public void setNametagColor(boolean nametagColor) {
        this.nametagColor = nametagColor;
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

    public int getWorldBorder() {
        return worldBorder;
    }

    public void setWorldBorder(int worldBorder) {
        this.worldBorder = worldBorder;
    }

    public boolean isSpectateOnWin() {
        return spectateOnWin;
    }

    public void setSpectateOnWin(boolean spectateOnWin) {
        this.spectateOnWin = spectateOnWin;
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

    public int getRunnerLeavingPauseTime() {
        return runnerLeavingPauseTime;
    }

    public void setRunnerLeavingPauseTime(int runnerLeavingPauseTime) {
        this.runnerLeavingPauseTime = runnerLeavingPauseTime;
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

    private String gameStartTitle = "Manhunt";
    private String gameStartSubtitle = "Good luck and have fun";
    private String gamePausedTitle = "Game paused";

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

    public String getGamePausedTitle() {
        return gamePausedTitle;
    }

    public void setGamePausedTitle(String gamePausedTitle) {
        this.gamePausedTitle = gamePausedTitle;
    }

    public void load() {
        if (!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            JsonObject gameSettings = jo.getAsJsonObject("gameSettings");
            if ((je = gameSettings.get("setMotd")) != null) setMotd = je.getAsBoolean();
            if ((je = gameSettings.get("teamPreset")) != null) teamPreset = je.getAsInt();
            if ((je = gameSettings.get("automaticCompass")) != null) automaticCompass = je.getAsBoolean();
            if ((je = gameSettings.get("teamColor")) != null) teamColor = je.getAsBoolean();
            if ((je = gameSettings.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = gameSettings.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = gameSettings.get("nameColorToTeamColor")) != null) nametagColor = je.getAsBoolean();
            if ((je = gameSettings.get("runnerHeadstart")) != null) runnerHeadstart = je.getAsInt();
            if ((je = gameSettings.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = gameSettings.get("runnersGlow")) != null) runnersGlow = je.getAsBoolean();
            if ((je = gameSettings.get("worldBorder")) != null) worldBorder = je.getAsInt();
            if ((je = gameSettings.get("spectateOnWin")) != null) spectateOnWin = je.getAsBoolean();
            if ((je = gameSettings.get("runnersHuntOnDeath")) != null) runnersHuntOnDeath = je.getAsBoolean();
            if ((je = gameSettings.get("runnersCanPause")) != null) runnersCanPause = je.getAsBoolean();
            if ((je = gameSettings.get("runnerLeavingPauseTime")) != null) runnerLeavingPauseTime = je.getAsInt();
            JsonObject globalPreferences = jo.getAsJsonObject("globalPreferences");
            if ((je = globalPreferences.get("gameTitles")) != null) gameTitles = je.getAsBoolean();
            if ((je = globalPreferences.get("manhuntSounds")) != null) manhuntSounds = je.getAsBoolean();
            if ((je = globalPreferences.get("nightVision")) != null) nightVision = je.getAsBoolean();
            if ((je = globalPreferences.get("friendlyFire")) != null) friendlyFire = je.getAsInt();
            if ((je = globalPreferences.get("bedExplosions")) != null) bedExplosions = je.getAsBoolean();
            if ((je = globalPreferences.get("lavaPvpInNether")) != null) lavaPvpInNether = je.getAsBoolean();
            if ((je = globalPreferences.get("runnerPreferences")) != null) runnerPreferences = je.getAsBoolean();
            JsonObject titleTexts = jo.getAsJsonObject("titleTexts");
            if ((je = titleTexts.get("gameStartTitle")) != null) gameStartTitle = je.getAsString();
            if ((je = titleTexts.get("gameStartSubtitle")) != null) gameStartSubtitle = je.getAsString();
            if ((je = titleTexts.get("gamePausedTitle")) != null) gamePausedTitle = je.getAsString();
            JsonObject modIntegrations = jo.getAsJsonObject("modIntegrations");
            JsonObject chunky = modIntegrations.getAsJsonObject("chunky");
            if ((je = chunky.get("enabled")) != null) this.chunky = je.getAsBoolean();
            if ((je = chunky.get("overworld")) != null) overworld = je.getAsInt();
            if ((je = chunky.get("the_nether")) != null) nether = je.getAsInt();
            if ((je = chunky.get("the_end")) != null) end = je.getAsInt();
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
            gameSettings.add("setMotd", new JsonPrimitive(setMotd));
            gameSettings.add("teamPreset", new JsonPrimitive(teamPreset));
            gameSettings.add("automaticCompass", new JsonPrimitive(automaticCompass));
            gameSettings.add("teamColor", new JsonPrimitive(teamColor));
            gameSettings.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            gameSettings.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            gameSettings.add("nameColorToTeamColor", new JsonPrimitive(nametagColor));
            gameSettings.add("runnerHeadstart", new JsonPrimitive(runnerHeadstart));
            gameSettings.add("timeLimit", new JsonPrimitive(timeLimit));
            gameSettings.add("runnersGlow", new JsonPrimitive(runnersGlow));
            gameSettings.add("worldBorder", new JsonPrimitive(worldBorder));
            gameSettings.add("spectateWin", new JsonPrimitive(spectateOnWin));
            gameSettings.add("runnersHuntOnDeath", new JsonPrimitive(runnersHuntOnDeath));
            gameSettings.add("runnersCanPause", new JsonPrimitive(runnersCanPauseDefault));
            gameSettings.add("runnerLeavingPauseTime", new JsonPrimitive(runnerLeavingPauseTime));
            JsonObject globalPreferences = new JsonObject();
            globalPreferences.add("gameTitles", new JsonPrimitive(gameTitles));
            globalPreferences.add("manhuntSounds", new JsonPrimitive(manhuntSounds));
            globalPreferences.add("nightVision", new JsonPrimitive(nightVision));
            globalPreferences.add("friendlyFire", new JsonPrimitive(friendlyFire));
            globalPreferences.add("bedExplosions", new JsonPrimitive(bedExplosions));
            globalPreferences.add("lavaPvpInNether", new JsonPrimitive(lavaPvpInNether));
            globalPreferences.add("runnerPreferences", new JsonPrimitive(runnerPreferences));
            JsonObject titleTexts = new JsonObject();
            titleTexts.add("gameStartTitle", new JsonPrimitive(gameStartTitle));
            titleTexts.add("gameStartSubtitle", new JsonPrimitive(gameStartSubtitle));
            titleTexts.add("gamePausedTitle", new JsonPrimitive(gamePausedTitle));
            JsonObject modIntegrations = new JsonObject();
            JsonObject chunky = new JsonObject();
            chunky.add("enabled", new JsonPrimitive(this.chunky));
            chunky.add("overworld", new JsonPrimitive(overworld));
            chunky.add("the_nether", new JsonPrimitive(nether));
            chunky.add("the_end", new JsonPrimitive(end));
            jo.add("gameSettings", gameSettings);
            jo.add("globalPreferences", globalPreferences);
            jo.add("titleTexts", titleTexts);
            jo.add("modIntegrations", modIntegrations);
            modIntegrations.add("chunky", chunky);

            PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
            printwriter.print(gson.toJson(jo));
            printwriter.close();
        } catch (IOException ex) {
            LOGGER.trace("Couldn't save configuration file", ex);
        }
    }

    private final boolean setMotdDefault = setMotd;
    private final int teamPresetDefault = teamPreset;
    private final boolean chunkyIntegrationDefault = chunky;
    private final int overworldDefault = overworld;
    private final int netherDefault = nether;
    private final int endDefault = end;
    private final boolean automaticCompassDefault = automaticCompass;
    private final boolean teamColorDefault = teamColor;
    private final Formatting huntersColorDefault = huntersColor;
    private final Formatting runnersColorDefault = runnersColor;
    private final boolean nameTagDefault = nametagColor;
    private final int runnerHeadstartDefault = runnerHeadstart;
    private final int timeLimitDefault = timeLimit;
    private final boolean runnersGlowDefault = runnersGlow;
    private final int worldBorderDefault = worldBorder;
    private final boolean spectateWinDefault = spectateOnWin;
    private final boolean runnersHuntOnDeathDefault = runnersHuntOnDeath;
    private final boolean runnersCanPauseDefault = runnersCanPause;
    private final int runnerLeavingPauseTimeDefault = runnerLeavingPauseTime;
    private final boolean gameTitlesDefault = gameTitles;
    private final boolean manhuntSoundsDefault = manhuntSounds;
    private final boolean nightVisionDefault = nightVision;
    private final int friendlyFireDefault = friendlyFire;
    private final boolean bedExplosionsDefault = bedExplosions;
    private final boolean lavaPvpInNetherDefault = lavaPvpInNether;
    private final boolean runnerPreferencesDefault = runnerPreferences;

    public boolean isSetMotdDefault() {
        return setMotdDefault;
    }

    public int getTeamPresetDefault() {
        return teamPresetDefault;
    }

    public boolean isChunkyIntegrationDefault() {
        return chunkyIntegrationDefault;
    }

    public int getOverworldDefault() {
        return overworldDefault;
    }

    public int getNetherDefault() {
        return netherDefault;
    }

    public int getEndDefault() {
        return endDefault;
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

    public boolean isNameTagDefault() {
        return nameTagDefault;
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

    public int getWorldBorderDefault() {
        return worldBorderDefault;
    }

    public boolean isSpectateWinDefault() {
        return spectateWinDefault;
    }

    public boolean isRunnersHuntOnDeathDefault() {
        return runnersHuntOnDeathDefault;
    }

    public boolean isRunnersCanPauseDefault() {
        return runnersCanPauseDefault;
    }

    public int getRunnerLeavingPauseTimeDefault() {
        return runnerLeavingPauseTimeDefault;
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

    private final String gameStartTitleDefault = gameStartTitle;
    private final String gameStartSubtitleDefault = gameStartSubtitle;
    private final String gamePausedTitleDefault = gamePausedTitle;

    public String getGameStartTitleDefault() {
        return gameStartTitleDefault;
    }

    public String getGameStartSubtitleDefault() {
        return gameStartSubtitleDefault;
    }

    public String getGamePausedTitleDefault() {
        return gamePausedTitleDefault;
    }
}