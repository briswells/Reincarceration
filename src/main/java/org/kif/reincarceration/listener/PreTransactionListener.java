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
        ConsoleUtil.sendDebug("Event details: " + event.toString());
        ConsoleUtil.sendDebug("Shop item: " + event.getShopItem());
        ConsoleUtil.sendDebug("Amount: " + event.getAmount());
        ConsoleUtil.sendDebug("Price: " + event.getPrice());
        ConsoleUtil.sendDebug("Transaction Type: " + event.getTransactionType());

        Player player = event.getPlayer();
        ConsoleUtil.sendDebug("Player: " + player.getName());

        if (!permissionManager.isAssociatedWithBaseGroup(player)) {
            ConsoleUtil.sendDebug("Player is not associated with the base group. Exiting.");
            return;
        }

        if (event.getTransactionType() == Transaction.Type.SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_COMMAND ||
                event.getTransactionType() == Transaction.Type.SELL_GUI_SCREEN ||
                event.getTransactionType() == Transaction.Type.SHOPSTAND_SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.QUICK_SELL) {

            ConsoleUtil.sendDebug("Processing SELL transaction for " + player.getName());
            outputInventoryContents(player);

            Map<ShopItem, Integer> itemsToSell = event.getItems();
            ConsoleUtil.sendDebug("Items to sell: " + itemsToSell);
            boolean hasUnflaggedItem = false;

            for (Map.Entry<ShopItem, Integer> entry : itemsToSell.entrySet()) {
                ShopItem shopItem = entry.getKey();
                ItemStack itemToCheck = shopItem.getShopItem();
                ConsoleUtil.sendDebug("Checking item: " + itemToCheck);

                if (!ItemUtil.hasReincarcerationFlag(itemToCheck)) {
                    ConsoleUtil.sendDebug("Found unflagged item: " + itemToCheck);
                    hasUnflaggedItem = true;
                    break;
                }
            }

            if (hasUnflaggedItem) {
                event.setCancelled(true);
                MessageUtil.sendPrefixMessage(player, "&cTransaction cancelled. You can only sell items associated with the reincarceration system.");
                ConsoleUtil.sendDebug("Blocked sale of unflagged items for " + player.getName());
            } else {
                ConsoleUtil.sendDebug("All items are flagged appropriately for sale.");
            }
        } else if (event.getTransactionType() == Transaction.Type.BUY_SCREEN ||
                event.getTransactionType() == Transaction.Type.BUY_STACKS_SCREEN ||
                event.getTransactionType() == Transaction.Type.SHOPSTAND_BUY_SCREEN ||
                event.getTransactionType() == Transaction.Type.QUICK_BUY) {

            ConsoleUtil.sendDebug("Processing BUY transaction for " + player.getName());
            outputInventoryContents(player);

            Map<ShopItem, Integer> itemsToBuy = event.getItems();
            ConsoleUtil.sendDebug("Items to buy: " + itemsToBuy);

            // You can add similar checks or additional logic for buying transactions if needed
            // For now, just logging the items to buy
            for (Map.Entry<ShopItem, Integer> entry : itemsToBuy.entrySet()) {
                ShopItem shopItem = entry.getKey();
                ItemStack itemToCheck = shopItem.getShopItem();
                ConsoleUtil.sendDebug("Item to buy: " + itemToCheck);
            }

            ConsoleUtil.sendDebug("Buy transaction processed successfully for " + player.getName());
        } else {
            ConsoleUtil.sendDebug("Unhandled transaction type for player " + player.getName() + ": " + event.getTransactionType());
            ConsoleUtil.sendDebug("Event details: " + event.toString());
            ConsoleUtil.sendDebug("Items involved: " + event.getItems());
        }
    }

    private ItemStack createItemToDrop(ItemStack item, int amount) {
        ConsoleUtil.sendDebug("Creating item to drop: " + item + " with amount: " + amount);
        ItemStack itemToDrop = item.clone();
        itemToDrop.setAmount(amount);
        ConsoleUtil.sendDebug("Item to drop created: " + itemToDrop);
        return itemToDrop;
    }

    private void dropItem(Player player, ItemStack item) {
        Location dropLocation = player.getLocation();
        ConsoleUtil.sendDebug("Dropping item: " + item + " at location: " + dropLocation);
        player.getWorld().dropItemNaturally(dropLocation, item);
        ConsoleUtil.sendDebug("Item dropped: " + item);
    }

    private void outputInventoryContents(Player player) {
        ConsoleUtil.sendDebug("Outputting inventory contents for player: " + player.getName());
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && !item.getType().isAir()) {
                boolean hasFlag = ItemUtil.hasReincarcerationFlag(item);
                ConsoleUtil.sendDebug("Slot " + i + ": " + item.getType() + " x" + item.getAmount() + " - Flagged: " + hasFlag);
            }
        }
    }
}
