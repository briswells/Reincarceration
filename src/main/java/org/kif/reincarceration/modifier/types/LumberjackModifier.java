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
import org.kif.reincarceration.util.MessageUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LumberjackModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private boolean provideAxeOnDeath;
    private final Set<Material> allowedItems;

    public LumberjackModifier(Reincarceration plugin) {
        super("lumberjack", "Lumberjack", "Provides woodcutting benefits and restricts selling to wood-related items");
        this.plugin = plugin;
        this.allowedItems = new HashSet<>();
        initializeAllowedItems();
        loadConfig();
    }

    private void initializeAllowedItems() {
        // Add all types of logs
        allowedItems.addAll(Arrays.asList(
                Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
                Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
                Material.CRIMSON_STEM, Material.WARPED_STEM
        ));

        // Add all types of planks
        allowedItems.addAll(Arrays.asList(
                Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS,
                Material.JUNGLE_PLANKS, Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS,
                Material.CRIMSON_PLANKS, Material.WARPED_PLANKS
        ));

        // Add all types of saplings
        allowedItems.addAll(Arrays.asList(
                Material.OAK_SAPLING, Material.SPRUCE_SAPLING, Material.BIRCH_SAPLING,
                Material.JUNGLE_SAPLING, Material.ACACIA_SAPLING, Material.DARK_OAK_SAPLING
        ));
    }

    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.lumberjack");
        if (config != null) {
            this.provideAxeOnDeath = config.getBoolean("provide_axe_on_death", true);
        } else {
            ConsoleUtil.sendError("Lumberjack modifier configuration not found. Using default values.");
            this.provideAxeOnDeath = true;
        }
        ConsoleUtil.sendDebug("Lumberjack Modifier Config: Provide Axe On Death = " + provideAxeOnDeath);
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

    @EventHandler
    public void onPreTransaction(PreTransactionEvent event) {
        if (event.getTransactionType() == Transaction.Type.SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SHOPSTAND_SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_GUI_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_COMMAND ||
                event.getTransactionType() == Transaction.Type.AUTO_SELL_CHEST ||
                event.getTransactionType() == Transaction.Type.QUICK_SELL) {
            ConsoleUtil.sendDebug("10565: Lumberjack: ");

            Player player = event.getPlayer();

            if (!isActive(player)) return;
            Map<ShopItem, Integer> itemsToSell = event.getItems();
            ConsoleUtil.sendDebug("10565: Amount: " + event.getAmount());
            ConsoleUtil.sendDebug("10565: ShopItem: " + event.getShopItem());
            ConsoleUtil.sendDebug("10565: Items: " + event.getItems());
            ConsoleUtil.sendDebug("10565: Player: " + event.getPlayer());

            for (ShopItem shopItem : itemsToSell.keySet()) {
                ItemStack itemToSell = shopItem.getShopItem();
                if (!allowedItems.contains(itemToSell.getType())) {
                    event.setCancelled(true);
                    MessageUtil.sendPrefixMessage(player, "&cYou can only sell logs, planks, and saplings while using the Lumberjack modifier.");
                    return;
                }
            }
        }
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