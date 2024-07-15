package org.kif.reincarceration.modifier.types;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;

public class CompactModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private int allowedSlots;

    public CompactModifier(Reincarceration plugin) {
        super("compact", "Compact", "Limits the number of usable inventory slots");
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.compact");
        if (config != null) {
            this.allowedSlots = config.getInt("allowed_slots", 9);
        } else {
            ConsoleUtil.sendError("Compact modifier configuration not found. Using default value.");
            this.allowedSlots = 9;
        }
        ConsoleUtil.sendDebug("Compact Modifier Config: Allowed Slots = " + allowedSlots);
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        enforceInventoryRestriction(player);
        ConsoleUtil.sendDebug("Applied Compact Modifier to " + player.getName());
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        ConsoleUtil.sendDebug("Removed Compact Modifier from " + player.getName());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isActive(player)) return;

        int slot = event.getRawSlot();
        if (slot >= allowedSlots && slot < player.getInventory().getSize()) {
            event.setCancelled(true);
            ConsoleUtil.sendDebug("Blocked inventory click in restricted slot " + slot + " for " + player.getName());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isActive(player)) return;

        for (int slot : event.getRawSlots()) {
            if (slot >= allowedSlots && slot < player.getInventory().getSize()) {
                event.setCancelled(true);
                ConsoleUtil.sendDebug("Blocked inventory drag to restricted slot " + slot + " for " + player.getName());
                return;
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!isActive(player)) return;

        if (!hasSpaceInAllowedSlots(player)) {
            event.setCancelled(true);
            ConsoleUtil.sendDebug("Blocked item pickup due to inventory restriction for " + player.getName());
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot >= allowedSlots) {
            event.setCancelled(true);
            ConsoleUtil.sendDebug("Blocked item drop from restricted slot " + slot + " for " + player.getName());
        }
    }

    private boolean hasSpaceInAllowedSlots(Player player) {
        for (int i = 0; i < allowedSlots; i++) {
            if (player.getInventory().getItem(i) == null) {
                return true;
            }
        }
        return false;
    }

    private void enforceInventoryRestriction(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive(player)) {
                    this.cancel();
                    return;
                }
                for (int i = allowedSlots; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null) {
                        player.getInventory().setItem(i, null);
                        for (int j = 0; j < allowedSlots; j++) {
                            if (player.getInventory().getItem(j) == null) {
                                player.getInventory().setItem(j, item);
                                break;
                            }
                        }
                        if (player.getInventory().getItem(i) != null) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    public void reloadConfig() {
        loadConfig();
    }
}