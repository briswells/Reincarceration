package org.kif.reincarceration.modifier.core;

import me.gypopo.economyshopgui.api.events.PostTransactionEvent;
import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface IModifier {
    String getId();
    String getName();
    String getDescription();
    void apply(Player player);
    void remove(Player player);
    boolean isActive(Player player);
    boolean handleBlockBreak(BlockBreakEvent event);
    boolean handleFishing(PlayerFishEvent event);
    boolean handlePreTransaction(PreTransactionEvent event);
    boolean handlePostTransaction(PostTransactionEvent event);
    boolean handleVaultAccess(PlayerInteractEvent event);
}