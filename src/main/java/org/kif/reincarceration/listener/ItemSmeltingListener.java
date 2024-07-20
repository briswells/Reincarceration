package org.kif.reincarceration.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;

public class ItemSmeltingListener implements Listener {
    private final Reincarceration plugin;

    public ItemSmeltingListener(Reincarceration plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSmeltItem(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        if (ItemUtil.hasReincarcerationFlag(source)) {
            ItemStack result = event.getResult();
            ItemUtil.addReincarcerationFlag(result);
            ConsoleUtil.sendDebug("Flagged smelted item: " + result.getType().name());
        }
    }
}
