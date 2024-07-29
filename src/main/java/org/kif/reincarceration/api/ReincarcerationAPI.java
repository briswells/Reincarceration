package org.kif.reincarceration.api;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.entity.Player;
import org.kif.reincarceration.modifier.core.IModifier;

public interface ReincarcerationAPI {
    boolean isPlayerInCycle(Player player);

    IModifier getPlayerModifier(Player player) throws SQLException;

    List<IModifier> getCompletedModifiers(Player player) throws SQLException;

    List<IModifier> getAvailableModifiers(Player player) throws SQLException;

    BigDecimal getStoredBalance(Player player) throws SQLException;
}
