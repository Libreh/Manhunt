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
    private int runnersHeadstart = 0;
    private int timeLimit = 0;
    private boolean runnersGlow = false;
    private Difficulty gameDifficulty = Difficulty.EASY;
    private int worldBorder = 0;
    private int spawnRadius = 10;
    private boolean spectateWin = true;
    private int friendlyFire = 1;
    private boolean bedExplosions = false;
    private boolean lavaPvpInNether = false;
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

    public int getRunnersHeadstart() {
        return runnersHeadstart;
    }

    public void setRunnersHeadstart(int runnersHeadstart) {
        this.runnersHeadstart = runnersHeadstart;
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

    public void load() {
        if (!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            if ((je = jo.get("automaticCompass")) != null) automaticCompass = je.getAsBoolean();
            if ((je = jo.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = jo.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = jo.get("runnersHeadstart")) != null) runnersHeadstart = je.getAsInt();
            if ((je = jo.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = jo.get("runnersGlow")) != null) runnersGlow = je.getAsBoolean();
            if ((je = jo.get("gameDifficulty")) != null) gameDifficulty = Difficulty.byName(je.getAsString());
            if ((je = jo.get("worldBorder")) != null) worldBorder = je.getAsInt();
            if ((je = jo.get("spawnRadius")) != null) spawnRadius = je.getAsInt();
            if ((je = jo.get("spectateWin")) != null) spectateWin = je.getAsBoolean();
            if ((je = jo.get("friendlyFire")) != null) friendlyFire = je.getAsInt();
            if ((je = jo.get("bedExplosions")) != null) bedExplosions = je.getAsBoolean();
            if ((je = jo.get("lavaPvpInNether")) != null) lavaPvpInNether = je.getAsBoolean();
            if ((je = jo.get("gameTitles")) != null) gameTitles = je.getAsBoolean();
            if ((je = jo.get("manhuntSounds")) != null) manhuntSounds = je.getAsBoolean();
            if ((je = jo.get("nightVision")) != null) nightVision = je.getAsBoolean();

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
            jo.add("automaticCompass", new JsonPrimitive(automaticCompass));
            jo.add("teamColor", new JsonPrimitive(teamColor));
            jo.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            jo.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            jo.add("runnerHeadstart", new JsonPrimitive(runnersHeadstart));
            jo.add("timeLimit", new JsonPrimitive(timeLimit));
            jo.add("runnersGlow", new JsonPrimitive(runnersGlow));
            jo.add("gameDifficulty", new JsonPrimitive(gameDifficulty.getName()));
            jo.add("worldBorder", new JsonPrimitive(worldBorder));
            jo.add("spawnRadius", new JsonPrimitive(spawnRadius));
            jo.add("spectateWin", new JsonPrimitive(spectateWin));
            jo.add("friendlyFire", new JsonPrimitive(friendlyFire));
            jo.add("bedExplosions", new JsonPrimitive(bedExplosions));
            jo.add("lavaPvpInNether", new JsonPrimitive(lavaPvpInNether));
            jo.add("gameTitles", new JsonPrimitive(gameTitles));
            jo.add("manhuntSounds", new JsonPrimitive(manhuntSounds));
            jo.add("nightVision", new JsonPrimitive(nightVision));

            PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
            printwriter.print(gson.toJson(jo));
            printwriter.close();
        } catch (IOException ex) {
            LOGGER.trace("Couldn't save configuration file", ex);
        }
    }
}