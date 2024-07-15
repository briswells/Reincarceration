package org.kif.reincarceration.modifier.types;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;

public class TortoiseModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private double speedDecrease;
    private double miningSpeedIncrease;

    public TortoiseModifier(Reincarceration plugin) {
        super("tortoise", "Tortoise", "Decreases movement speed but increases mining speed");
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.tortoise");
        if (config != null) {
            this.speedDecrease = config.getDouble("speed_decrease", 0.3);
            this.miningSpeedIncrease = config.getDouble("mining_speed_increase", 0.5);
        } else {
            ConsoleUtil.sendError("Tortoise modifier configuration not found. Using default values.");
            this.speedDecrease = 0.3;
            this.miningSpeedIncrease = 0.5;
        }
        ConsoleUtil.sendDebug("Tortoise Modifier Config: Speed Decrease = " + speedDecrease + ", Mining Speed Increase = " + miningSpeedIncrease);
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        applySpeedEffect(player);
        startEffectChecker(player);
        ConsoleUtil.sendDebug("Applied Tortoise Modifier to " + player.getName());
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        removeSpeedEffect(player);
        ConsoleUtil.sendDebug("Removed Tortoise Modifier from " + player.getName());
    }

    private void applySpeedEffect(Player player) {
        int amplifier = (int) (speedDecrease * 10) - 1; // Convert to potion effect amplifier
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, amplifier, false, false));
    }

    private void removeSpeedEffect(Player player) {
        player.removePotionEffect(PotionEffectType.SLOW);
    }

    private void startEffectChecker(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive(player)) {
                    this.cancel();
                    return;
                }
                if (!player.hasPotionEffect(PotionEffectType.SLOW)) {
                    applySpeedEffect(player);
                    ConsoleUtil.sendDebug("Reapplied Tortoise speed effect to " + player.getName());
                }
            }
        }.runTaskTimer(plugin, 20L * 30, 20L * 30); // Check every 30 seconds
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            // Simulate faster mining by giving a brief haste effect
            int amplifier = (int) (miningSpeedIncrease * 10) - 1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, amplifier, false, false));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            // Use a delayed task to ensure the effect is applied after respawn
            new BukkitRunnable() {
                @Override
                public void run() {
                    applySpeedEffect(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (isActive(player)) {
            // Reapply effect on next tick to override any potion effects from consumed item
            new BukkitRunnable() {
                @Override
                public void run() {
                    applySpeedEffect(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    public void reloadConfig() {
        loadConfig();
    }
}