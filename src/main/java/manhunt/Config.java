package manhunt;

import com.google.gson.*;
import org.apache.logging.log4j.Level;

import java.io.*;

import static manhunt.Manhunt.log;

public class Config {

    private Config() {
    }
    private static final File confFile = new File("./config/manhunt.json");
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String databaseType = "SQLITE";
    public static String databaseName = "";
    public static String sqliteDirectory = "/path/to/folder";
    public static String mysqlAddress = "";
    public static int mysqlPort = 3306;
    public static String mysqlUser = "username";
    public static String mysqlPassword = "password";
    public static String presetSelection = "Casual";
    public static String setRoles = "Free Select";
    public static int hunterFreeze = 10;
    public static int timeLimit = 180;
    public static String compassUpdate = "Automatic";
    public static boolean showDimensionInfo = true;
    public static String latePlayers = "Join Hunters";
    public static boolean showTeamColor = true;
    public static boolean disableBedExplosions = true;
    public static String worldDifficulty = "easy";
    public static int borderSize = 59999968;
    public static boolean showGameTitles = true;

    public static void load() {
        if(!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            if ((je = jo.get("databaseType")) != null) databaseType = je.getAsString();
            if ((je = jo.get("databaseName")) != null) databaseName = je.getAsString();
            if ((je = jo.get("sqliteDirectory")) != null) sqliteDirectory = je.getAsString();
            if ((je = jo.get("databaseAddress")) != null) mysqlAddress = je.getAsString();
            if ((je = jo.get("databasePort")) != null) mysqlPort = je.getAsInt();
            if ((je = jo.get("databaseUser")) != null) mysqlUser = je.getAsString();
            if ((je = jo.get("databasePassword")) != null) mysqlPassword = je.getAsString();
            if ((je = jo.get("setRoles")) != null) setRoles = je.getAsString();
            if ((je = jo.get("hunterFreeze")) != null) hunterFreeze = je.getAsInt();
            if ((je = jo.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = jo.get("compassUpdate")) != null) compassUpdate = je.getAsString();
            if ((je = jo.get("dimensionInfo")) != null) showDimensionInfo = je.getAsBoolean();
            if ((je = jo.get("latePlayers")) != null) latePlayers = je.getAsString();
            if ((je = jo.get("showTeamColor")) != null) showTeamColor = je.getAsBoolean();
            if ((je = jo.get("disableBedExplosions")) != null) disableBedExplosions = je.getAsBoolean();
            if ((je = jo.get("worldDifficulty")) != null) worldDifficulty = je.getAsString();
            if ((je = jo.get("borderSize")) != null) borderSize = je.getAsInt();
            if ((je = jo.get("showGameTitles")) != null) showGameTitles = je.getAsBoolean();
        } catch (FileNotFoundException ex) {
            log(Level.ERROR, "Couldn't load configuration file");
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
            jo.add("databaseType", new JsonPrimitive(databaseType));
            jo.add("databaseName", new JsonPrimitive(databaseName));
            jo.add("sqliteDirectory", new JsonPrimitive(sqliteDirectory));
            jo.add("databaseAddress", new JsonPrimitive(mysqlAddress));
            jo.add("databasePort", new JsonPrimitive(mysqlPort));
            jo.add("databaseUser", new JsonPrimitive(mysqlUser));
            jo.add("databasePassword", new JsonPrimitive(mysqlPassword));
            jo.add("setRoles", new JsonPrimitive(setRoles));
            jo.add("hunterFreeze", new JsonPrimitive(hunterFreeze));
            jo.add("timeLimit", new JsonPrimitive(timeLimit));
            jo.add("compassUpdate", new JsonPrimitive(compassUpdate));
            jo.add("dimensionInfo", new JsonPrimitive(showDimensionInfo));
            jo.add("latePlayers", new JsonPrimitive(latePlayers));
            jo.add("showTeamColor", new JsonPrimitive(showTeamColor));
            jo.add("disableBedExplosions", new JsonPrimitive(disableBedExplosions));
            jo.add("worldDifficulty", new JsonPrimitive(worldDifficulty));
            jo.add("borderSize", new JsonPrimitive(borderSize));
            jo.add("gameTitles", new JsonPrimitive(showGameTitles));

            PrintWriter printWriter = new PrintWriter(new FileWriter(confFile));
            printWriter.print(gson.toJson(jo));
            printWriter.close();
        } catch (IOException ex) {
            log(Level.ERROR, "Couldn't save configuration file");
        }
    }
}