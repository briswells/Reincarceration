package org.kif.reincarceration.listener;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.core.CoreModule;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.economy.EconomyManager;
import org.kif.reincarceration.economy.EconomyModule;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerListener implements Listener {
    private final Reincarceration plugin;
    private final DataManager dataManager;
    private final ConfigManager configManager;
    private final EconomyManager economyManager;
    private final PermissionManager permissionManager;

    public PlayerListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
        CoreModule coreModule = plugin.getModuleManager().getModule(CoreModule.class);
        DataModule dataModule = plugin.getModuleManager().getModule(DataModule.class);
        EconomyModule economyModule = plugin.getModuleManager().getModule(EconomyModule.class);

        if (coreModule == null || dataModule == null || economyModule == null) {
            throw new IllegalStateException("Required modules are not initialized");
        }

        this.configManager = coreModule.getConfigManager();
        this.dataManager = dataModule.getDataManager();
        this.economyManager = economyModule.getEconomyManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            dataManager.createPlayerData(player);
        } catch (SQLException e) {
            plugin.getLogger().severe("Error initializing data inside database for " + player.getName() + ": " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // If needed, perform any cleanup or data saving operations here
        // For now, we'll leave this empty as our data is saved in real-time
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);

        ConsoleUtil.sendDebug("Player " + player.getName() + " broke a block. Associated with base group: " + isAssociated);

        if (isAssociated) {
            List<ItemStack> containerContents = new ArrayList<>();

            // Check if the block is a container (has an inventory)
            BlockState state = event.getBlock().getState();
            if (state instanceof Container) {
                Container container = (Container) state;
                for (ItemStack item : container.getInventory().getContents()) {
                    if (item != null) {
                        containerContents.add(item.clone());
                    }
                }
                container.getInventory().clear();
            }

            // Cancel normal drops
            event.setDropItems(false);

            // Flag and drop the block itself
            for (ItemStack drop : event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand())) {
                if (drop != null) {
                    ItemUtil.addReincarcerationFlag(drop);
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
                    ConsoleUtil.sendDebug("Dropped flagged block item: " + drop.getType().name());
                }
            }

            // Drop container contents without flagging
            for (ItemStack item : containerContents) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
                ConsoleUtil.sendDebug("Dropped container item: " + item.getType().name() +
                        ", Flagged: " + ItemUtil.hasReincarcerationFlag(item));
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);
        boolean hasFlag = ItemUtil.hasReincarcerationFlag(item);

        if (isAssociated && !hasFlag) {
            event.setCancelled(true);
        } else if (!isAssociated && hasFlag) {
            ItemUtil.removeReincarcerationFlag(item);
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();

        if (result != null) {
            boolean allIngredientsFlagged = true;
            for (ItemStack ingredient : inventory.getMatrix()) {
                if (ingredient != null && !ItemUtil.hasReincarcerationFlag(ingredient)) {
                    allIngredientsFlagged = false;
                    break;
                }
            }

            if (allIngredientsFlagged) {
                ItemUtil.addReincarcerationFlag(result);
                inventory.setResult(result);
                ConsoleUtil.sendDebug("Prepared flagged crafted item: " + result.getType().name() + " x" + result.getAmount());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);

        // Existing crafting logic
        if (event.getInventory().getType() == InventoryType.WORKBENCH ||
                event.getInventory().getType() == InventoryType.CRAFTING) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                ItemStack craftedItem = event.getCurrentItem();
                if (craftedItem != null && !craftedItem.getType().isAir() && ItemUtil.hasReincarcerationFlag(craftedItem)) {
                    ConsoleUtil.sendDebug("Crafted flagged item: " + craftedItem.getType().name() + " x" + craftedItem.getAmount());
                }
            }
            // Allow all interactions within crafting inventories
            return;
        }

        // New restriction logic
        if (isAssociated) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            // Check clicked item
            if (clickedItem != null && !clickedItem.getType().isAir() && !ItemUtil.hasReincarcerationFlag(clickedItem)) {
                // Allow interactions within player inventory
                if (event.getClickedInventory() != player.getInventory()) {
                    event.setCancelled(true);
                    MessageUtil.sendPrefixMessage(player, "&cYou cannot interact with unflagged items outside your inventory.");
                    ConsoleUtil.sendDebug("Cancelled inventory click for " + player.getName() + " on unflagged item: " + clickedItem.getType().name());
                    return;
                }
            }

            // Check cursor item (for placing items)
            if (cursorItem != null && !cursorItem.getType().isAir() && !ItemUtil.hasReincarcerationFlag(cursorItem)) {
                // Allow placing items into player inventory
                if (event.getClickedInventory() != player.getInventory()) {
                    event.setCancelled(true);
                    MessageUtil.sendPrefixMessage(player, "&cYou cannot place unflagged items outside your inventory.");
                    ConsoleUtil.sendDebug("Cancelled inventory click for " + player.getName() + " with unflagged cursor item: " + cursorItem.getType().name());
                }
            }
        }
    }

    @EventHandler
    public void onSmeltItem(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        if (ItemUtil.hasReincarcerationFlag(source)) {
            ItemStack result = event.getResult();
            ItemUtil.addReincarcerationFlag(result);
            ConsoleUtil.sendDebug("Flagged smelted item: " + result.getType().name());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);

        if (isAssociated) {
            for (Integer slot : event.getRawSlots()) {
                if (slot >= player.getInventory().getSize()) {
                    // This slot is outside the player's inventory
                    ItemStack draggedItem = event.getNewItems().get(slot);
                    if (draggedItem != null && !draggedItem.getType().isAir() && !ItemUtil.hasReincarcerationFlag(draggedItem)) {
                        event.setCancelled(true);
                        MessageUtil.sendPrefixMessage(player, "&cYou cannot drag unflagged items outside your inventory.");
                        ConsoleUtil.sendDebug("Cancelled inventory drag for " + player.getName() + " involving unflagged item: " + draggedItem.getType().name());
                        return;
                    }
                }
            }
        }
    }
}