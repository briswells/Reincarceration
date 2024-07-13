package org.kif.reincarceration.modifier.core;

import org.bukkit.entity.Player;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.permission.PermissionManager;

import java.sql.SQLException;
import java.util.*;

public class ModifierManager {
    private final ModifierModule modifierModule;
    private final DataManager dataManager;
    private final PermissionManager permissionManager;
    private final ModifierRegistry modifierRegistry;

    public ModifierManager(ModifierModule modifierModule, DataManager dataManager, PermissionManager permissionManager, ModifierRegistry modifierRegistry) {
        this.modifierModule = modifierModule;
        this.dataManager = dataManager;
        this.permissionManager = permissionManager;
        this.modifierRegistry = modifierRegistry;
    }

    public void applyModifier(Player player, IModifier modifier) throws SQLException {
        modifier.apply(player);
        dataManager.setActiveModifier(player, modifier.getId());
        permissionManager.addPermission(player, "reincarceration.modifier." + modifier.getId());
    }

    public void reapplyModifier(Player player) throws SQLException {
        IModifier modifier = getActiveModifier(player);
        if (modifier != null) {
            modifier.apply(player);
        }
    }

    public void disableModifier(Player player) throws SQLException {
        IModifier activeModifier = getActiveModifier(player);
        if (activeModifier != null) {
            activeModifier.remove(player);
        }
    }

    public void removeModifier(Player player) throws SQLException {
        IModifier activeModifier = getActiveModifier(player);
        if (activeModifier != null) {
            activeModifier.remove(player);
            dataManager.removeActiveModifier(player);
            permissionManager.removePermission(player, "reincarceration.modifier." + activeModifier.getId());
        }
    }

    public IModifier getActiveModifier(Player player) throws SQLException {
        String modifierId = dataManager.getActiveModifier(player);
        if (modifierId != null) {
            return modifierRegistry.getModifier(modifierId);
        }
        return null;
    }

    public boolean hasActiveModifier(Player player, String modifierId) {
        return permissionManager.hasPermission(player, "reincarceration.modifier." + modifierId);
    }

    public boolean hasAnyActiveModifier(Player player) throws SQLException {
        return dataManager.getActiveModifier(player) != null;
    }

    public boolean canUseModifier(Player player, IModifier modifier) throws SQLException {
        return !dataManager.hasCompletedModifier(player, modifier.getId());
    }

    public void completeModifier(Player player, IModifier modifier) throws SQLException {
        dataManager.addCompletedModifier(player, modifier.getId());
        removeModifier(player);
    }

    public List<String> getCompletedModifiers(Player player) throws SQLException {
        return dataManager.getCompletedModifiers(player);
    }

    public List<IModifier> getAvailableModifiers(Player player) throws SQLException {
        List<String> completedModifiers = getCompletedModifiers(player);
        return modifierRegistry.getAvailableModifiers(completedModifiers);
    }

    public int getTotalModifierCount() {
        return modifierRegistry.getAllModifiers().size();
    }
}