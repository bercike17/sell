package hu.sellshop.data;

import hu.sellshop.SellShopPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rang-alapu eladasi szorzo: a config.yml sell-multipliers.permissions alatt
 * felsorolt PERMISSION NODE-okhoz (nem LuckPerms-specifikus - sima Bukkit
 * permission, amit a LuckPerms VAGY barmilyen mas jog-kezelo plugin ki tud
 * osztani) tartozo szorzo-ertekek kozul a jatekos a LEGNAGYOBBAT kapja meg
 * (ha tobb ilyen jogot is birtokol egyszerre - pl. egy magasabb rang
 * "orokolte" egy alacsonyabbtol). Ha egyiket sem birtokolja, 1.0 (nincs
 * szorzas).
 *
 * Hasznalat: /lp user <nev> permission set sellshop.multiplier.vip true
 */
public class SellMultiplierManager {

    private final SellShopPlugin plugin;
    // permission node -> szorzo, a config sorrendjeben (LinkedHashMap, hogy
    // determinisztikus legyen, bar a sorrend a maximum-kereses miatt amugy sem szamit)
    private final Map<String, Double> multipliers = new LinkedHashMap<>();

    public SellMultiplierManager(SellShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        multipliers.clear();
        if (!plugin.getConfig().getBoolean("sell-multipliers.enabled", true)) {
            return;
        }
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("sell-multipliers.permissions");
        if (section == null) {
            return;
        }
        // FONTOS: a Bukkit config-rendszer a kulcsokban levo PONTOT is
        // utvonal-elvalasztokent ertelmezi - tehat a YAML-ban levo
        // "sellshop.multiplier.vip: 1.1" NEM egyetlen kulcskent toltodik
        // be, hanem beagyazott al-szekciokka bomlik (sellshop -> multiplier
        // -> vip -> 1.1). Ezert getKeys(false) helyett getKeys(true)-t
        // hasznalunk (ez visszaadja a TELJES, pontozott utvonalakat is),
        // es csak a "level" ertekeket (nem magukat a koztes al-szekciokat)
        // vesszuk fel - igy a "sellshop.multiplier.vip" tenyleges,
        // eredeti permission node-kent all vissza.
        for (String key : section.getKeys(true)) {
            if (section.isConfigurationSection(key)) {
                continue; // ez csak egy koztes csomopont (pl. "sellshop" vagy "sellshop.multiplier"), nem tenyleges ertek
            }
            double value = section.getDouble(key, 1.0);
            if (value > 0) {
                multipliers.put(key, value);
            }
        }
    }

    /**
     * A jatekos AKTUALIS eladasi szorzoja - a birtokolt, konfiguralt
     * jogosultsagok kozul a LEGNAGYOBB ertek, vagy 1.0, ha egyiket sem
     * birtokolja (tehat nincs szorzas).
     */
    public double getMultiplier(Player player) {
        double best = 1.0;
        for (Map.Entry<String, Double> entry : multipliers.entrySet()) {
            if (entry.getValue() > best && player.hasPermission(entry.getKey())) {
                best = entry.getValue();
            }
        }
        return best;
    }
}
