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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.kif.reincarceration.util.ItemUtil.addReincarcerationFlag;

public class AnglerModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private boolean provideRodOnDeath;
    private boolean preventRodDurabilityLoss;
    private final Set<Material> allowedItems;

    public AnglerModifier(Reincarceration plugin) {
        super("angler", "Angler", "Provides fishing benefits and restricts selling to fish-related items");
        this.plugin = plugin;
        this.allowedItems = new HashSet<>();
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.angler");
        if (config != null) {
            this.provideRodOnDeath = config.getBoolean("provide_rod_on_death", true);
            this.preventRodDurabilityLoss = config.getBoolean("prevent_rod_durability_loss", true);

            List<String> allowedItemsList = config.getStringList("allowed_items");
            if (!allowedItemsList.isEmpty()) {
                for (String item : allowedItemsList) {
                    try {
                        Material material = Material.valueOf(item.toUpperCase());
                        allowedItems.add(material);
                    } catch (IllegalArgumentException e) {
                        ConsoleUtil.sendError("Invalid material in Angler modifier config: " + item);
                    }
                }
            } else {
                ConsoleUtil.sendError("No allowed items specified in Angler modifier config. Using default values.");
                initializeDefaultAllowedItems();
            }
        } else {
            ConsoleUtil.sendError("Angler modifier configuration not found. Using default values.");
            this.provideRodOnDeath = true;
            this.preventRodDurabilityLoss = true;
            initializeDefaultAllowedItems();
        }
        ConsoleUtil.sendDebug("Angler Modifier Config: Provide Rod On Death = " + provideRodOnDeath +
                ", Prevent Rod Durability Loss = " + preventRodDurabilityLoss);
        ConsoleUtil.sendDebug("Angler Modifier Config: Allowed Items = " + allowedItems);
    }

    private void initializeDefaultAllowedItems() {
        allowedItems.add(Material.COD);
        allowedItems.add(Material.SALMON);
        allowedItems.add(Material.TROPICAL_FISH);
        allowedItems.add(Material.PUFFERFISH);
        allowedItems.add(Material.NAUTILUS_SHELL);
        allowedItems.add(Material.FISHING_ROD);
        allowedItems.add(Material.ENCHANTED_BOOK);
        allowedItems.add(Material.BOW);
        allowedItems.add(Material.LILY_PAD);
        allowedItems.add(Material.BOWL);
        allowedItems.add(Material.LEATHER);
        allowedItems.add(Material.LEATHER_BOOTS);
        allowedItems.add(Material.SADDLE);
        allowedItems.add(Material.NAME_TAG);
        allowedItems.add(Material.TRIPWIRE_HOOK);
        allowedItems.add(Material.ROTTEN_FLESH);
        allowedItems.add(Material.STICK);
        allowedItems.add(Material.STRING);
        allowedItems.add(Material.BONE);
        allowedItems.add(Material.INK_SAC);
        allowedItems.add(Material.BAMBOO);
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

    @EventHandler
    public void onPreTransaction(PreTransactionEvent event) {
        if (event.getTransactionType() == Transaction.Type.SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SHOPSTAND_SELL_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_GUI_SCREEN ||
                event.getTransactionType() == Transaction.Type.SELL_ALL_COMMAND ||
                event.getTransactionType() == Transaction.Type.AUTO_SELL_CHEST ||
                event.getTransactionType() == Transaction.Type.QUICK_SELL) {

            Player player = event.getPlayer();

            if (isActive(player)) {
                ShopItem shopItem = event.getShopItem();
                if (shopItem != null) {
                    ItemStack itemStack = shopItem.getItemToGive();
                    ConsoleUtil.sendDebug("ShopItem: " + shopItem);
                    ConsoleUtil.sendDebug("ItemStack: " + itemStack);
                    if (itemStack != null) {
                        ConsoleUtil.sendDebug("Item Type: " + itemStack.getType());
                        ConsoleUtil.sendDebug("Item Amount: " + itemStack.getAmount());
                        ConsoleUtil.sendDebug("Item Meta: " + itemStack.getItemMeta());
                        if (!allowedItems.contains(itemStack.getType())) {
                            event.setCancelled(true);
                            MessageUtil.sendPrefixMessage(player, "&cTransaction Denied - Attempted to sell prohibited items.");
                            ConsoleUtil.sendDebug("Transaction cancelled because item " + itemStack.getType() + " is not allowed.");
                            ConsoleUtil.sendDebug("Cancelled: " + event.isCancelled());
                            return;
                        }
                    }
                }
            }
        }
    }

    private void provideFishingRod(Player player) {
        if (!player.getInventory().contains(Material.FISHING_ROD)) {
            ItemStack item = new ItemStack(Material.FISHING_ROD);
            addReincarcerationFlag(item);
            player.getInventory().addItem(item);

            ConsoleUtil.sendDebug("Provided fishing rod to " + player.getName());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }
}
