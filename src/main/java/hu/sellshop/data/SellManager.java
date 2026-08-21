package hu.sellshop.data;

import hu.sellshop.SellShopPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SellManager {

    private final SellShopPlugin plugin;
    private final Map<Material, Double> prices = new LinkedHashMap<>();
    private File file;

    public SellManager(SellShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        prices.clear();
        file = new File(plugin.getDataFolder(), "prices.yml");

        if (!file.exists()) {
            return; // meg nincs egy ar sem beallitva
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("prices");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                double price = section.getDouble(key);
                if (price > 0) {
                    prices.put(material, price);
                }
            } catch (IllegalArgumentException ignored) {
                // ervenytelen anyagnev a fajlban, kihagyjuk
            }
        }
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<Material, Double> entry : prices.entrySet()) {
            yaml.set("prices." + entry.getKey().name(), entry.getValue());
        }

        try {
            if (file == null) {
                file = new File(plugin.getDataFolder(), "prices.yml");
            }
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Nem sikerult elmenteni a prices.yml fajlt: " + e.getMessage());
        }
    }

    public void setPrice(Material material, double price) {
        prices.put(material, price);
        save();
    }

    public boolean removePrice(Material material) {
        boolean removed = prices.remove(material) != null;
        if (removed) save();
        return removed;
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, -1.0);
    }

    public boolean hasPrice(Material material) {
        return prices.containsKey(material);
    }

    public Map<Material, Double> getAllPrices() {
        return prices;
    }
}
