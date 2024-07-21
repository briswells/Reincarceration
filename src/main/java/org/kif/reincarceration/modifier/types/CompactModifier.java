package org.kif.reincarceration.modifier.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompactModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private int allowedInventorySlots;
    private int allowedHotbarSlots;
    private static final int HOTBAR_SIZE = 9;
    private static final int PLAYER_INVENTORY_SIZE = 36; // 27 main inventory + 9 hotbar
    private static final int INVENTORY_ROW_SIZE = 9;
    private ItemStack restrictedSlotItem;

    public CompactModifier(Reincarceration plugin) {
        super("compact", "Compact", "Limits the number of usable inventory slots");
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.compact");
        if (config != null) {
            this.allowedInventorySlots = config.getInt("allowed_inventory_slots", 9);
            this.allowedHotbarSlots = config.getInt("allowed_hotbar_slots", 9);
        } else {
            ConsoleUtil.sendError("Compact modifier configuration not found. Using default values.");
            this.allowedInventorySlots = 9;
            this.allowedHotbarSlots = 9;
        }
        ConsoleUtil.sendDebug("Compact Modifier Config: Allowed Inventory Slots = " + allowedInventorySlots + ", Allowed Hotbar Slots = " + allowedHotbarSlots);
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        this.restrictedSlotItem = createRestrictedSlotItem();
        enforceInventoryRestriction(player);
        fillRestrictedSlots(player);
        removeDisallowedDeadBushes(player);
        ConsoleUtil.sendDebug("Applied Compact Modifier to " + player.getName() + ". Allowed slots: " + allowedInventorySlots + " inventory slots, " + allowedHotbarSlots + " hotbar slots");
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        clearRestrictedSlots(player);
        ConsoleUtil.sendDebug("Removed Compact Modifier from " + player.getName());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isActive(player)) return;

        int slot = event.getRawSlot();
        int playerInvSlot = event.getView().convertSlot(slot);

        // Allow interactions with allowed hotbar slots
        if (playerInvSlot < allowedHotbarSlots) return;

        // Allow interactions with the allowed inventory slots
        if (playerInvSlot >= PLAYER_INVENTORY_SIZE - allowedInventorySlots) return;

        // Allow interactions with armor and offhand slots
        if (playerInvSlot >= PLAYER_INVENTORY_SIZE) return;

        // Prevent interaction with restricted slot item (dead bush)
        if (event.getCurrentItem() != null && event.getCurrentItem().isSimilar(restrictedSlotItem)) {
            event.setCancelled(true);
            return;
        }

        // Cancel interactions with restricted slots
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isActive(player)) return;

        Set<Integer> allowedSlotSet = IntStream.range(0, allowedHotbarSlots)
                .boxed()
                .collect(Collectors.toSet());
        allowedSlotSet.addAll(IntStream.range(PLAYER_INVENTORY_SIZE - allowedInventorySlots, PLAYER_INVENTORY_SIZE)
                .boxed()
                .collect(Collectors.toSet()));

        Set<Integer> affectedPlayerSlots = event.getRawSlots().stream()
                .map(event.getView()::convertSlot)
                .filter(slot -> slot < PLAYER_INVENTORY_SIZE)
                .collect(Collectors.toSet());

        if (!allowedSlotSet.containsAll(affectedPlayerSlots)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!isActive(player)) return;

        ItemStack item = event.getItem().getItemStack();
        if (item.getType() == Material.DEAD_BUSH) {
            event.setCancelled(true);
            event.getItem().remove();
        }

        if (getTotalItemsInAllowedSlots(player) >= (allowedInventorySlots + allowedHotbarSlots)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot >= allowedHotbarSlots) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (!isActive(player)) return;

        if (event.getInventory().getType() == InventoryType.CRAFTING || event.getInventory().getType() == InventoryType.PLAYER) {
            removeDisallowedDeadBushes(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!isActive(player)) return;

        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == Material.DEAD_BUSH) {
                item.setAmount(0);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;

        this.restrictedSlotItem = createRestrictedSlotItem();
        new BukkitRunnable() {
            @Override
            public void run() {
                fillRestrictedSlots(player);
            }
        }.runTaskLater(plugin, 1L); // Run on next tick to ensure inventory is available
    }

    private boolean hasSpaceInAllowedSlots(Player player) {
        return getTotalItemsInAllowedSlots(player) < (allowedInventorySlots + allowedHotbarSlots);
    }

    private int getTotalItemsInAllowedSlots(Player player) {
        int count = 0;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < allowedHotbarSlots; i++) {
            if (inventory.getItem(i) != null) count++;
        }
        for (int i = PLAYER_INVENTORY_SIZE - allowedInventorySlots; i < PLAYER_INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) != null) count++;
        }
        return count;
    }

    private void enforceInventoryRestriction(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive(player)) {
                    this.cancel();
                    return;
                }
                fillRestrictedSlots(player);
            }
        }.runTaskTimer(plugin, 20L * 60, 20L * 60); // Run every minute
    }

    private void fillRestrictedSlots(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = allowedHotbarSlots; i < PLAYER_INVENTORY_SIZE - allowedInventorySlots; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                inventory.setItem(i, restrictedSlotItem);
            } else if (!item.isSimilar(restrictedSlotItem)) {
                moveItemToAllowedSlot(player, i);
                inventory.setItem(i, restrictedSlotItem);
            }
        }
    }

    private void clearRestrictedSlots(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = allowedHotbarSlots; i < PLAYER_INVENTORY_SIZE - allowedInventorySlots; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.isSimilar(restrictedSlotItem)) {
                inventory.setItem(i, null);
            }
        }
    }

    private void removeDisallowedDeadBushes(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < allowedHotbarSlots; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.DEAD_BUSH && !item.isSimilar(restrictedSlotItem)) {
                inventory.setItem(i, null);
            }
        }
        for (int i = PLAYER_INVENTORY_SIZE - allowedInventorySlots; i < PLAYER_INVENTORY_SIZE; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.DEAD_BUSH && !item.isSimilar(restrictedSlotItem)) {
                inventory.setItem(i, null);
            }
        }
    }

    private void moveItemToAllowedSlot(Player player, int fromSlot) {
        ItemStack item = player.getInventory().getItem(fromSlot);
        if (item != null && !item.getType().isAir()) {
            player.getInventory().setItem(fromSlot, null);
            if (hasSpaceInAllowedSlots(player)) {
                HashMap<Integer, ItemStack> leftover = new HashMap<>();
                for (int i = 0; i < allowedHotbarSlots; i++) {
                    if (player.getInventory().getItem(i) == null) {
                        player.getInventory().setItem(i, item);
                        return;
                    }
                }
                for (int i = PLAYER_INVENTORY_SIZE - allowedInventorySlots; i < PLAYER_INVENTORY_SIZE; i++) {
                    if (player.getInventory().getItem(i) == null) {
                        player.getInventory().setItem(i, item);
                        return;
                    }
                }
                if (!leftover.isEmpty()) {
                    for (ItemStack dropItem : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), dropItem);
                    }
                }
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }

    private ItemStack createRestrictedSlotItem() {
        ItemStack deadBush = new ItemStack(Material.DEAD_BUSH);
        ItemUtil.addReincarcerationFlag(deadBush);
        return deadBush;
    }

    public boolean handleVaultAccess(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        MessageUtil.sendPrefixMessage(player, "&cVault Access Denied - You are not allowed to access vaults.");
        return true;
    }

    public void reloadConfig() {
        loadConfig();
    }
}
