package manhunt;

public enum GameState {
    PREGAME("§a", "PRE-GAME"),
    PLAYING("§6", "IN-GAME"),
    POSTGAME("§e", "POST-GAME");

    private final String color;
    private final String motd;

    GameState(String color, String motd) {
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
