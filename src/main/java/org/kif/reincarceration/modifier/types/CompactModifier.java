package org.kif.reincarceration.modifier.types;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.MessageUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompactModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private int allowedSlots;
    private static final int HOTBAR_SIZE = 9;
    private static final int PLAYER_INVENTORY_SIZE = 36; // 27 main inventory + 9 hotbar

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
        ConsoleUtil.sendDebug("Applied Compact Modifier to " + player.getName() + ". Allowed slots: " + allowedSlots);
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
        int playerInvSlot = event.getView().convertSlot(slot);

        ConsoleUtil.sendDebug("Inventory Click - Player: " + player.getName() + ", Raw Slot: " + slot + ", Player Inv Slot: " + playerInvSlot);

        // Allow interactions with hotbar
        if (playerInvSlot < HOTBAR_SIZE) return;

        // Allow interactions with armor and offhand slots
        if (playerInvSlot >= PLAYER_INVENTORY_SIZE) return;

        // Check if the slot is within the allowed range
        if (playerInvSlot < HOTBAR_SIZE + allowedSlots) return;

        // Handle shift-clicks
        if (event.isShiftClick()) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && !clickedItem.getType().isAir()) {
                if (!hasSpaceInAllowedSlots(player)) {
                    event.setCancelled(true);
                    ConsoleUtil.sendDebug("Blocked shift-click to full allowed slots for " + player.getName());
                }
            }
            return;
        }

        // Cancel interactions with restricted slots
        event.setCancelled(true);
        ConsoleUtil.sendDebug("Blocked inventory interaction in restricted slot " + playerInvSlot + " for " + player.getName());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isActive(player)) return;

        Set<Integer> allowedSlotSet = IntStream.range(0, HOTBAR_SIZE + allowedSlots).boxed().collect(Collectors.toSet());

        Set<Integer> affectedPlayerSlots = event.getRawSlots().stream()
                .map(event.getView()::convertSlot)
                .filter(slot -> slot < PLAYER_INVENTORY_SIZE)
                .collect(Collectors.toSet());

        ConsoleUtil.sendDebug("Inventory Drag - Player: " + player.getName() + ", Affected Player Slots: " + affectedPlayerSlots);

        if (!allowedSlotSet.containsAll(affectedPlayerSlots)) {
            event.setCancelled(true);
            ConsoleUtil.sendDebug("Blocked inventory drag to restricted slots for " + player.getName());
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!isActive(player)) return;

        ConsoleUtil.sendDebug("Item Pickup - Player: " + player.getName() + ", Item: " + event.getItem().getItemStack().getType());

        if (getTotalItemsInAllowedSlots(player) >= (allowedSlots + HOTBAR_SIZE)) {
            event.setCancelled(true);
            ConsoleUtil.sendDebug("Blocked item pickup due to inventory restriction for " + player.getName());
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;

        int slot = player.getInventory().getHeldItemSlot();
        ConsoleUtil.sendDebug("Item Drop - Player: " + player.getName() + ", Slot: " + slot);

        if (slot >= HOTBAR_SIZE) {
            event.setCancelled(true);
            ConsoleUtil.sendDebug("Blocked item drop from restricted slot " + slot + " for " + player.getName());
        }
    }

    private boolean hasSpaceInAllowedSlots(Player player) {
        return getTotalItemsInAllowedSlots(player) < (allowedSlots + HOTBAR_SIZE);
    }

    private int getTotalItemsInAllowedSlots(Player player) {
        int count = 0;
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < HOTBAR_SIZE + allowedSlots; i++) {
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
                PlayerInventory inventory = player.getInventory();
                for (int i = HOTBAR_SIZE + allowedSlots; i < PLAYER_INVENTORY_SIZE; i++) {
                    moveItemToAllowedSlot(player, i);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    private void moveItemToAllowedSlot(Player player, int fromSlot) {
        ItemStack item = player.getInventory().getItem(fromSlot);
        if (item != null && !item.getType().isAir()) {
            ConsoleUtil.sendDebug("Attempting to move item from slot " + fromSlot + " for " + player.getName());
            player.getInventory().setItem(fromSlot, null);
            if (hasSpaceInAllowedSlots(player)) {
                HashMap<Integer, ItemStack> leftover = new HashMap<>();
                for (int i = 0; i < HOTBAR_SIZE + allowedSlots; i++) {
                    if (player.getInventory().getItem(i) == null) {
                        player.getInventory().setItem(i, item);
                        ConsoleUtil.sendDebug("Successfully moved item " + item.getType() + " to allowed slot " + i + " for " + player.getName());
                        return;
                    }
                }
                if (!leftover.isEmpty()) {
                    for (ItemStack dropItem : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), dropItem);
                        ConsoleUtil.sendDebug("Dropped item " + dropItem.getType() + " for " + player.getName() + " due to no space");
                    }
                }
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                ConsoleUtil.sendDebug("Dropped item " + item.getType() + " for " + player.getName() + " due to no space in allowed slots");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) return;

        String locationKey = getLocationKey(block);
        int vaultNumber = getChestNumberFromSign(locationKey);

        if (vaultNumber == -1) return; // Not a PlayerVault sign

        int reoffenderVaultNumber = plugin.getModuleManager().getConfigManager().getReoffenderVaultNumber();

        ConsoleUtil.sendDebug("Compact Modifier - Player " + player.getName() + " attempting to access Vault #" + vaultNumber);

        if (vaultNumber == reoffenderVaultNumber) {
            event.setCancelled(true);
            MessageUtil.sendPrefixMessage(player, "&cYou cannot access vaults while under the Compact modifier effect.");
            ConsoleUtil.sendDebug("Compact Modifier blocked " + player.getName() + " from accessing Vault #" + vaultNumber);
        }
    }

    private String getLocationKey(Block block) {
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        return world + ";;" + x + ";;" + y + ";;" + z;
    }

    private int getChestNumberFromSign(String locationKey) {
        File signsFile = new File(plugin.getDataFolder().getParentFile(), "PlayerVaults/signs.yml");
        if (!signsFile.exists()) {
            ConsoleUtil.sendError("signs.yml file not found!");
            return -1;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(signsFile);
        if (config.contains(locationKey)) {
            return config.getInt(locationKey + ".chest", -1);
        }
        ConsoleUtil.sendDebug("Chest number not found for sign at " + locationKey);
        return -1;
    }

    public void reloadConfig() {
        loadConfig();
    }
}