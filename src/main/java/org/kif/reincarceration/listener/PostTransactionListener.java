package org.kif.reincarceration.listener;

import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.Transaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.permission.PermissionManager;

public class PostTransactionListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public PostTransactionListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPostTransaction(PostTransactionEvent event) {
        ConsoleUtil.sendDebug("PostTransaction Entering");
        ConsoleUtil.sendDebug("Event details: " + event.toString());
        ConsoleUtil.sendDebug("Shop item: " + event.getShopItem());
        ConsoleUtil.sendDebug("Items: " + event.getItems());
        ConsoleUtil.sendDebug("Item stack: " + event.getItemStack());
        ConsoleUtil.sendDebug("Transaction type: " + event.getTransactionType());
        ConsoleUtil.sendDebug("Amount: " + event.getAmount());
        ConsoleUtil.sendDebug("Price: " + event.getPrice());

        Player player = event.getPlayer();
        ConsoleUtil.sendDebug("Player: " + player.getName());

        if (!permissionManager.isAssociatedWithBaseGroup(player)) {
            ConsoleUtil.sendDebug("Player is not associated with the base group. Exiting.");
            return;
        }

        if (event.getTransactionType() == Transaction.Type.BUY_SCREEN ||
                event.getTransactionType() == Transaction.Type.BUY_STACKS_SCREEN ||
                event.getTransactionType() == Transaction.Type.SHOPSTAND_BUY_SCREEN ||
                event.getTransactionType() == Transaction.Type.QUICK_BUY) {

            ConsoleUtil.sendDebug("Processing BUY transaction for " + player.getName());
            outputInventoryContents(player);

            ItemStack boughtItemType = event.getItemStack();
            int amount = event.getAmount();

            if (boughtItemType != null) {
                ConsoleUtil.sendDebug("Processing purchase: " + boughtItemType.getType() + " x" + amount +
                        " (Max stack size: " + boughtItemType.getMaxStackSize() + ")");
                flagPurchasedItem(player, boughtItemType, amount);
            } else {
                ConsoleUtil.sendError("Bought item is null for player " + player.getName());
            }

            // Schedule another check after 10 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    ConsoleUtil.sendDebug("PostTransaction Delayed (10s) Inventory Check for " + player.getName() + ":");
                    outputInventoryContents(player);
                }
            }.runTaskLater(plugin, 200L); // 200 ticks = 10 seconds
        } else {
            ConsoleUtil.sendDebug("Unhandled transaction type for player " + player.getName() + ": " + event.getTransactionType());
            ConsoleUtil.sendDebug("Event details: " + event.toString());
            ConsoleUtil.sendDebug("Items involved: " + event.getItems());
        }
    }

    private void flagPurchasedItem(Player player, ItemStack itemType, int totalAmount) {
        new BukkitRunnable() {
            int attempts = 0;
            int flaggedAmount = 0;

            @Override
            public void run() {
                attempts++;
                for (ItemStack item : player.getInventory().getContents()) {
                    if (item != null && item.getType() == itemType.getType()) {
                        if (!ItemUtil.hasReincarcerationFlag(item)) {
                            ItemUtil.addReincarcerationFlag(item);
                            flaggedAmount += item.getAmount();
                            ConsoleUtil.sendDebug("Flagged item: " + item.getType() + " x" + item.getAmount() +
                                    " (Total flagged: " + flaggedAmount + "/" + totalAmount + ")");
                        }
                    }
                    if (flaggedAmount >= totalAmount) {
                        break;
                    }
                }

                if (flaggedAmount >= totalAmount || attempts >= 10) {
                    this.cancel();
                    if (flaggedAmount < totalAmount) {
                        ConsoleUtil.sendError("Failed to flag all items for " + player.getName() +
                                ". Flagged: " + flaggedAmount + "/" + totalAmount +
                                " of " + itemType.getType());
                    } else {
                        ConsoleUtil.sendDebug("Successfully flagged all items for " + player.getName() +
                                ": " + itemType.getType() + " x" + totalAmount);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
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