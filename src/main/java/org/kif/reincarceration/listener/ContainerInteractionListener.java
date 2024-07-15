package org.kif.reincarceration.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.util.ConsoleUtil;

public class ContainerInteractionListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public ContainerInteractionListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);
        Inventory inventory = event.getInventory();

        // Ignore player inventory, crafting tables, and certain other inventory types
        if (shouldIgnoreInventory(inventory)) {
            return;
        }

        if (isAssociated) {
            if (containsUnflaggedItem(inventory)) {
                event.setCancelled(true);
                MessageUtil.sendPrefixMessage(player, "&cWarning: This container has items not associated with the reincarceration system.");
                ConsoleUtil.sendDebug("Blocked inventory open for " + player.getName() + ": contains unflagged items");
            }
        } else {
            removeFlagsFromInventory(inventory);
            ConsoleUtil.sendDebug("Removed flags from inventory for non-associated player: " + player.getName());
        }
    }

    private boolean shouldIgnoreInventory(Inventory inventory) {
        InventoryType type = inventory.getType();
        return inventory.getHolder() == null ||
                type == InventoryType.CRAFTING ||
                type == InventoryType.CREATIVE ||
                type == InventoryType.PLAYER ||
                type == InventoryType.CHEST || // This could be a fishing result, so we'll ignore it
                type.name().contains("FISHING"); // For future-proofing, in case a FISHING type is added
    }

    private boolean containsUnflaggedItem(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir() && !ItemUtil.hasReincarcerationFlag(item)) {
                return true;
            }
        }
        return false;
    }

    private void removeFlagsFromInventory(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir() && ItemUtil.hasReincarcerationFlag(item)) {
                ItemUtil.removeReincarcerationFlag(item);
            }
        }
    }
}