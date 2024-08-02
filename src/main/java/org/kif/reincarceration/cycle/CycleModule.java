package org.kif.reincarceration.cycle;

import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.core.CoreModule;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.economy.EconomyModule;
import org.kif.reincarceration.modifier.core.ModifierModule;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.rank.RankModule;
import org.kif.reincarceration.rewards.RewardModule;
import org.kif.reincarceration.util.ConsoleUtil;

public class CycleModule implements Module {
    private final Reincarceration plugin;
    private CycleManager cycleManager;

    public CycleModule(Reincarceration plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        CoreModule coreModule = plugin.getModuleManager().getModule(CoreModule.class);
        DataModule dataModule = plugin.getModuleManager().getModule(DataModule.class);
        EconomyModule economyModule = plugin.getModuleManager().getModule(EconomyModule.class);
        RankModule rankModule = plugin.getModuleManager().getModule(RankModule.class);
        ModifierModule modifierModule = plugin.getModuleManager().getModule(ModifierModule.class);
        RewardModule rewardModule = plugin.getModuleManager().getModule(RewardModule.class);

        if (coreModule == null || dataModule == null || economyModule == null || rankModule == null
                || modifierModule == null) {
            throw new IllegalStateException("Required modules are not initialized");
        }

        ConfigManager configManager = coreModule.getConfigManager();
        PermissionManager permissionManager = new PermissionManager(plugin);

        this.cycleManager = new CycleManager(plugin, this, configManager, dataModule.getDataManager(),
                economyModule.getEconomyManager(), rankModule.getRankManager(),
                permissionManager, modifierModule.getModifierManager(), rewardModule.getRewardManager());

        ConsoleUtil.sendSuccess("Cycle Module enabled");
    }

    @Override
    public void onDisable() {
        ConsoleUtil.sendSuccess("Cycle Module disabled");
    }

    public CycleManager getCycleManager() {
        return cycleManager;
    }

    public Reincarceration getPlugin() {
        return plugin;
    }
}