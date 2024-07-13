package org.kif.reincarceration.core;

import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.util.ConsoleUtil;

public class CoreModule implements Module {
    private final Reincarceration plugin;
    private final ConfigManager configManager;

    public CoreModule(Reincarceration plugin) {
        this.plugin = plugin;
        // Initialize configuration immediately
        plugin.saveDefaultConfig();
        this.configManager = new ConfigManager(plugin, plugin.getConfig());
    }

    @Override
    public void onEnable() {
        // Initialize other core components here
        // Initialize console utility
        ConsoleUtil.initialize(this.plugin);

        ConsoleUtil.sendSuccess("Core Module enabled");
    }

    @Override
    public void onDisable() {
        // Perform any necessary cleanup
        ConsoleUtil.sendSuccess("Core Module disabled");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}