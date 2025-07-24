package networkbalance.commands;

import networkbalance.NetworkBalance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PayAcceptCommand implements CommandExecutor {

    private final NetworkBalance plugin;

    public PayAcceptCommand(NetworkBalance plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player targetPlayer)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("/payaccept <player>");
            return true;
        }

        String senderName = args[0];

        Map<String, Object> msg = new HashMap<>();
        msg.put("plugin", "networkbalance");
        msg.put("type", "PAY_CONFIRM");
        msg.put("from", senderName);
        msg.put("to", targetPlayer.getName());

        plugin.sendPluginMessage(targetPlayer, msg);
        targetPlayer.sendMessage("Accepted payment from " + senderName);
        return true;
    }
}
