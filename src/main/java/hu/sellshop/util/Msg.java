package hu.sellshop.util;

import hu.sellshop.SellShopPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Msg {

    private Msg() {
    }

    public static String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String raw(String path) {
        String s = SellShopPlugin.getInstance().getConfig().getString("messages." + path, path);
        return color(s);
    }

    public static void send(CommandSender sender, String path, String... replacements) {
        String msg = raw(path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(msg);
    }
}
