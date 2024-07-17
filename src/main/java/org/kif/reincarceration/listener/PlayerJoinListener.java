package org.kif.reincarceration.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;

import java.sql.SQLException;

public class PlayerJoinListener implements Listener {
    private final Reincarceration plugin;
    private final DataManager dataManager;

    public PlayerJoinListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getModuleManager().getModule(DataModule.class).getDataManager();
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
}
