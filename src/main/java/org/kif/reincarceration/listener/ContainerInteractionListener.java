package org.kif.reincarceration.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ContainerViewerTracker;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.util.ConsoleUtil;

import java.util.List;
import java.util.Set;

public class ContainerInteractionListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;
    private final List<String> blacklistedInventories;

    public ContainerInteractionListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
        this.blacklistedInventories = plugin.getConfig().getStringList("blacklisted_containers");
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        if (player.isOp()) return;

        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);

        Inventory inventory = event.getInventory();

        // Ignore player inventory, crafting tables, and certain other inventory types
        if (shouldIgnoreInventory(inventory)) {
            return;
        }

        if (isAssociated) {
            if (isBlacklistedInventory(inventory)) {
                event.setCancelled(true);
                MessageUtil.sendPrefixMessage(player, "&cThis container has been blacklisted from you.");
                ConsoleUtil.sendDebug("Blocked blacklisted inventory open for " + player.getName() + ": " + inventory.getType());
                return;
            }

            if (containsUnflaggedItem(inventory)) {
                event.setCancelled(true);
                MessageUtil.sendPrefixMessage(player, "&cThis container has prohibited contents.");
                ConsoleUtil.sendDebug("Blocked inventory open for " + player.getName() + ": contains unflagged items");
                return;
            }
        } else {
            // Non-associated player is opening the inventory
            Set<Player> currentViewers = ContainerViewerTracker.getViewers(inventory);
            for (Player viewer : currentViewers) {
                if (permissionManager.isAssociatedWithBaseGroup(viewer)) {
                    // Close inventory for associated players
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            viewer.closeInventory();
                            MessageUtil.sendPrefixMessage(viewer, "&cThis container has been accessed by a prohibited player.");
                        }
                    }.runTask(plugin);
                }
            }
            removeFlagsFromInventory(inventory);
            ConsoleUtil.sendDebug("Removed flags from inventory for non-associated player: " + player.getName());
        }

        // Add player to container viewers
        ContainerViewerTracker.addViewer(inventory, player);
        logContainerViewers(inventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        // Remove player from container viewers
        ContainerViewerTracker.removeViewer(inventory, player);
        logContainerViewers(inventory);
    }

    private boolean isBlacklistedInventory(Inventory inventory) {
        return blacklistedInventories.contains(inventory.getType().name());
    }

    private boolean shouldIgnoreInventory(Inventory inventory) {
        InventoryType type = inventory.getType();
        return inventory.getHolder() == null ||
                type == InventoryType.CRAFTING ||
                type == InventoryType.ANVIL ||
                type == InventoryType.ENCHANTING ||
                type == InventoryType.SMITHING ||
                type == InventoryType.CREATIVE ||
                type == InventoryType.PLAYER;
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

    private void logContainerViewers(Inventory inventory) {
        Set<Player> viewers = ContainerViewerTracker.getViewers(inventory);
        if (!viewers.isEmpty()) {
            ConsoleUtil.sendDebug("Current viewers of inventory " + inventory.getType() + ":");
            for (Player viewer : viewers) {
                ConsoleUtil.sendDebug("- " + viewer.getName() + " (Associated: " + permissionManager.isAssociatedWithBaseGroup(viewer) + ")");
            }
        }
    }
}