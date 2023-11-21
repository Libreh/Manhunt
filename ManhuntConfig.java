package manhunt.config;

import com.google.gson.*;
import manhunt.Manhunt;

import java.io.*;

public class ManhuntConfig {

    private ManhuntConfig() {
    }
    private static final File confFile = new File("./config/manhunt.json");
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static String presetSelection = "Casual";
    public static String setRoles = "Free Select";
    public static int hunterFreeze = 10;
    public static int timeLimit = 180;
    public static String compassUpdate = "Automatic";
    public static String latePlayers = "Join Hunters";
    public static String whenRunnersDie = "Join Hunters";
    public static boolean showTeamColor = true;
    public static String worldDifficulty = "easy";
    public static int borderSize = 59999968;
    public static boolean showGameTitles = true;

    public static void load() {
        if(!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            if ((je = jo.get("setRoles")) != null) setRoles = je.getAsString();
            if ((je = jo.get("hunterFreeze")) != null) hunterFreeze = je.getAsInt();
            if ((je = jo.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = jo.get("compassUpdate")) != null) compassUpdate = je.getAsString();
            if ((je = jo.get("latePlayers")) != null) latePlayers = je.getAsString();
            if ((je = jo.get("whenRunnersDie")) != null) whenRunnersDie = je.getAsString();
            if ((je = jo.get("showTeamColor")) != null) showTeamColor = je.getAsBoolean();
            if ((je = jo.get("worldDifficulty")) != null) worldDifficulty = je.getAsString();
            if ((je = jo.get("borderSize")) != null) borderSize = je.getAsInt();
            if ((je = jo.get("showGameTitles")) != null) showGameTitles = je.getAsBoolean();
        } catch (FileNotFoundException ex) {
            Manhunt.LOGGER.error("Couldn't load configuration file");
        }
        save();
    }

    public static void save() {
        try {
            if (!confFile.exists()) {
                confFile.getParentFile().mkdirs();
                confFile.createNewFile();
            }

            JsonObject jo = new JsonObject();
            jo.add("setRoles", new JsonPrimitive(setRoles));
            jo.add("hunterFreeze", new JsonPrimitive(hunterFreeze));
            jo.add("timeLimit", new JsonPrimitive(timeLimit));
            jo.add("compassUpdate", new JsonPrimitive(compassUpdate));
            jo.add("latePlayers", new JsonPrimitive(latePlayers));
            jo.add("whenRunnersDie", new JsonPrimitive(whenRunnersDie));
            jo.add("showTeamColor", new JsonPrimitive(showTeamColor));
            jo.add("worldDifficulty", new JsonPrimitive(worldDifficulty));
            jo.add("borderSize", new JsonPrimitive(borderSize));
            jo.add("gameTitles", new JsonPrimitive(showGameTitles));

            PrintWriter printWriter = new PrintWriter(new FileWriter(confFile));
            printWriter.print(gson.toJson(jo));
            printWriter.close();
        } catch (IOException ex) {
            Manhunt.LOGGER.error("Couldn't save configuration file");
        }
    }
}