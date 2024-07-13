package org.kif.reincarceration.modifier.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.modifier.listeners.ModifierListener;
import org.kif.reincarceration.modifier.types.*;
import org.kif.reincarceration.util.ConsoleUtil;

import java.sql.SQLException;

public class ModifierModule implements Module {
    private final Reincarceration plugin;
    private ModifierManager modifierManager;
    private ModifierRegistry modifierRegistry;

    public ModifierModule(Reincarceration plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.modifierRegistry = new ModifierRegistry();
        DataModule dataModule = plugin.getModuleManager().getModule(DataModule.class);
        PermissionManager permissionManager = new PermissionManager(plugin);
        this.modifierManager = new ModifierManager(this, dataModule.getDataManager(), permissionManager, modifierRegistry);
        registerDefaultModifiers();
        plugin.getServer().getPluginManager().registerEvents(new ModifierListener(this), plugin);

        // Schedule the reapplication of modifiers
        Bukkit.getScheduler().runTaskLater(plugin, this::reapplyModifiersToOnlinePlayers, 20L); // 1 second delay

        ConsoleUtil.sendSuccess("Modifier Module enabled");
    }

    @Override
    public void onDisable() {
        ConsoleUtil.sendSuccess("Modifier Module disabled");
    }

    private void registerDefaultModifiers() {
        ConfigurationSection modifiersConfig = plugin.getConfig().getConfigurationSection("modifiers");
        if (modifiersConfig != null) {
            if (modifiersConfig.getBoolean("ore_sickness.enabled", true)) {
                OreSicknessModifier oreSicknessModifier = new OreSicknessModifier(plugin);
                modifierRegistry.registerModifier(oreSicknessModifier);
                plugin.getServer().getPluginManager().registerEvents(oreSicknessModifier, plugin);
            }
            if (modifiersConfig.getBoolean("combustion.enabled", true)) {
                modifierRegistry.registerModifier(new CombustionModifier(plugin));
            }
        }
    }

    private void reapplyModifiersToOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (modifierManager.hasAnyActiveModifier(player)) {
                    modifierManager.reapplyModifier(player);
                    ConsoleUtil.sendInfo("Reapplied modifier for player: " + player.getName());
                } else {
                    ConsoleUtil.sendDebug("No active modifier found for player: " + player.getName());
                }
            } catch (SQLException e) {
                ConsoleUtil.sendError("Error reapplying modifier for player " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    public ModifierManager getModifierManager() {
        return modifierManager;
    }

    public ModifierRegistry getModifierRegistry() {
        return modifierRegistry;
    }

    public Reincarceration getPlugin() {
        return plugin;
    }
}