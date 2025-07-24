package networkbalance;

import com.google.gson.Gson;
import networkbalance.commands.*;
import networkbalance.database.DatabaseManager;
import networkbalance.messaging.PluginMessageHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class NetworkBalance extends JavaPlugin {

    private static NetworkBalance instance;
    private DatabaseManager databaseManager;
    private PluginMessageHandler messageHandler;
    private Gson gson;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        gson = new Gson();
        databaseManager = new DatabaseManager(this);
        messageHandler = new PluginMessageHandler(this, databaseManager, gson);

        getServer().getMessenger().registerIncomingPluginChannel(this, "network:core", messageHandler);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "network:core");

        getLogger().info("Registering plugin channels on network:core");


        getCommand("balance").setExecutor(new BalanceCommand(databaseManager));
        getCommand("balancegive").setExecutor(new BalanceGiveCommand(databaseManager));
        getCommand("balanceremove").setExecutor(new BalanceRemoveCommand(databaseManager));
        getCommand("pay").setExecutor(new PayCommand(this));

        getLogger().info("NetworkBalance enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        getLogger().info("NetworkBalance disabled.");
    }

    public void sendPluginMessage(Player player, Map<String, Object> data) {
        String json = gson.toJson(data); // reuse existing Gson

        getLogger().info("[sendPluginMessage] Sending plugin message to player " + player.getName() + ": " + json);

        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        player.sendPluginMessage(this, "network:core", payload);
    }

    public static NetworkBalance getInstance() {
        return instance;
    }

    public DatabaseManager getDatabase() {
        return databaseManager;
    }

}
