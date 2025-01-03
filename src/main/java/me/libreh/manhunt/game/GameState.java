package me.libreh.manhunt.game;

public enum GameState {
    PREGAME("§a", "PRE-GAME"),
    PRELOADING("§a", "PRE-GAME"),
    WAITING("§e", "PLAYING"),
    STARTED("§e", "PLAYING"),
    POSTGAME("§6", "POST-GAME");

    private final String color;
    private final String motd;

    public final String getColor() {
        return this.color;
    }

    public final String getMotd() {
        return this.motd;
    }

    GameState(String color, String motd) {
        this.color = color;
        this.motd = motd;
    }
}
