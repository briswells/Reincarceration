package org.kif.reincarceration.listener;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.modifier.core.ModifierManager;
import org.kif.reincarceration.modifier.core.ModifierModule;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;

import java.sql.SQLException;

public class FishingListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;
    private final ModifierManager modifierManager;

    public FishingListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);

        ModifierModule modifierModule = plugin.getModuleManager().getModule(ModifierModule.class);
        this.modifierManager = modifierModule.getModifierManager();
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) throws SQLException {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player.getUniqueId());

        if (event.getCaught() instanceof Item) {
            Item caughtItem = (Item) event.getCaught();
            ItemStack fishItem = caughtItem.getItemStack();
            boolean hasFlag = ItemUtil.hasReincarcerationFlag(fishItem);

            if (isAssociated && !hasFlag) {

                IModifier activeModifier = modifierManager.getActiveModifier(player);
                if (activeModifier != null && activeModifier.handleFishing(event)) {
                    // The modifier handled the event, so we're done
                    return;
                }
                // Associated player caught non-flagged fish, flag it
                ItemUtil.addReincarcerationFlag(fishItem);
                caughtItem.setItemStack(fishItem);
                ConsoleUtil.sendDebug("Flagged caught fish for associated player: " + player.getName());
            } else if (!isAssociated && hasFlag) {
                // Non-associated player caught flagged fish, remove the flag
                ItemUtil.removeReincarcerationFlag(fishItem);
                caughtItem.setItemStack(fishItem);
                ConsoleUtil.sendDebug("Removed flag from caught fish for non-associated player: " + player.getName());
            }
        }
    }
}
