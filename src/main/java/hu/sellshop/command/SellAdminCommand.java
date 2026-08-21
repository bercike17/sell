package hu.sellshop.command;

import hu.sellshop.SellShopPlugin;
import hu.sellshop.hook.VaultHook;
import hu.sellshop.util.Msg;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SellAdminCommand implements CommandExecutor, TabCompleter {

    private final SellShopPlugin plugin;

    public SellAdminCommand(SellShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sell.admin")) {
            Msg.send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            usage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setprice" -> handleSetPrice(sender, args);
            case "removeprice" -> handleRemovePrice(sender, args);
            case "list" -> handleList(sender);
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getSellManager().load();
                plugin.getSellMultiplierManager().load();
                Msg.send(sender, "reload-success");
            }
            default -> usage(sender, label);
        }

        return true;
    }

    /**
     * Ket forma tamogatott:
     *   /sellshop setprice <ar>            - a kezben tartott targy anyagara allitja be
     *   /sellshop setprice <material> <ar> - kezben tartas nelkul, kifejezett anyagnevvel
     */
    private void handleSetPrice(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (!(sender instanceof Player player)) {
                Msg.send(sender, "player-only");
                return;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                Msg.send(sender, "hold-item-required");
                return;
            }
            Double price = parsePrice(args[1]);
            if (price == null) {
                Msg.send(sender, "invalid-price");
                return;
            }
            plugin.getSellManager().setPrice(hand.getType(), price);
            Msg.send(sender, "price-set", "%material%", niceName(hand.getType()), "%price%", formatPrice(price));
            return;
        }

        if (args.length >= 3) {
            Material material = parseMaterial(args[1]);
            if (material == null) {
                Msg.send(sender, "invalid-material", "%material%", args[1]);
                return;
            }
            Double price = parsePrice(args[2]);
            if (price == null) {
                Msg.send(sender, "invalid-price");
                return;
            }
            plugin.getSellManager().setPrice(material, price);
            Msg.send(sender, "price-set", "%material%", niceName(material), "%price%", formatPrice(price));
            return;
        }

        Msg.send(sender, "invalid-usage", "%usage%", "/sellshop setprice <material> <ar>  (vagy csak <ar>, ha tartod a targyat)");
    }

    private void handleRemovePrice(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Material material = parseMaterial(args[1]);
            if (material == null) {
                Msg.send(sender, "invalid-material", "%material%", args[1]);
                return;
            }
            if (!plugin.getSellManager().removePrice(material)) {
                Msg.send(sender, "price-not-set", "%material%", niceName(material));
                return;
            }
            Msg.send(sender, "price-removed", "%material%", niceName(material));
            return;
        }

        if (sender instanceof Player player) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.AIR) {
                Msg.send(sender, "hold-item-required");
                return;
            }
            if (!plugin.getSellManager().removePrice(hand.getType())) {
                Msg.send(sender, "price-not-set", "%material%", niceName(hand.getType()));
                return;
            }
            Msg.send(sender, "price-removed", "%material%", niceName(hand.getType()));
            return;
        }

        Msg.send(sender, "invalid-usage", "%usage%", "/sellshop removeprice <material>");
    }

    private void handleList(CommandSender sender) {
        Map<Material, Double> prices = plugin.getSellManager().getAllPrices();
        if (prices.isEmpty()) {
            Msg.send(sender, "no-prices");
            return;
        }
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            sender.sendMessage(Msg.color("&7- &f" + entry.getKey().name() + " &7- " + formatPrice(entry.getValue()) + " / db"));
        }
    }

    private String formatPrice(double price) {
        return VaultHook.isEnabled() ? VaultHook.format(price) : String.valueOf(price);
    }

    private String niceName(Material material) {
        String name = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    private Double parsePrice(String s) {
        try {
            double value = Double.parseDouble(s);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Material parseMaterial(String s) {
        try {
            return Material.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void usage(CommandSender sender, String label) {
        sender.sendMessage(Msg.color("&7Hasznalat: &f/" + label + " setprice|removeprice|list|reload ..."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("setprice", "removeprice", "list", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("removeprice")) {
            return plugin.getSellManager().getAllPrices().keySet().stream()
                    .map(Material::name)
                    .toList();
        }
        return new ArrayList<>();
    }
}
