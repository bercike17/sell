package hu.sellshop.command;

import hu.sellshop.SellShopPlugin;
import hu.sellshop.gui.SellGUI;
import hu.sellshop.util.Msg;
import hu.sellshop.util.SellLogic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SellCommand implements CommandExecutor, TabCompleter {

    private final SellShopPlugin plugin;

    public SellCommand(SellShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Msg.send(sender, "player-only");
            return true;
        }
        if (!player.hasPermission("sell.use")) {
            Msg.send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            if (plugin.getSellManager().getAllPrices().isEmpty()) {
                Msg.send(player, "no-prices");
                return true;
            }
            player.openInventory(SellGUI.build(plugin, player));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "hand" -> SellLogic.sellHand(plugin, player);
            case "all" -> SellLogic.sellAll(plugin, player);
            default -> Msg.send(player, "invalid-usage", "%usage%", "/" + label + " [hand|all]");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("hand", "all");
        }
        return new ArrayList<>();
    }
}
