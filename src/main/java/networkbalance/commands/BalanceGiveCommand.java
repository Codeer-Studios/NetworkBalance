package networkbalance.commands;

import networkbalance.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BalanceGiveCommand implements CommandExecutor {

    private final DatabaseManager database;

    public BalanceGiveCommand(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /balance give <player> <amount>");
            return true;
        }

        String playerName = args[0];
        double amount;

        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        UUID uuid = target.getUniqueId();
        double current = database.getBalance(uuid);
        database.setBalance(uuid, current + amount);

        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " to " + playerName + ". New balance: " + (current + amount));
        return true;
    }
}
