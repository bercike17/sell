package hu.sellshop.listener;

import hu.sellshop.SellShopPlugin;
import hu.sellshop.gui.SellGUIHolder;
import hu.sellshop.util.SellLogic;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SellGUIListener implements Listener {

    private final SellShopPlugin plugin;

    public SellGUIListener(SellShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof SellGUIHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;

        String materialName = clicked.getItemMeta().getPersistentDataContainer()
                .get(plugin.getMaterialKey(), PersistentDataType.STRING);
        if (materialName == null) return;

        try {
            Material material = Material.valueOf(materialName);
            SellLogic.sellMaterial(plugin, player, material);
        } catch (IllegalArgumentException ignored) {
            // ervenytelen anyagnev a targyon, nem tortenik semmi
        }
    }
}
