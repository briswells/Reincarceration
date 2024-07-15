package org.kif.reincarceration.listener;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ItemUtil;

public class FishingListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public FishingListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);

        if (event.getCaught() instanceof Item) {
            Item caughtItem = (Item) event.getCaught();
            ItemStack fishItem = caughtItem.getItemStack();
            boolean hasFlag = ItemUtil.hasReincarcerationFlag(fishItem);

            if (isAssociated && !hasFlag) {
                // Associated player caught non-flagged fish, cancel the event
                event.setCancelled(true);
                caughtItem.remove();
            } else if (!isAssociated && hasFlag) {
                // Non-associated player caught flagged fish, remove the flag
                ItemUtil.removeReincarcerationFlag(fishItem);
                caughtItem.setItemStack(fishItem);
            }
            // If it's an associated player catching a flagged fish, or a non-associated player
            // catching a non-flagged fish, we don't need to do anything special
        }
    }
}
