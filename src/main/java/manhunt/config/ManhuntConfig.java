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

    private long worldSeed = -1;
    private boolean trackerCompass = false;
    private boolean teamColor = true;
    private Formatting huntersColor = Formatting.RED;
    private Formatting runnersColor = Formatting.GREEN;
    private int runnerHeadstart = 0;
    private int timeLimit = 0;
    private Difficulty gameDifficulty = Difficulty.EASY;
    private int worldBorder = 0;

    public long getWorldSeed() {
        return worldSeed;
    }

    public void setWorldSeed(long worldSeed) {
        this.worldSeed = worldSeed;
    }

    public boolean isTrackerCompass() {
        return trackerCompass;
    }

    public void setTrackerCompass(boolean trackerCompass) {
        this.trackerCompass = trackerCompass;
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

    public void setRunnerHeadstart(int runnerHeadstart) {
        this.runnerHeadstart = runnerHeadstart;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
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

    public void load() {
        if (!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            if ((je = jo.get("worldSeed")) != null) worldSeed = je.getAsLong();
            if ((je = jo.get("trackerCompass")) != null) trackerCompass = je.getAsBoolean();
            if ((je = jo.get("huntersColor")) != null) huntersColor = Formatting.byName(je.getAsString());
            if ((je = jo.get("runnersColor")) != null) runnersColor = Formatting.byName(je.getAsString());
            if ((je = jo.get("runnerHeadstart")) != null) runnerHeadstart = je.getAsInt();
            if ((je = jo.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = jo.get("gameDifficulty")) != null) gameDifficulty = Difficulty.byName(je.getAsString());
            if ((je = jo.get("worldBorder")) != null) worldBorder = je.getAsInt();

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
            jo.add("worldSeed", new JsonPrimitive(worldSeed));
            jo.add("trackerCompass", new JsonPrimitive(trackerCompass));
            jo.add("teamColor", new JsonPrimitive(teamColor));
            jo.add("_ColorsList", new JsonPrimitive(String.join(", ", Formatting.getNames(true, false))));
            jo.add("huntersColor", new JsonPrimitive(huntersColor.getName()));
            jo.add("runnersColor", new JsonPrimitive(runnersColor.getName()));
            jo.add("runnerHeadstart", new JsonPrimitive(runnerHeadstart));
            jo.add("timeLimit", new JsonPrimitive(timeLimit));
            jo.add("gameDifficulty", new JsonPrimitive(gameDifficulty.getName()));
            jo.add("worldBorder", new JsonPrimitive(worldBorder));

            PrintWriter printwriter = new PrintWriter(new FileWriter(confFile));
            printwriter.print(gson.toJson(jo));
            printwriter.close();
        } catch (IOException ex) {
            LOGGER.trace("Couldn't save configuration file", ex);
        }
    }
}