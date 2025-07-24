package networkbalance.commands;

import com.google.gson.Gson;
import networkbalance.NetworkBalance;
import networkbalance.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PayCommand implements CommandExecutor {

    private final NetworkBalance plugin;
    private final DatabaseManager database;

    public PayCommand(NetworkBalance plugin) {
        this.plugin = plugin;
        this.database = plugin.getDatabase();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /pay <player> <amount>");
            return true;
        }

        String targetName = args[0];
        double amount;

        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount.");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("Amount must be positive.");
            return true;
        }

        Player targetPlayer = plugin.getServer().getPlayerExact(targetName);
        if (targetPlayer == null) {
            sender.sendMessage("Player " + targetName + " is not online.");
            return true;
        }

        // Check if both players are in the same world
        if (!senderPlayer.getWorld().equals(targetPlayer.getWorld())) {
            sender.sendMessage("You must be in the same world as " + targetName + " to send money.");
            return true;
        }

        UUID senderUUID = senderPlayer.getUniqueId();
        UUID targetUUID = targetPlayer.getUniqueId();

        double senderBalance = database.getBalance(senderUUID);
        if (senderBalance < amount) {
            sender.sendMessage("You don't have enough balance.");
            return true;
        }

        // Perform the transfer
        database.setBalance(senderUUID, senderBalance - amount);

        double targetBalance = database.getBalance(targetUUID);
        database.setBalance(targetUUID, targetBalance + amount);

        sender.sendMessage("You paid $" + amount + " to " + targetName + ".");
        targetPlayer.sendMessage("You received $" + amount + " from " + senderPlayer.getName() + ".");

        return true;
    }
}
