package networkbalance;

import com.google.gson.Gson;
import networkbalance.commands.*;
import networkbalance.database.DatabaseManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class NetworkBalance extends JavaPlugin {

    private static NetworkBalance instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        databaseManager = new DatabaseManager(this);

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

    public static NetworkBalance getInstance() {
        return instance;
    }

    public DatabaseManager getDatabase() {
        return databaseManager;
    }

}
