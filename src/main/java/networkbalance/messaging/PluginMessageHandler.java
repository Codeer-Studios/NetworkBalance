package networkbalance.messaging;

import com.google.gson.Gson;
import networkbalance.NetworkBalance;
import networkbalance.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PluginMessageHandler implements PluginMessageListener {

    private final DatabaseManager database;
    private final Gson gson;
    private final NetworkBalance plugin;

    public PluginMessageHandler(NetworkBalance plugin, DatabaseManager database, Gson gson) {
        this.database = database;
        this.gson = gson;
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        System.out.println("[PluginMessageHandler] Received plugin message on channel: " + channel);
        String json2 = new String(message, StandardCharsets.UTF_8);
        System.out.println("[PluginMessageHandler] Message content: " + json2);
        if (!channel.equals("network:core")) return;

        String json = new String(message, StandardCharsets.UTF_8);
        Map<String, Object> msg = gson.fromJson(json, Map.class);

        // Ensure the message is intended for this plugin
        String pluginName = (String) msg.get("plugin");
        if (!"networkbalance".equals(pluginName)) return;

        String type = (String) msg.get("type");

        switch (type) {
            case "GET_BALANCE" -> {
                String playerName = (String) msg.get("player");
                Player target = Bukkit.getPlayerExact(playerName);
                if (target == null) return;
                double bal = database.getBalance(target.getUniqueId());
                sendResponse(target, playerName, bal);
            }

            case "SET_BALANCE" -> {
                String playerName = (String) msg.get("player");
                double amount = ((Number) msg.get("amount")).doubleValue();
                Player target = Bukkit.getPlayerExact(playerName);
                if (target == null) return;
                database.setBalance(target.getUniqueId(), amount);
                sendResponse(target, playerName, amount);
            }
        }
    }

    private void sendResponse(Player sender, String playerName, double balance) {
        Map<String, Object> response = new HashMap<>();
        response.put("plugin", "networkbalance"); // Important for NetworkCore routing
        response.put("type", "BALANCE_RESPONSE");
        response.put("player", playerName);
        response.put("balance", balance);

        String json = gson.toJson(response);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        sender.sendPluginMessage(NetworkBalance.getInstance(), "network:core", data);
    }
}
