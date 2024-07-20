package org.kif.reincarceration.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ItemUtil;

public class ItemPickupListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public ItemPickupListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        // Check if the player is an operator
        if (player.isOp()) return;

        ItemStack item = event.getItem().getItemStack();

        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);
        boolean hasFlag = ItemUtil.hasReincarcerationFlag(item);

        if (isAssociated && !hasFlag) {
            event.setCancelled(true);
        } else if (!isAssociated && hasFlag) {
            ItemUtil.removeReincarcerationFlag(item);
        }
    }
}
