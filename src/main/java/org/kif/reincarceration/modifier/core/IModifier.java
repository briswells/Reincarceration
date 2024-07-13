package org.kif.reincarceration.modifier.core;

import org.bukkit.entity.Player;

public interface IModifier {
    String getId();
    String getName();
    String getDescription();
    void apply(Player player);
    void remove(Player player);
    boolean isActive(Player player);
}