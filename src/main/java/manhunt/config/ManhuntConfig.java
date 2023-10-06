package manhunt.config;

import com.google.gson.*;
import manhunt.Manhunt;

import java.io.*;

public class ManhuntConfig {

    private ManhuntConfig() {
    }
    private static final File confFile = new File("./config/manhunt.json");
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String databaseName = "";
    public static String databaseAddress = "";
    public static String databasePort = "";
    public static String databaseUser = "";
    public static String databasePassword = "";
    public static String musicDirectory = "";
    public static boolean pregenerationEnabled = true;
    public static boolean pingingEnabled = true;
    public static int hunterFreeze = 10;
    public static int timeLimit = 180;
    public static String compassUpdate = "Automatic";
    public static boolean showRunnerDimension = true;
    public static boolean latePlayersJoinHunters = true;
    public static boolean showTeamColor = true;
    public static boolean disableBedExplosions = true;
    public static String worldDifficulty = "easy";
    public static int borderSize = 59999968;
    public static boolean revealWinner = true;
    public static boolean automaticRoleSelection = true;

    public static void load() {
        if(!confFile.exists() || confFile.length() == 0) save();
        try {
            JsonObject jo = gson.fromJson(new FileReader(confFile), JsonObject.class);
            JsonElement je;

            if ((je = jo.get("databaseName")) != null) databaseName = je.getAsString();
            if ((je = jo.get("databaseAddress")) != null) databaseAddress = je.getAsString();
            if ((je = jo.get("databasePort")) != null) databasePort = je.getAsString();
            if ((je = jo.get("databaseUser")) != null) databaseUser = je.getAsString();
            if ((je = jo.get("databasePassword")) != null) databasePassword = je.getAsString();
            if ((je = jo.get("musicDirectory")) != null) musicDirectory = je.getAsString();
            if ((je = jo.get("pregenerationEnabled")) != null) pregenerationEnabled = je.getAsBoolean();
            if ((je = jo.get("pingingEnabled")) != null) pingingEnabled = je.getAsBoolean();
            if ((je = jo.get("hunterFreeze")) != null) hunterFreeze = je.getAsInt();
            if ((je = jo.get("timeLimit")) != null) timeLimit = je.getAsInt();
            if ((je = jo.get("compassUpdate")) != null) compassUpdate = je.getAsString();
            if ((je = jo.get("showRunnerDimension")) != null) showRunnerDimension = je.getAsBoolean();
            if ((je = jo.get("latePlayersJoinHunters")) != null) latePlayersJoinHunters = je.getAsBoolean();
            if ((je = jo.get("showTeamColor")) != null) showTeamColor = je.getAsBoolean();
            if ((je = jo.get("disableBedExplosions")) != null) disableBedExplosions = je.getAsBoolean();
            if ((je = jo.get("worldDifficulty")) != null) worldDifficulty = je.getAsString();
            if ((je = jo.get("borderSize")) != null) borderSize = je.getAsInt();
            if ((je = jo.get("revealWinner")) != null) revealWinner = je.getAsBoolean();
            if ((je = jo.get("automaticRoleSelection")) != null) automaticRoleSelection = je.getAsBoolean();
        } catch (FileNotFoundException ex) {
            Manhunt.LOGGER.trace("Couldn't load configuration file", ex);
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
            jo.add("databaseName", new JsonPrimitive(databaseName));
            jo.add("databaseAddress", new JsonPrimitive(databaseAddress));
            jo.add("databasePort", new JsonPrimitive(databasePort));
            jo.add("databaseUser", new JsonPrimitive(databaseUser));
            jo.add("databasePassword", new JsonPrimitive(databasePassword));
            jo.add("musicDirectory", new JsonPrimitive(musicDirectory));
            jo.add("pregenerationEnabled", new JsonPrimitive(pregenerationEnabled));
            jo.add("pingingEnabled", new JsonPrimitive(pingingEnabled));
            jo.add("hunterFreeze", new JsonPrimitive(hunterFreeze));
            jo.add("timeLimit", new JsonPrimitive(timeLimit));
            jo.add("compassUpdate", new JsonPrimitive(compassUpdate));
            jo.add("showRunnerDimension", new JsonPrimitive(showRunnerDimension));
            jo.add("latePlayersJoinHunters", new JsonPrimitive(latePlayersJoinHunters));
            jo.add("showTeamColor", new JsonPrimitive(showTeamColor));
            jo.add("disableBedExplosions", new JsonPrimitive(disableBedExplosions));
            jo.add("worldDifficulty", new JsonPrimitive(worldDifficulty));
            jo.add("borderSize", new JsonPrimitive(borderSize));
            jo.add("revealWinner", new JsonPrimitive(revealWinner));
            jo.add("automaticRoleSelection", new JsonPrimitive(automaticRoleSelection));

            PrintWriter printWriter = new PrintWriter(new FileWriter(confFile));
            printWriter.print(gson.toJson(jo));
            printWriter.close();
        } catch (IOException ex) {
            Manhunt.LOGGER.trace("Couldn't save configuration file", ex);
        }
    }
}
