package org.kif.reincarceration.util;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporaryDataManager {
    private final Map<UUID, Inventory> virtualInventories = new HashMap<>();

    public void setVirtualInventory(UUID playerUUID, Inventory inventory) {
        virtualInventories.put(playerUUID, inventory);
    }

    public Inventory getVirtualInventory(UUID playerUUID) {
        return virtualInventories.get(playerUUID);
    }

    public void removeVirtualInventory(UUID playerUUID) {
        virtualInventories.remove(playerUUID);
    }

    public void clearAllData() {
        virtualInventories.clear();
    }
}