package manhunt.game;

// Thanks to https://gitlab.com/horrific-tweaks/bingo

public enum ManhuntState {
    PREGAME("§a", "PRE-GAME"),
    PLAYING("§6", "IN-GAME"),
    POSTGAME("§e", "POST-GAME");

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
