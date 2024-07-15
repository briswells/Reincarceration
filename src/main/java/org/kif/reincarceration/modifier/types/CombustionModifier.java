package org.kif.reincarceration.modifier.types;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitTask;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.bukkit.event.entity.EntityCombustEvent;
import org.kif.reincarceration.util.ConsoleUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CombustionModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Random random = new Random();

    private int checkInterval;
    private int fireDuration;
    private int spreadFireDuration;
    private double spreadRadius;
    private boolean spreadFireEnabled;
    private double combustionChance;

    public CombustionModifier(Reincarceration plugin) {
        super("combustion", "Combustion", "Players will spontaneously combust, advised to keep close to water sources");
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        checkInterval = plugin.getConfig().getInt("modifiers.combustion.check_interval", 100);
        fireDuration = plugin.getConfig().getInt("modifiers.combustion.fire_duration", 200);
        spreadFireDuration = plugin.getConfig().getInt("modifiers.combustion.spread_fire_duration", 100);
        spreadRadius = plugin.getConfig().getDouble("modifiers.combustion.spread_radius", 5.0);
        spreadFireEnabled = plugin.getConfig().getBoolean("modifiers.combustion.spread_fire_enabled", true);
        combustionChance = plugin.getConfig().getDouble("modifiers.combustion.combustion_chance", 0.1);
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> checkAndApplyFire(player), 0L, checkInterval);
        activeTasks.put(player.getUniqueId(), task);
        ConsoleUtil.sendDebug("CombustionModifier applied to " + player.getName());
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        player.setFireTicks(0);
        ConsoleUtil.sendDebug("CombustionModifier removed from " + player.getName());
    }

    private void checkAndApplyFire(Player player) {
        if (!player.isOnline()) {
            remove(player);
            return;
        }

        ConsoleUtil.sendDebug("Checking fire for " + player.getName() + ". Current fire ticks: " + player.getFireTicks());

        if (player.getFireTicks() > 0) {
            player.setFireTicks(fireDuration);
            ConsoleUtil.sendDebug("Reapplied fire to " + player.getName() + ". New fire ticks: " + player.getFireTicks());
            if (spreadFireEnabled) {
                spreadFireToNearbyPlayers(player);
            }
        } else if (random.nextDouble() < combustionChance) {
            player.setFireTicks(fireDuration);
            ConsoleUtil.sendDebug(player.getName() + " spontaneously combusted. Fire ticks: " + player.getFireTicks());
        }
    }

    private void spreadFireToNearbyPlayers(Player sourcePlayer) {
        ConsoleUtil.sendDebug("Attempting to spread fire from " + sourcePlayer.getName());
        for (Player nearbyPlayer : sourcePlayer.getWorld().getPlayers()) {
            if (nearbyPlayer != sourcePlayer && !isActive(nearbyPlayer) &&
                nearbyPlayer.getLocation().distance(sourcePlayer.getLocation()) <= spreadRadius) {
                nearbyPlayer.setFireTicks(spreadFireDuration);
                ConsoleUtil.sendDebug("Spread fire to " + nearbyPlayer.getName() + ". Fire ticks: " + nearbyPlayer.getFireTicks());
            }
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isActive(player)) {
                event.setDuration(fireDuration);
                ConsoleUtil.sendDebug("EntityCombustEvent: Set fire duration for " + player.getName() + " to " + fireDuration);
            }
        }
    }
}