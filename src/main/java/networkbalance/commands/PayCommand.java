package networkbalance.commands;

import com.google.gson.Gson;
import networkbalance.NetworkBalance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PayCommand implements CommandExecutor {

    private final NetworkBalance plugin;

    public PayCommand(NetworkBalance plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("/pay <player> <amount>");
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

        double senderBalance = plugin.getDatabase().getBalance(senderPlayer.getUniqueId());
        if (amount <= 0 || senderBalance < amount) {
            sender.sendMessage("You don't have enough balance.");
            return true;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("plugin", "networkbalance");
        message.put("type", "PAY_REQUEST");
        message.put("from", senderPlayer.getName());
        message.put("to", targetName);
        message.put("amount", amount);

        plugin.getLogger().info("[PayCommand] Sending PAY_REQUEST to Velocity: " + new Gson().toJson(message));

        plugin.sendPluginMessage(senderPlayer, message);

        sender.sendMessage("Payment request sent to " + targetName);
        return true;
    }
}
