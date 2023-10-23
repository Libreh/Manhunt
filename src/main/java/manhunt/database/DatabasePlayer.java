package manhunt.database;


/**
 * DAO for a whitelisted user
 * @author Richard Nader, Jr. <rmnader@svsu.edu>
 */
public class DatabasePlayer {

    private String uuid;
    private String name;
    private boolean muteLobbyMusic;
    private boolean doNotDisturb;
    private String pingSound;
    private boolean gameLeader;

    public DatabasePlayer() {
    }

    public DatabasePlayer(String uuid, String name, boolean muteLobbyMusic, boolean doNotDisturb, String pingSound, boolean gameLeader) {
        this.uuid = uuid;
        this.name = name;
        this.muteLobbyMusic = muteLobbyMusic;
        this.doNotDisturb = doNotDisturb;
        this.pingSound = pingSound;
        this.gameLeader = gameLeader;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DatabasePlayer{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", muteLobbyMusic=" + muteLobbyMusic +
                ", doNotDisturb=" + doNotDisturb +
                '}';
    }

    public boolean isMuteLobbyMusic() {
        return muteLobbyMusic;
    }

    public void setMuteLobbyMusic(boolean muteLobbyMusic) {
        this.muteLobbyMusic = muteLobbyMusic;
    }

    public boolean isDoNotDisturb() {
        return doNotDisturb;
    }

    public void setDoNotDisturb(boolean doNotDisturb) {
        this.doNotDisturb = doNotDisturb;
    }

    public boolean isGameLeader() {
        return gameLeader;
    }

    public void setGameLeader(boolean gameLeader) {
        this.gameLeader = gameLeader;
    }

    public String getPingSound() {
        return pingSound;
    }

    public void setPingSound(String pingSound) {
        this.pingSound = pingSound;
    }
}