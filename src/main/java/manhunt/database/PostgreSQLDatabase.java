package manhunt.database;

import manhunt.Manhunt;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.*;
import java.util.ArrayList;

import static manhunt.Manhunt.LOGGER;
import static manhunt.config.ManhuntConfig.*;

/**
 * Service for PostgreSQL databases
 *
 * @author Foxite <the@dirkkok.nl>
 */
public class PostgreSQLDatabase {
    private final String url;
    private final String username;
    private final String password;

    public PostgreSQLDatabase() {
        this.url = "jdbc:postgresql://" + databaseAddress + ":" + 5432 + "/" + databaseName;
        this.username = databaseUser;
        this.password = databasePassword;
    }

    private Connection getConnection() throws SQLException {
        Connection connection;

        try {
            Class.forName("org.postgresql.Driver"); // This executes the static constructor of the class, which registers it to JDBC (or something)
            connection = DriverManager.getConnection(this.url, this.username, this.password);
        } catch (Exception e) {
            throw new SQLException("Error connecting to PostgreSQL database. See the inner exception message for more information", e);
        }
        return connection;
    }

    public boolean initializeDatabase() {
        try (Connection connection = getConnection()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet countResult = stmt.executeQuery("SELECT COUNT(*) FROM public.playerdata LIMIT 1")) {
                if (countResult.next() && countResult.getInt(1) > 0) {
                    Manhunt.LOGGER.debug("The whitelist table contains " + stmt.getResultSet().getInt(1) + " items");
                } else {
                    LOGGER.info("The whitelist table is present but empty");
                }

            } catch (SQLException e) {
                LOGGER.error("Caught exception while trying to count the whitelist table", e);

                if (e.getMessage().contains("relation \"public.playerdata\" does not exist")) {
                    LOGGER.info("Error appears to be caused by a missing table; creating it", e);
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("CREATE TABLE public.playerdata (\n" +
                                "    uuid uuid,\n" +
                                "    playername character varying,\n" +
                                "    mutelobbymusic boolean,\n" +
                                "    donotdisturb boolean,\n" +
                                "    pingsound character varying,\n" +
                                "    gameleader boolean\n" +
                                ");");
                    }

                    LOGGER.info("Playerdata table has been created");
                }
            }

            return true;
        } catch (SQLException e) {
            LOGGER.error("Unexpected exception while setting up database", e);
            return false;
        }
    }

    public ArrayList<DatabasePlayer> getPlayersDataFromDatabase() {
        ArrayList<DatabasePlayer> ret = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT uuid, playername, mutelobbymusic, donotdisturb, pingsound, gameleader FROM public.playerdata")) {

                while (result.next()) {
            ret.add(new DatabasePlayer(result.getString("uuid"), result.getString("playername"), result.getBoolean("mutelobbymusic"), result.getBoolean("donotdisturb"), result.getString("pingsound"), result.getBoolean("gameleader")));
        }
        } catch (SQLException e) {
            LOGGER.error("Unexpected exception while reading database playerdata", e);
        }
        return ret;
    }

    public boolean addPlayerToDatabase(ServerPlayerEntity player) {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            if (stmt.executeUpdate("UPDATE public.playerdata SET playername = '' WHERE uuid = '" + player.getUuid() + "'") == 0) {
                stmt.executeUpdate(String.format("INSERT INTO public.playerdata(uuid, playername, mutelobbymusic, donotdisturb, pingsound, gameleader) VALUES ('%s', '%s', '%s', '%s', '%s', '%s')", player.getUuid(), player.getName().getString(), false, false, "minecraft:block.bell.use", false));
            } else {
                stmt.executeUpdate("UPDATE public.playerdata SET playername = '" + player.getName().getString() + "' WHERE uuid = '" + player.getUuid() + "'");
            }
            return true;
        } catch (SQLException e) {
            LOGGER.error("Unexpected exception while adding player to database playerdata", e);
            return false;
        }
    }

    public boolean insertPlayerDataToDatabase(ServerPlayerEntity player, String row, String column) {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(String.format("UPDATE public.playerdata SET " + row + " = '" + column + "' WHERE uuid = '" + player.getUuid() + "'"));
            return true;
        } catch (SQLException e) {
            LOGGER.error("Unexpected exception while inserting data to database", e);
            return false;
        }
    }
}