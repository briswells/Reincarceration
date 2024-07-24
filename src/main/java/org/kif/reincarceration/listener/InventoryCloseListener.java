package org.kif.reincarceration.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class InventoryCloseListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public InventoryCloseListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            ConsoleUtil.sendDebug("InventoryCloseListener: Event player is not a Player instance");
            return;
        }

        Player player = (Player) event.getPlayer();
        ConsoleUtil.sendDebug("InventoryCloseListener: Processing inventory close for player " + player.getName());

        // Check if the player is associated with the reincarceration system
        if (!permissionManager.isAssociatedWithBaseGroup(player)) {
            ConsoleUtil.sendDebug("InventoryCloseListener: Player " + player.getName() + " is not associated with reincarceration system");
            return;
        }

        PlayerInventory inventory = player.getInventory();
        List<ItemStack> unflaggedItems = new ArrayList<>();

        // Check all items in the player's inventory
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().isAir()) {
                if (!ItemUtil.hasReincarcerationFlag(item)) {
                    unflaggedItems.add(item.clone());  // Clone the item to avoid issues
                    ConsoleUtil.sendDebug("InventoryCloseListener: Unflagged item found - " + item.getType().name());
                }
            }
        }

        // Remove and drop unflagged items
        if (!unflaggedItems.isEmpty()) {
            Location dropLocation = player.getLocation();
            for (ItemStack item : unflaggedItems) {
                inventory.remove(item);
                player.getWorld().dropItemNaturally(dropLocation, item);
                ConsoleUtil.sendDebug("InventoryCloseListener: Dropped item - " + item.getType().name());
            }

            // Update the inventory
            player.updateInventory();

            MessageUtil.sendPrefixMessage(player, "&cUnflagged items have been removed from your inventory.");
            ConsoleUtil.sendDebug("InventoryCloseListener: Removed " + unflaggedItems.size() + " unflagged items from " + player.getName() + "'s inventory");
        } else {
            ConsoleUtil.sendDebug("InventoryCloseListener: No unflagged items found in " + player.getName() + "'s inventory");
        }
    }
}