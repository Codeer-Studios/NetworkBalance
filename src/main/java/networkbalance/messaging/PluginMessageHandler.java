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
        if (!channel.equals("networkbalance:balance")) return;

        String json = new String(message, StandardCharsets.UTF_8);
        Map<String, Object> msg = gson.fromJson(json, Map.class);

        String type = (String) msg.get("type");
        String playerName = (String) msg.get("player");

        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) return;

        if ("GET_BALANCE".equals(type)) {
            double bal = database.getBalance(target.getUniqueId());
            sendResponse(target, playerName, bal);
        } else if ("SET_BALANCE".equals(type)) {
            double amount = ((Number) msg.get("amount")).doubleValue();
            database.setBalance(target.getUniqueId(), amount);
            sendResponse(target, playerName, amount);
        }
    }

    private void sendResponse(Player sender, String playerName, double balance) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "BALANCE_RESPONSE");
        response.put("player", playerName);
        response.put("balance", balance);

        String json = gson.toJson(response);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        sender.sendPluginMessage(NetworkBalance.getInstance(), "networkbalance:balance", data);
    }
}
