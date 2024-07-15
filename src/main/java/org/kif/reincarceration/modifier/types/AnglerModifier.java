package org.kif.reincarceration.modifier.types;

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

public class AnglerModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private boolean provideRodOnDeath;
    private boolean preventRodDurabilityLoss;

    public AnglerModifier(Reincarceration plugin) {
        super("angler", "Angler", "Provides fishing benefits");
        this.plugin = plugin;
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