package org.kif.reincarceration.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;

public class ItemCraftingListener implements Listener {
    private final Reincarceration plugin;

    public ItemCraftingListener(Reincarceration plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();

        if (result != null) {
            boolean allIngredientsFlagged = true;
            for (ItemStack ingredient : inventory.getMatrix()) {
                if (ingredient != null && !ItemUtil.hasReincarcerationFlag(ingredient)) {
                    allIngredientsFlagged = false;
                    break;
                }
            }

            if (allIngredientsFlagged) {
                ItemUtil.addReincarcerationFlag(result);
                inventory.setResult(result);
                ConsoleUtil.sendDebug("Prepared flagged crafted item: " + result.getType().name() + " x" + result.getAmount());
            }
        }
    }
}
