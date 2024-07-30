package org.kif.reincarceration.api;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.entity.Player;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.cycle.CycleManager;
import org.kif.reincarceration.cycle.CycleModule;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.modifier.core.ModifierManager;
import org.kif.reincarceration.modifier.core.ModifierModule;

public class ReincarcerationAPI {
    public static boolean isPlayerInCycle(Reincarceration plugin, Player player) {
        CycleManager cycleManager = plugin.getModuleManager().getModule(CycleModule.class).getCycleManager();
        return cycleManager.isPlayerInCycle(player);
    }

    public static IModifier getPlayerModifier(Reincarceration plugin, Player player) throws SQLException {
        ModifierManager modifierManager = plugin.getModuleManager().getModule(ModifierModule.class)
                .getModifierManager();
        return modifierManager.getActiveModifier(player);
    }

    public static List<IModifier> getCompletedModifiers(Reincarceration plugin, Player player) throws SQLException {
        ModifierManager modifierManager = plugin.getModuleManager().getModule(ModifierModule.class)
                .getModifierManager();
        return modifierManager.getCompletedModifiers(player);
    }

    public static List<IModifier> getAvailableModifiers(Reincarceration plugin, Player player) throws SQLException {
        ModifierManager modifierManager = plugin.getModuleManager().getModule(ModifierModule.class)
                .getModifierManager();
        return modifierManager.getAvailableModifiers(player);
    }

    public static BigDecimal getStoredBalance(Reincarceration plugin, Player player) throws SQLException {
        DataManager dataManager = plugin.getModuleManager().getModule(DataModule.class).getDataManager();
        return dataManager.getStoredBalance(player);
    }
}
