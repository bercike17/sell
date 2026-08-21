package hu.sellshop;

import hu.sellshop.command.SellAdminCommand;
import hu.sellshop.command.SellCommand;
import hu.sellshop.data.SellManager;
import hu.sellshop.data.SellMultiplierManager;
import hu.sellshop.hook.VaultHook;
import hu.sellshop.listener.SellGUIListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class SellShopPlugin extends JavaPlugin {

    private static SellShopPlugin instance;

    private SellManager sellManager;
    private SellMultiplierManager sellMultiplierManager;
    private NamespacedKey materialKey;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        materialKey = new NamespacedKey(this, "sell_material");

        boolean vault = VaultHook.setup();
        getLogger().info("Vault: " + (vault ? "csatlakozva" : "nincs / nem talalhato"));

        sellManager = new SellManager(this);
        sellManager.load();

        sellMultiplierManager = new SellMultiplierManager(this);
        sellMultiplierManager.load();

        getServer().getPluginManager().registerEvents(new SellGUIListener(this), this);

        SellCommand sellCommand = new SellCommand(this);
        getCommand("sell").setExecutor(sellCommand);
        getCommand("sell").setTabCompleter(sellCommand);

        SellAdminCommand adminCommand = new SellAdminCommand(this);
        getCommand("sellshop").setExecutor(adminCommand);
        getCommand("sellshop").setTabCompleter(adminCommand);

        getLogger().info("SellShop elindult.");
    }

    @Override
    public void onDisable() {
        if (sellManager != null) {
            sellManager.save();
        }
    }

    public static SellShopPlugin getInstance() {
        return instance;
    }

    public SellManager getSellManager() {
        return sellManager;
    }

    public SellMultiplierManager getSellMultiplierManager() {
        return sellMultiplierManager;
    }

    public NamespacedKey getMaterialKey() {
        return materialKey;
    }
}
