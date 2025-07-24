package networkbalance.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.UUID;


public class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin) {
        String host = plugin.getConfig().getString("database.host");
        int port = plugin.getConfig().getInt("database.port");
        String database = plugin.getConfig().getString("database.database");
        String username = plugin.getConfig().getString("database.username");
        String password = plugin.getConfig().getString("database.password");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);

        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Existing player balances table
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_balances (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "balance DOUBLE NOT NULL" +
                            ");"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getBalance(UUID uuid) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM player_balances WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                setBalance(uuid, 0);
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setBalance(UUID uuid, double balance) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_balances (uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = ?")) {
            stmt.setString(1, uuid.toString());
            stmt.setDouble(2, balance);
            stmt.setDouble(3, balance);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        dataSource.close();
    }

}
