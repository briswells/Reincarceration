package org.kif.reincarceration.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.ConsoleUtil;

import java.util.List;

public class MobDropListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public MobDropListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Skip if the dead entity is a player
        if (event.getEntity() instanceof Player) {
            return;
        }

        Player killer = event.getEntity().getKiller();

        if (killer != null && permissionManager.isAssociatedWithBaseGroup(killer)) {
            List<ItemStack> drops = event.getDrops();
            for (ItemStack drop : drops) {
                if (drop != null) {
                    ItemUtil.addReincarcerationFlag(drop);
                    ConsoleUtil.sendDebug("Flagged mob drop: " + drop.getType().name() + " x" + drop.getAmount() +
                                          " from " + event.getEntityType().name() + " killed by " + killer.getName());
                }
            }
        }
    }
}