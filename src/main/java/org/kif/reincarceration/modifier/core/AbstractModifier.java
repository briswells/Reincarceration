package org.kif.reincarceration.modifier.core;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractModifier implements IModifier {
    private final String id;
    private final String name;
    private final String description;
    private final Set<UUID> activePlayers;

    public AbstractModifier(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.activePlayers = new HashSet<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void apply(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void remove(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }
}