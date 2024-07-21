package org.kif.reincarceration.modifier.types;

import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.Transaction;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LumberjackModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private boolean provideAxeOnDeath;
    private final Set<Material> allowedItems;

    public LumberjackModifier(Reincarceration plugin) {
        super("lumberjack", "Lumberjack", "Provides woodcutting benefits and restricts selling to wood-related items");
        this.plugin = plugin;
        this.allowedItems = new HashSet<>();
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.lumberjack");
        if (config != null) {
            this.provideAxeOnDeath = config.getBoolean("provide_axe_on_death", false);
            List<String> allowedItemsList = config.getStringList("allowed_items");
            if (!allowedItemsList.isEmpty()) {
                for (String item : allowedItemsList) {
                    try {
                        Material material = Material.valueOf(item.toUpperCase());
                        allowedItems.add(material);
                    } catch (IllegalArgumentException e) {
                        ConsoleUtil.sendError("Invalid material in Lumberjack modifier config: " + item);
                    }
                }
            } else {
                ConsoleUtil.sendError("No allowed items specified in Lumberjack modifier config. Using default values.");
                initializeDefaultAllowedItems();
            }
        } else {
            ConsoleUtil.sendError("Lumberjack modifier configuration not found. Using default values.");
            this.provideAxeOnDeath = false;
            initializeDefaultAllowedItems();
        }
        ConsoleUtil.sendDebug("Lumberjack Modifier Config: Provide Axe On Death = " + provideAxeOnDeath);
        ConsoleUtil.sendDebug("Lumberjack Modifier Config: Allowed Items = " + allowedItems);
    }

    private void initializeDefaultAllowedItems() {
        // Default allowed items in case the config is not found or invalid
        allowedItems.add(Material.OAK_LOG);
        allowedItems.add(Material.SPRUCE_LOG);
        allowedItems.add(Material.BIRCH_LOG);
        allowedItems.add(Material.JUNGLE_LOG);
        allowedItems.add(Material.ACACIA_LOG);
        allowedItems.add(Material.DARK_OAK_LOG);
        allowedItems.add(Material.CRIMSON_STEM);
        allowedItems.add(Material.WARPED_STEM);
        allowedItems.add(Material.OAK_PLANKS);
        allowedItems.add(Material.SPRUCE_PLANKS);
        allowedItems.add(Material.BIRCH_PLANKS);
        allowedItems.add(Material.JUNGLE_PLANKS);
        allowedItems.add(Material.ACACIA_PLANKS);
        allowedItems.add(Material.DARK_OAK_PLANKS);
        allowedItems.add(Material.CRIMSON_PLANKS);
        allowedItems.add(Material.WARPED_PLANKS);
        allowedItems.add(Material.OAK_SAPLING);
        allowedItems.add(Material.SPRUCE_SAPLING);
        allowedItems.add(Material.BIRCH_SAPLING);
        allowedItems.add(Material.JUNGLE_SAPLING);
        allowedItems.add(Material.ACACIA_SAPLING);
        allowedItems.add(Material.DARK_OAK_SAPLING);
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        ConsoleUtil.sendDebug("Applied Lumberjack Modifier to " + player.getName());
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        ConsoleUtil.sendDebug("Removed Lumberjack Modifier from " + player.getName());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isActive(player) && provideAxeOnDeath) {
            // Use a delayed task to ensure the item is given after respawn
            new BukkitRunnable() {
                @Override
                public void run() {
                    provideWoodenAxe(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    public boolean handlePreTransaction(PreTransactionEvent event) {
        if (event.getTransactionType() == Transaction.Type.SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SHOPSTAND_SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_GUI_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_COMMAND ||
                event.getTransactionType() == Transaction.Type.AUTO_SELL_CHEST ||
                event.getTransactionType() == Transaction.Type.QUICK_SELL) {

            Player player = event.getPlayer();

            if (areAllItemsFlagged(player)) {
                ConsoleUtil.sendDebug("All items are flagged for player: " + player.getName());
            } else {
                MessageUtil.sendPrefixMessage(player, "&cTransaction Denied: Prohibited Items found on Player.");
                event.setCancelled(true);
                return true;
            }

            ShopItem shopItem = event.getShopItem();
            if (shopItem != null) {
                ItemStack itemStack = shopItem.getItemToGive();
                ConsoleUtil.sendDebug("ShopItem: " + shopItem);
                ConsoleUtil.sendDebug("ItemStack: " + itemStack);
                if (itemStack != null) {
                    ConsoleUtil.sendDebug("Item Type: " + itemStack.getType());
                    ConsoleUtil.sendDebug("Item Amount: " + itemStack.getAmount());
                    ConsoleUtil.sendDebug("Item Meta: " + itemStack.getItemMeta());
                    if (!allowedItems.contains(itemStack.getType())) {
                        event.setCancelled(true);
                        MessageUtil.sendPrefixMessage(player, "&cTransaction Denied - Attempted to sell prohibited items.");
                        ConsoleUtil.sendDebug("Transaction cancelled because item " + itemStack.getType() + " is not allowed.");
                        ConsoleUtil.sendDebug("Cancelled: " + event.isCancelled());
                        return true;
                    }
                }
            }
        }
        return true;
    }

    private boolean areAllItemsFlagged(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir() && !ItemUtil.hasReincarcerationFlag(item)) {
                ConsoleUtil.sendDebug("Unflagged item found: " + item.getType() + " for player: " + player.getName());
                return false;
            }
        }
        return true;
    }

    private void provideWoodenAxe(Player player) {
        if (!player.getInventory().contains(Material.WOODEN_AXE)) {
            player.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
            ConsoleUtil.sendDebug("Provided wooden axe to " + player.getName());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }
}
