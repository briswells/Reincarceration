package org.kif.reincarceration.modifier.types;

import me.gypopo.economyshopgui.api.events.PreTransactionEvent;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.Transaction;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.MessageUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnglerModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private boolean provideRodOnDeath;
    private boolean preventRodDurabilityLoss;
    private final Set<Material> allowedItems;

    public AnglerModifier(Reincarceration plugin) {
        super("angler", "Angler", "Provides fishing benefits and restricts selling to fish-related items");
        this.plugin = plugin;
        this.allowedItems = new HashSet<>(Arrays.asList(
            Material.COD,
            Material.SALMON,
            Material.TROPICAL_FISH,
            Material.PUFFERFISH,
            Material.NAUTILUS_SHELL,
            Material.FISHING_ROD,
            Material.ENCHANTED_BOOK,
            Material.BOW,
            Material.LILY_PAD,
            Material.BOWL,
            Material.LEATHER,
            Material.LEATHER_BOOTS,
            Material.SADDLE,
            Material.NAME_TAG,
            Material.TRIPWIRE_HOOK,
            Material.ROTTEN_FLESH,
            Material.STICK,
            Material.STRING,
            Material.BONE,
            Material.INK_SAC,
            Material.BAMBOO
        ));
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.angler");
        if (config != null) {
            this.provideRodOnDeath = config.getBoolean("provide_rod_on_death", true);
            this.preventRodDurabilityLoss = config.getBoolean("prevent_rod_durability_loss", true);
        } else {
            ConsoleUtil.sendError("Angler modifier configuration not found. Using default values.");
            this.provideRodOnDeath = true;
            this.preventRodDurabilityLoss = true;
        }
        ConsoleUtil.sendDebug("Angler Modifier Config: Provide Rod On Death = " + provideRodOnDeath +
                ", Prevent Rod Durability Loss = " + preventRodDurabilityLoss);
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        ConsoleUtil.sendDebug("Applied Angler Modifier to " + player.getName());
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        ConsoleUtil.sendDebug("Removed Angler Modifier from " + player.getName());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isActive(player) && provideRodOnDeath) {
            // Use a delayed task to ensure the item is given after respawn
            new BukkitRunnable() {
                @Override
                public void run() {
                    provideFishingRod(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (isActive(player) && preventRodDurabilityLoss) {
            ItemStack fishingRod = player.getInventory().getItemInMainHand();
            if (fishingRod.getType() == Material.FISHING_ROD) {
                // Restore durability using setDamage
                if (fishingRod.getItemMeta() instanceof Damageable) {
                    Damageable meta = (Damageable) fishingRod.getItemMeta();
                    meta.setDamage(0);
                    fishingRod.setItemMeta((ItemMeta) meta);
                    ConsoleUtil.sendDebug("Restored durability of fishing rod for " + player.getName());
                }
            }
        }
    }

//    @EventHandler
//    public void onPreTransaction(PreTransactionEvent event) {
//        if (event.getTransactionType() == Transaction.Type.SELL_SCREEN ||
//            event.getTransactionType() == Transaction.Type.SELL_ALL_SCREEN ||
//            event.getTransactionType() == Transaction.Type.QUICK_SELL) {
//
//            Player player = event.getPlayer();
//
//            if (!isActive(player)) return;
//
//            Map<ShopItem, Integer> itemsToSell = event.getItems();
//
//            for (ShopItem shopItem : itemsToSell.keySet()) {
//                ItemStack itemToSell = shopItem.getShopItem();
//                if (!allowedItems.contains(itemToSell.getType())) {
//                    event.setCancelled(true);
//                    MessageUtil.sendPrefixMessage(player, "&cYou can only sell items obtained from fishing while using the Angler modifier.");
//                    return;
//                }
//            }
//        }
//    }

    private void provideFishingRod(Player player) {
        if (!player.getInventory().contains(Material.FISHING_ROD)) {
            player.getInventory().addItem(new ItemStack(Material.FISHING_ROD));
            ConsoleUtil.sendDebug("Provided fishing rod to " + player.getName());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }
}