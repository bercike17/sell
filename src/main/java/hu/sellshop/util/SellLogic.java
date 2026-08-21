package hu.sellshop.util;

import hu.sellshop.SellShopPlugin;
import hu.sellshop.hook.VaultHook;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class SellLogic {

    private SellLogic() {
    }

    /**
     * Eladja a jatekos teljes inventoryjabol (fo inventory + hotbar) az ADOTT
     * anyagtipusu targyakat. Nem nyul a pancelhoz/mellekkezhez, mivel ercek
     * es blokkok ott ugysem szoktak lenni.
     */
    public static void sellMaterial(SellShopPlugin plugin, Player player, Material material) {
        if (!VaultHook.isEnabled()) {
            Msg.send(player, "vault-missing");
            return;
        }

        double basePrice = plugin.getSellManager().getPrice(material);
        if (basePrice <= 0) {
            Msg.send(player, "no-price-set", "%material%", niceName(material));
            return;
        }
        double multiplier = plugin.getSellMultiplierManager().getMultiplier(player);
        double price = basePrice * multiplier;

        PlayerInventory inv = player.getInventory();
        int totalAmount = 0;

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() != material) continue;
            totalAmount += item.getAmount();
            inv.setItem(slot, null);
        }

        if (totalAmount <= 0) {
            Msg.send(player, "nothing-to-sell");
            return;
        }

        double total = totalAmount * price;
        VaultHook.deposit(player, total);

        if (multiplier > 1.0) {
            Msg.send(player, "sold-with-multiplier",
                    "%amount%", String.valueOf(totalAmount),
                    "%material%", niceName(material),
                    "%price%", VaultHook.format(total),
                    "%multiplier%", formatMultiplier(multiplier));
        } else {
            Msg.send(player, "sold",
                    "%amount%", String.valueOf(totalAmount),
                    "%material%", niceName(material),
                    "%price%", VaultHook.format(total));
        }
    }

    /**
     * Eladja a kezben tartott teljes stacket (ha van ra beallitott ar).
     */
    public static void sellHand(SellShopPlugin plugin, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) {
            Msg.send(player, "hold-item-required");
            return;
        }
        sellMaterial(plugin, player, hand.getType());
    }

    /**
     * Eladja a teljes inventorybol MINDEN olyan targyat, amire van beallitott ar.
     */
    public static void sellAll(SellShopPlugin plugin, Player player) {
        if (!VaultHook.isEnabled()) {
            Msg.send(player, "vault-missing");
            return;
        }

        double multiplier = plugin.getSellMultiplierManager().getMultiplier(player);
        PlayerInventory inv = player.getInventory();
        Map<Material, Integer> sold = new HashMap<>();
        double total = 0;

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            double basePrice = plugin.getSellManager().getPrice(item.getType());
            if (basePrice <= 0) continue;

            sold.merge(item.getType(), item.getAmount(), Integer::sum);
            total += item.getAmount() * basePrice * multiplier;
            inv.setItem(slot, null);
        }

        if (sold.isEmpty()) {
            Msg.send(player, "nothing-to-sell");
            return;
        }

        VaultHook.deposit(player, total);

        if (multiplier > 1.0) {
            Msg.send(player, "sold-all-with-multiplier",
                    "%price%", VaultHook.format(total),
                    "%items%", String.valueOf(sold.size()),
                    "%multiplier%", formatMultiplier(multiplier));
        } else {
            Msg.send(player, "sold-all",
                    "%price%", VaultHook.format(total),
                    "%items%", String.valueOf(sold.size()));
        }
    }

    private static String formatMultiplier(double multiplier) {
        String formatted = String.valueOf(multiplier);
        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }
        return "x" + formatted;
    }

    private static String niceName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
