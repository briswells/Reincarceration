package org.kif.reincarceration.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.permission.PermissionManager;

public class InventoryChangeListener implements Listener {

    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public InventoryChangeListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!permissionManager.isAssociatedWithBaseGroup(player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && !clickedItem.getType().isAir()) {
            if (!ItemUtil.hasReincarcerationFlag(clickedItem)) {
                ItemUtil.addReincarcerationFlag(clickedItem);
                ConsoleUtil.sendDebug("Flagged item from inventory click: " + clickedItem.getType() + " x" + clickedItem.getAmount());
            }
        }
    }
}