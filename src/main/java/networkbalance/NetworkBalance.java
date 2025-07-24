package networkbalance;

import com.google.gson.Gson;
import networkbalance.commands.BalanceCommand;
import networkbalance.commands.BalanceGiveCommand;
import networkbalance.commands.BalanceRemoveCommand;
import networkbalance.database.DatabaseManager;
import networkbalance.messaging.PluginMessageHandler;
import org.bukkit.plugin.java.JavaPlugin;

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

        getServer().getMessenger().registerIncomingPluginChannel(this, "networkbalance:balance", messageHandler);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "networkbalance:balance");

        getCommand("balance").setExecutor(new BalanceCommand(databaseManager));
        getCommand("balancegive").setExecutor(new BalanceGiveCommand(databaseManager));
        getCommand("balanceremove").setExecutor(new BalanceRemoveCommand(databaseManager));

        getLogger().info("NetworkBalance enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        getLogger().info("NetworkBalance disabled.");
    }

    public static NetworkBalance getInstance() {
        return instance;
    }
}
