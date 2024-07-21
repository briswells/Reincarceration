package org.kif.reincarceration.modifier.core;

import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    @Override
    public boolean handleBlockBreak(BlockBreakEvent event) {
        // Default implementation returns false, indicating that the BlockBreakListener should handle it
        return false;
    }

    @Override
    public boolean handleFishing(PlayerFishEvent event) {
        // Default implementation returns false, indicating that the FishingListener should handle it
        return false;
    }

    @Override
    public boolean handlePreTransaction(PreTransactionEvent event) {
        // Default implementation returns false, indicating that the PreTransactionListener should handle it
        return false;
    }

    @Override
    public boolean handlePostTransaction(PostTransactionEvent event) {
        // Default implementation returns false, indicating that the PostTransactionListener should handle it
        return false;
    }

    @Override
    public boolean handleVaultAccess(PlayerInteractEvent event) {
        // Default implementation returns false, indicating that the VaultAccessListener should handle it
        return false;
    }
}