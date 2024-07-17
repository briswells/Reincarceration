package org.kif.reincarceration.listener;

import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.Transaction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.permission.PermissionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreTransactionListener implements Listener {

    private final Reincarceration plugin;
    private final PermissionManager permissionManager;
    private final boolean cancelEntireTransactionOnUnflaggedItem;

    public PreTransactionListener(Reincarceration plugin, boolean cancelEntireTransaction) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
        this.cancelEntireTransactionOnUnflaggedItem = cancelEntireTransaction;
    }

        @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreTransaction(PreTransactionEvent event) {
        ConsoleUtil.sendDebug("PreTransaction Entering");
        event.setCancelled(true);
        ConsoleUtil.sendDebug(event.toString());
        ConsoleUtil.sendDebug(String.valueOf(event.getShopItem()));
        ConsoleUtil.sendDebug(String.valueOf(event.getAmount()));
        ConsoleUtil.sendDebug(String.valueOf(event.getPrice()));
        if (event.getTransactionType() == Transaction.Type.SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_SCREEN ||
                event.getTransactionType() == Transaction.Type.QUICK_SELL) {

            Player player = event.getPlayer();
            if (!permissionManager.isAssociatedWithBaseGroup(player)) return;

            ConsoleUtil.sendDebug("PreTransaction Inventory Check for " + player.getName() + ":");
            outputInventoryContents(player);

            Map<ShopItem, Integer> itemsToSell = event.getItems();
            boolean hasUnflaggedItem = false;

            for (Map.Entry<ShopItem, Integer> entry : itemsToSell.entrySet()) {
                ShopItem shopItem = entry.getKey();
                ItemStack itemToCheck = shopItem.getShopItem();

                if (!ItemUtil.hasReincarcerationFlag(itemToCheck)) {
                    hasUnflaggedItem = true;
                    break;
                }
            }

            if (hasUnflaggedItem) {
                event.setCancelled(true);
                MessageUtil.sendPrefixMessage(player, "&cTransaction cancelled. You can only sell items associated with the reincarceration system.");
                ConsoleUtil.sendDebug("Blocked sale of unflagged items for " + player.getName());
            }
        }
    }

    private ItemStack createItemToDrop(ItemStack item, int amount) {
        ItemStack itemToDrop = item.clone();
        itemToDrop.setAmount(amount);
        return itemToDrop;
    }

    private void dropItem(Player player, ItemStack item) {
        Location dropLocation = player.getLocation();
        player.getWorld().dropItemNaturally(dropLocation, item);
    }

    private void outputInventoryContents(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                boolean hasFlag = ItemUtil.hasReincarcerationFlag(item);
                ConsoleUtil.sendDebug("Slot " + i + ": " + item.getType() + " x" + item.getAmount() + " - Flagged: " + hasFlag);
            }
        }
    }
}