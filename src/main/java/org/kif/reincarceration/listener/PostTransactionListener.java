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

import java.util.Map;

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
        ConsoleUtil.sendDebug(event.toString());
        ConsoleUtil.sendDebug(String.valueOf(event.getShopItem()));
        ConsoleUtil.sendDebug(String.valueOf(event.getItems()));
        ConsoleUtil.sendDebug(String.valueOf(event.getItemStack()));
        ConsoleUtil.sendDebug(String.valueOf(event.getTransactionType()));
        ConsoleUtil.sendDebug(String.valueOf(event.getAmount()));
        ConsoleUtil.sendDebug(String.valueOf(event.getPrice()));
        if (event.getTransactionType() == Transaction.Type.BUY_SCREEN ||
                event.getTransactionType() == Transaction.Type.BUY_STACKS_SCREEN ||
                event.getTransactionType() == Transaction.Type.QUICK_BUY) {

            Player player = event.getPlayer();
            if (!permissionManager.isAssociatedWithBaseGroup(player)) return;

            ConsoleUtil.sendDebug("PostTransaction Immediate Inventory Check for " + player.getName() + ":");
            outputInventoryContents(player);

            // Schedule another check after 10 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    ConsoleUtil.sendDebug("PostTransaction Delayed (10s) Inventory Check for " + player.getName() + ":");
                    outputInventoryContents(player);
                }
            }.runTaskLater(plugin, 200L); // 200 ticks = 10 seconds

            Map<ShopItem, Integer> boughtItems = event.getItems();

            ConsoleUtil.sendDebug("Player " + player.getName() + " bought " + boughtItems.size() + " types of items.");

            new BukkitRunnable() {
                int attempts = 0;
                @Override
                public void run() {
                    attempts++;
                    boolean allFlagged = true;

                    for (Map.Entry<ShopItem, Integer> entry : boughtItems.entrySet()) {
                        ShopItem shopItem = entry.getKey();
                        int amount = entry.getValue();
                        ItemStack boughtItemType = shopItem.getItemToGive();

                        boolean itemFlagged = false;
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null && item.isSimilar(boughtItemType) && item.getAmount() == amount) {
                                if (!ItemUtil.hasReincarcerationFlag(item)) {
                                    ItemUtil.addReincarcerationFlag(item);
                                    ConsoleUtil.sendDebug("Flagged purchased item: " + item.getType() + " x" + item.getAmount());
                                }
                                itemFlagged = true;
                                break;
                            }
                        }

                        if (!itemFlagged) {
                            allFlagged = false;
                        }
                    }

                    if (allFlagged || attempts >= 10) {  // Try for up to 10 ticks (0.5 seconds)
                        this.cancel();
                        if (!allFlagged) {
                            ConsoleUtil.sendError("Failed to flag all purchased items for " + player.getName() + " after " + attempts + " attempts.");
                        }
                    }
                }
            }.runTaskTimer(plugin, 1L, 1L);  // Start after 1 tick, run every tick
        }
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