package manhunt.config.model;


@SuppressWarnings("ALL")
public class ConfigModel {

    public Settings settings = new Settings();

    public class Settings {
        public int setRoles = 1;
        public int hunterFreeze = 10;
        public int timeLimit = 180;
        public boolean compassUpdate = true;
        public boolean teamColor = true;
        public int worldDifficulty = 1;
        public int borderSize = 59999968;
        public boolean gameTitles = true;
    }
}
