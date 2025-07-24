package networkbalance.commands;

import networkbalance.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final DatabaseManager database;

    public BalanceCommand(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        double balance = database.getBalance(player.getUniqueId());
        player.sendMessage("Your balance is: " + balance);
        return true;
    }
}
