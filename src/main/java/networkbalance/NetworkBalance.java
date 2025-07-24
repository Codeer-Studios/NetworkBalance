package networkbalance;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NetworkBalance extends JavaPlugin implements PluginMessageListener {

    private HikariDataSource dataSource;
    private Gson gson;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        gson = new Gson();

        setupDatabase();

        getServer().getMessenger().registerIncomingPluginChannel(this, "balance", this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "balance");

        getCommand("balance").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            double balance = getBalance(player.getUniqueId());
            player.sendMessage("Your balance is: " + balance);
            return true;
        });

        getLogger().info("NetworkBalance enabled!");
    }

    private void setupDatabase() {
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String database = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);

        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS player_balances (" +
                             "uuid VARCHAR(36) PRIMARY KEY," +
                             "balance DOUBLE NOT NULL" +
                             ");"
             )) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("Failed to create player_balances table.");
            e.printStackTrace();
        }
    }

    public double getBalance(UUID playerUUID) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT balance FROM player_balances WHERE uuid = ?")) {
            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                setBalance(playerUUID, 0);
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setBalance(UUID playerUUID, double balance) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_balances (uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = ?")) {
            String uuidStr = playerUUID.toString();
            stmt.setString(1, uuidStr);
            stmt.setDouble(2, balance);
            stmt.setDouble(3, balance);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("balance")) return;

        String json = new String(message, StandardCharsets.UTF_8);
        getLogger().info("Received message: " + json);

        Map<String, Object> msg = gson.fromJson(json, Map.class);

        String type = (String) msg.get("type");
        String playerName = (String) msg.get("player");

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            getLogger().warning("Player " + playerName + " not found.");
            return;
        }

        if ("GET_BALANCE".equals(type)) {
            double bal = getBalance(target.getUniqueId());
            sendResponse(playerName, bal);
        } else if ("SET_BALANCE".equals(type)) {
            double amount = ((Number) msg.get("amount")).doubleValue();
            setBalance(target.getUniqueId(), amount);
            sendResponse(playerName, amount);
        }
    }

    private void sendResponse(String playerName, double balance) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "BALANCE_RESPONSE");
        response.put("player", playerName);
        response.put("balance", balance);

        String json = gson.toJson(response);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        // No direct player to send to here; sending to proxy instead
        // Bukkit doesn't have a direct method to send to proxy so it depends on your setup.
        // If needed, you can send this message via some other plugin messaging channel or system.

        getServer().sendPluginMessage(this, "balance", data);
        getLogger().info("Sent balance response for " + playerName);
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            dataSource.close();
        }
        getLogger().info("NetworkBalance disabled.");
    }
}
