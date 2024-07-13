package org.kif.reincarceration.rank;

import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.economy.EconomyManager;
import org.kif.reincarceration.economy.EconomyModule;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ConsoleUtil;

public class RankModule implements Module {
    private final Reincarceration plugin;
    private RankManager rankManager;

    public RankModule(Reincarceration plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        ConfigManager configManager = plugin.getModuleManager().getConfigManager();
        DataModule dataModule = plugin.getModuleManager().getModule(DataModule.class);
        EconomyModule economyModule = plugin.getModuleManager().getModule(EconomyModule.class);

        if (configManager == null || dataModule == null || economyModule == null) {
            throw new IllegalStateException("Required modules are not initialized");
        }

        DataManager dataManager = dataModule.getDataManager();
        EconomyManager economyManager = economyModule.getEconomyManager();
        PermissionManager permissionManager = new PermissionManager(plugin);

        this.rankManager = new RankManager(plugin, configManager, dataManager, economyManager, permissionManager);
        ConsoleUtil.sendSuccess("Rank Module enabled");
    }

    @Override
    public void onDisable() {
        // Perform any cleanup if necessary
        ConsoleUtil.sendSuccess("Rank Module disabled");
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}