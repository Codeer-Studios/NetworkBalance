package networkbalance.messaging;

import com.google.gson.Gson;
import networkbalance.NetworkBalance;
import networkbalance.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PluginMessageHandler implements PluginMessageListener {

    private final DatabaseManager database;
    private final Gson gson;

    public PluginMessageHandler(JavaPlugin plugin, DatabaseManager database, Gson gson) {
        this.database = database;
        this.gson = gson;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
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

            case "PAY_REQUEST" -> {
                String from = (String) msg.get("from");
                String to = (String) msg.get("to");
                double amount = ((Number) msg.get("amount")).doubleValue();

                Player target = Bukkit.getPlayerExact(to);
                if (target == null) return;

                target.sendMessage("§e" + from + " wants to send you §a$" + amount + "§e. Type §b/payaccept " + from + "§e to accept.");
            }

            case "PAY_CONFIRM" -> {
                String from = (String) msg.get("from");
                String to = (String) msg.get("to");
                double amount = ((Number) msg.get("amount")).doubleValue();

                Player sender = Bukkit.getPlayerExact(from);
                Player receiver = Bukkit.getPlayerExact(to);

                if (sender == null || receiver == null) return;

                double senderBalance = database.getBalance(sender.getUniqueId());
                if (senderBalance < amount) {
                    sender.sendMessage("§cYou no longer have enough funds to complete this transaction.");
                    return;
                }

                database.setBalance(sender.getUniqueId(), senderBalance - amount);
                double receiverBalance = database.getBalance(receiver.getUniqueId());
                database.setBalance(receiver.getUniqueId(), receiverBalance + amount);

                sender.sendMessage("§aYou paid §e$" + amount + "§a to " + to + ".");
                receiver.sendMessage("§aYou received §e$" + amount + "§a from " + from + ".");
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
