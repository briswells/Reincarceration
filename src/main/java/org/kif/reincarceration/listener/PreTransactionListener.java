package org.kif.reincarceration.listener;

import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.Transaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.modifier.core.ModifierManager;
import org.kif.reincarceration.modifier.core.ModifierModule;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.permission.PermissionManager;

import java.sql.SQLException;
import java.util.Map;

public class PreTransactionListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;
    private final ModifierManager modifierManager;

    public PreTransactionListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);

        ModifierModule modifierModule = plugin.getModuleManager().getModule(ModifierModule.class);
        this.modifierManager = modifierModule.getModifierManager();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPreTransaction(PreTransactionEvent event) throws SQLException {
        Player player = event.getPlayer();

        ConsoleUtil.sendDebug("PreTransaction Entering for player: " + player.getName());
        ConsoleUtil.sendDebug("Transaction type: " + event.getTransactionType());

        if (!permissionManager.isAssociatedWithBaseGroup(player)) {
            ConsoleUtil.sendDebug("Player is not associated with the base group. Exiting.");
            return;
        }

        if(areAllItemsFlagged(player)) {
            ConsoleUtil.sendDebug("All items are flagged for player: " + player.getName());
        } else {
//            ConsoleUtil.sendDebug("Unflagged items found in inventory for player: " + player.getName());
            MessageUtil.sendPrefixMessage(player, "&cTransaction Denied: Prohibited Items found on Player.");
            event.setCancelled(true);
            return;
        }

        IModifier activeModifier = modifierManager.getActiveModifier(player);
        if (activeModifier != null && activeModifier.handlePreTransaction(event)) {
            // The modifier handled the event, so we're done
            return;
        }
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
}