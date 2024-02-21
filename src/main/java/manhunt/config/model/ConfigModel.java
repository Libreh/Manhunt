package manhunt.config.model;

// Thanks to https://github.com/sakurawald/fuji-fabric

@SuppressWarnings("ALL")
public class ConfigModel {

    public Settings settings = new Settings();

    public class Settings {
        public long worldSeed = 0;
        public int setRoles = 1;
        public int hunterFreeze = 0;
        public int timeLimit = 0;
        public boolean compassUpdate = true;
        public boolean teamColor = true;
        public int worldDifficulty = 1;
        public int borderSize = 59999968;
        public boolean winnerTitle = true;
    }
}
