package org.kif.reincarceration.modifier.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kif.reincarceration.modifier.core.ModifierModule;

import java.sql.SQLException;

public class ModifierListener implements Listener {
    private final ModifierModule modifierModule;

    public ModifierListener(ModifierModule modifierModule) {
        this.modifierModule = modifierModule;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            modifierModule.getModifierManager().reapplyModifier(event.getPlayer());
        } catch (SQLException e) {
            modifierModule.getPlugin().getLogger().severe("Error reapplying modifier on player join: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove active modifier (if any) when player leaves
        try {
            modifierModule.getModifierManager().disableModifier(event.getPlayer());
        } catch (SQLException e) {
            modifierModule.getPlugin().getLogger().severe("Error removing modifier on player quit: " + e.getMessage());
        }
    }
}