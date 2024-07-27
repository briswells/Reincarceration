package org.kif.reincarceration.modifier.core;

import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractModifier implements IModifier {
    private final String id;
    private final String name;
    private final String description;
    private final Set<UUID> activePlayers;

    public AbstractModifier(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.activePlayers = new HashSet<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void apply(Player player) {
        activePlayers.add(player.getUniqueId());
    }

    @Override
    public void remove(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    @Override
    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    @Override
    public boolean isSecret() {
        return false; // By default, modifiers are not secret
    }

    @Override
    public boolean handleBlockBreak(BlockBreakEvent event) {
        // Default implementation returns false, indicating that the BlockBreakListener should handle it
        return false;
    }

    @Override
    public boolean handleFishing(PlayerFishEvent event) {
        // Default implementation returns false, indicating that the FishingListener should handle it
        return false;
    }

    @Override
    public boolean handleBuyTransaction(PreTransactionEvent event) {
        ShopItem shopItem = event.getShopItem();
        if (shopItem != null) {
            ItemStack itemStack = shopItem.getItemToGive();
            if (itemStack != null) {
                ItemUtil.addReincarcerationFlag(itemStack);
                ConsoleUtil.sendDebug(getName() + " modifier applied flag to purchased item: " + itemStack.getType() + " x" + event.getAmount());
            }
        }
        return true;
    }

    @Override
    public boolean handleSellTransaction(PreTransactionEvent event) {
        Player player = event.getPlayer();
        ShopItem shopItem = event.getShopItem();
        if (shopItem != null) {
            ItemStack itemStack = ((ShopItem) shopItem).getItemToGive();
            if (itemStack != null) {
                ConsoleUtil.sendDebug(getName() + " modifier allowing sale of item: " + itemStack.getType() + " x" + event.getAmount() + " by player " + player.getName());
            }
        }
        // Default behavior: allow all sales
        return false;
    }

    @Override
    public boolean handlePostTransaction(PostTransactionEvent event) {
        // Default implementation returns false, indicating that the PostTransactionListener should handle it
        return false;
    }

    @Override
    public boolean handleVaultAccess(PlayerInteractEvent event) {
        // Default implementation returns false, indicating that the VaultAccessListener should handle it
        return false;
    }
}