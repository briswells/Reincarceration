package org.kif.reincarceration.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.util.ConsoleUtil;

public class EconomyModule implements Module {
    private final Reincarceration plugin;
    private Economy economy;
    private EconomyManager economyManager;

    public EconomyModule(Reincarceration plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            plugin.getLogger().severe("Disabled due to no Vault dependency found!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        this.economyManager = new EconomyManager(this);
        ConsoleUtil.sendSuccess("Economy Module enabled");
    }

    @Override
    public void onDisable() {
        ConsoleUtil.sendSuccess("Economy Module disabled");
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        if (economy == null) {
            setupEconomy();
        }
        return economy;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public Reincarceration getPlugin() {
        return plugin;
    }
}