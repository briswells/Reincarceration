package org.kif.reincarceration.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.core.CoreModule;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.economy.EconomyManager;
import org.kif.reincarceration.economy.EconomyModule;

import java.sql.SQLException;

public class PlayerListener implements Listener {
    private final Reincarceration plugin;
    private final DataManager dataManager;
    private final ConfigManager configManager;
    private final EconomyManager economyManager;

    public PlayerListener(Reincarceration plugin) {
        this.plugin = plugin;
        CoreModule coreModule = plugin.getModuleManager().getModule(CoreModule.class);
        DataModule dataModule = plugin.getModuleManager().getModule(DataModule.class);
        EconomyModule economyModule = plugin.getModuleManager().getModule(EconomyModule.class);

        if (coreModule == null || dataModule == null || economyModule == null) {
            throw new IllegalStateException("Required modules are not initialized");
        }

        this.configManager = coreModule.getConfigManager();
        this.dataManager = dataModule.getDataManager();
        this.economyManager = economyModule.getEconomyManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            dataManager.createPlayerData(player);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error initializing data inside database for " + player.getName() + ": " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // If needed, perform any cleanup or data saving operations here
        // For now, we'll leave this empty as our data is saved in real-time
    }
}