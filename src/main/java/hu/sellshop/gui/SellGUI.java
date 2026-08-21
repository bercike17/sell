package hu.sellshop.gui;

import hu.sellshop.SellShopPlugin;
import hu.sellshop.hook.VaultHook;
import hu.sellshop.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class SellGUI {

    private SellGUI() {
    }

    public static Inventory build(SellShopPlugin plugin, Player viewer) {
        Map<Material, Double> prices = plugin.getSellManager().getAllPrices();
        int size = computeSize(prices.size());
        double multiplier = plugin.getSellMultiplierManager().getMultiplier(viewer);

        SellGUIHolder holder = new SellGUIHolder();
        Inventory inv = Bukkit.createInventory(holder, size, Msg.color(plugin.getConfig().getString("gui.title", "&8Eladas")));
        holder.setInventory(inv);

        int slot = 0;
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            if (slot >= size) break;

            Material material = entry.getKey();
            double price = entry.getValue() * multiplier;

            ItemStack icon = new ItemStack(material);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(Msg.color("&f" + niceName(material)));

            String priceLine = Msg.color(plugin.getConfig().getString("messages.lore-price", "&aAr: &f%price% / db")
                    .replace("%price%", VaultHook.isEnabled() ? VaultHook.format(price) : String.valueOf(price)));
            List<String> lore = multiplier > 1.0
                    ? List.of(priceLine, Msg.color(plugin.getConfig().getString("messages.lore-multiplier", "&7(&6%multiplier% szorzoval&7)")
                            .replace("%multiplier%", formatMultiplier(multiplier))), Msg.raw("lore-click"))
                    : List.of(priceLine, Msg.raw("lore-click"));
            meta.setLore(lore);

            meta.getPersistentDataContainer().set(plugin.getMaterialKey(), PersistentDataType.STRING, material.name());
            icon.setItemMeta(meta);

            inv.setItem(slot++, icon);
        }

        return inv;
    }

    private static String formatMultiplier(double multiplier) {
        String formatted = String.valueOf(multiplier);
        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }
        return "x" + formatted;
    }

    private static int computeSize(int itemCount) {
        int rows = Math.min(6, Math.max(1, (int) Math.ceil(itemCount / 9.0)));
        return rows * 9;
    }

    private static String niceName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
