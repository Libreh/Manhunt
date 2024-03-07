package manhunt.config;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static manhunt.config.ConfigFileHandler.config;

// Thanks to https://github.com/malte0811/FerriteCore

public class ManhuntConfig {
    public static Setting WORLD_SEED;
    public static Setting PRELOADING;
    public static Setting SET_MOTD;
    public static Setting RUNNER_VOTING;
    public static Setting VOTES_PER_PLAYER;
    public static Setting TOP_VOTED_RUNS;
    public static Setting VOTE_PLACES;
    public static Setting RESET_SECONDS;
    public static Setting AUTO_RESET;
    public static Setting AUTO_START;
    public static Setting SET_ROLES;
    public static Setting HUNTER_FREEZE_SECONDS;
    public static Setting TIME_LIMIT_MINUTES;
    public static Setting TRACKER_UPDATE_METHOD;
    public static Setting SHOW_TEAM_COLOR;
    public static Setting WORLD_DIFFICULTY;
    public static Setting WORLD_BORDER_BLOCKS;
    public static Setting ALLOW_BED_EXPLOSIONS;
    public static Setting ALLOW_LAVA_PVP_IN_THE_NETHER;
    public static Setting CHANGEABLE_PREFERENCES;
    public static Setting SHOW_WINNER_TITLE;
    public static Setting MANHUNT_SOUNDS_VOLUME;
    public static Setting SHOW_SETTINGS_AT_START;
    public static Setting SHOW_DURATION_ON_WIN;
    public static Setting SHOW_SEED_ON_WIN;

    public static void setDefaults() {
        ConfigBuilder builder = new ConfigBuilder();
        WORLD_SEED = builder.createSetting("worldSeed", "Used to hash the world seed", 0L);
        PRELOADING = builder.createSetting("preloading", "Preload the world before start", false);
        SET_MOTD = builder.createSetting("setMotd", "Set motd based on game state", true);
        RUNNER_VOTING = builder.createSetting("runnerVoting", "Voting system to select runners", false);
        VOTES_PER_PLAYER = builder.createSetting("votesPerPlayer", "Votes per player", 1);
        TOP_VOTED_RUNS = builder.createSetting("topVotedRuns", "Runs per top voted runner", 3);
        VOTE_PLACES = builder.createSetting("votePlaces", "Top voted runners amount", 2);
        RESET_SECONDS = builder.createSetting("resetSeconds", "Seconds until a game auto resets", 5);
        AUTO_RESET = builder.createSetting("autoReset", "Reset game a select seconds after a win", true);
        AUTO_START = builder.createSetting("autoStart", "Start game after voting", true);
        SET_ROLES = builder.createSetting("setRoles", "How the roles should be choosen\n# Available options are \"Free Select\", \"All Hunters\", and \"All Runners\"", "Free Select");
        HUNTER_FREEZE_SECONDS = builder.createSetting("hunterFreezeSeconds", "Freezes the hunters at start", 0);
        TIME_LIMIT_MINUTES = builder.createSetting("timeLimitMinutes", "Hunters win at the limit", 0);
        TRACKER_UPDATE_METHOD = builder.createSetting("trackerUpdateMethod", "Set update method for the tracker\n# Available options are \"Manual Click\" (right click manually), \"Basic Update\" (update every second), and \"Smart Rate\" (update based on distance to runner)", "Smart Rate");
        SHOW_TEAM_COLOR = builder.createSetting("showTeamColor", "The color of nametags depend on team", true);
        WORLD_DIFFICULTY = builder.createSetting("worldDifficulty", "The world difficulty\n# Available options are \"Easy\", \"Normal\", and \"Hard\"", "Easy");
        WORLD_BORDER_BLOCKS = builder.createSetting("worldBorderBlocks", "Set border size in blocks\n# It defaults to the maximum if it's higher than it (59999968)", 59999968);
        ALLOW_BED_EXPLOSIONS = builder.createSetting("allowBedExplosions", "If false then disables placement if enemy team is closer than 9 blocks", false);
        ALLOW_LAVA_PVP_IN_THE_NETHER = builder.createSetting("allowLavaPvpInTheNether", "If false then disables placement if enemy team is closer than 9 blocks", false);
        CHANGEABLE_PREFERENCES = builder.createSetting("changeablePreferences", "If false then preferences default", true);
        SHOW_WINNER_TITLE = builder.createSetting("showWinnerTitle", "Default preference value", true);
        MANHUNT_SOUNDS_VOLUME = builder.createSetting("manhuntSoundsVolume", "Default preference value in percent", 100);
        SHOW_SETTINGS_AT_START = builder.createSetting("showSettingsAtStart", "Default preference value in percent", true);
        SHOW_DURATION_ON_WIN = builder.createSetting("showDurationOnWin", "Default preference value in percent", true);
        SHOW_SEED_ON_WIN = builder.createSetting("showSeedOnWin", "Default preference value", true);
        builder.finish();
    }

    public static class ConfigBuilder {
        public static final List<Setting> settings = new ArrayList<>();

        public Setting createSetting(String name, String comment, Object defaultValue) {
            Setting result = new Setting(name, comment, defaultValue);
            settings.add(result);
            return result;
        }

        private void finish() {
            try {
                ConfigFileHandler.finish(settings);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Setting {
        private final String name;
        private final String comment;
        private final Object defaultValue;
        private String value;

        public Setting(String name, String comment, Object defaultValue) {
            this.name = name;
            this.comment = comment;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public String get() {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(config.toFile()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            String content;
            while (true) {
                try {
                    if (!((content = br.readLine()) != null)) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (content.lastIndexOf(name) >= 0) {
                    value = content.substring(content.lastIndexOf("=") + 2);
                }
            }

            return value;
        }

        public void set(Object value) {
            StringBuilder oldContent = new StringBuilder();

            BufferedReader reader = null;

            FileWriter writer = null;

            try {
                reader = new BufferedReader(new FileReader(config.toFile()));

                String line = reader.readLine();

                while (line != null)
                {
                    oldContent.append(line).append(System.lineSeparator());

                    line = reader.readLine();
                }

                String newContent = oldContent.toString().replaceAll(name + " = " + get(), name + " = " + value);

                writer = new FileWriter(config.toFile());

                writer.write(newContent);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}