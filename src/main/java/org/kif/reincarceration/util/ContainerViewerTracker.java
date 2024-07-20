package org.kif.reincarceration.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContainerViewerTracker {
    private static final Map<Inventory, Set<Player>> containerViewers = new HashMap<>();

    public static void addViewer(Inventory inventory, Player player) {
        containerViewers.computeIfAbsent(inventory, k -> new HashSet<>()).add(player);
    }

    public static void removeViewer(Inventory inventory, Player player) {
        Set<Player> viewers = containerViewers.get(inventory);
        if (viewers != null) {
            viewers.remove(player);
            if (viewers.isEmpty()) {
                containerViewers.remove(inventory);
            }
        }
    }

    public static Set<Player> getViewers(Inventory inventory) {
        return new HashSet<>(containerViewers.getOrDefault(inventory, new HashSet<>()));
    }

    public static boolean hasViewers(Inventory inventory) {
        Set<Player> viewers = containerViewers.get(inventory);
        return viewers != null && !viewers.isEmpty();
    }
}