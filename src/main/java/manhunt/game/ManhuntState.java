package manhunt.game;

public enum ManhuntState {
    PREPARING("§2", "PREPARING"),
    PREGAME("§a", "PRE-GAME"),
    PLAYING("§e", "IN-GAME"),
    POSTGAME("§6", "POST-GAME");

    private final String color;
    private final String motd;

    ManhuntState(String color, String motd) {
        this.color = color;
        this.motd = motd;
    }

    public String getColor() {
        return color;
    }

    public String getMotd() {
        return motd;
    }
}
