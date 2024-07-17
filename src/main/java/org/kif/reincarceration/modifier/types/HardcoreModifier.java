package org.kif.reincarceration.modifier.types;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.cycle.CycleManager;
import org.kif.reincarceration.cycle.CycleModule;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;

public class HardcoreModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private final int maxHearts;

    public HardcoreModifier(Reincarceration plugin) {
        super("hardcore", "Hardcore", "Limited hearts and one death means game over!");
        this.plugin = plugin;
        this.maxHearts = plugin.getConfig().getInt("modifiers.hardcore.max_hearts", 1);
        ConsoleUtil.sendDebug("HardcoreModifier initialized with max hearts: " + maxHearts);
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        setMaxHealth(player);
        ConsoleUtil.sendDebug("Applied Hardcore modifier to " + player.getName() + " with " + maxHearts + " max hearts");
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        resetMaxHealth(player);
        ConsoleUtil.sendDebug("Removed Hardcore modifier from " + player.getName());
    }

    private void setMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            double oldValue = attribute.getBaseValue();
            attribute.setBaseValue(maxHearts * 2); // Each heart is 2 health points
            player.setHealth(attribute.getValue()); // Set current health to new max
            ConsoleUtil.sendDebug("Set max health for " + player.getName() + " from " + oldValue + " to " + attribute.getBaseValue());
        } else {
            ConsoleUtil.sendDebug("Failed to set max health for " + player.getName() + ": Attribute is null");
        }
    }

    private void resetMaxHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) {
            double oldValue = attribute.getBaseValue();
            attribute.setBaseValue(20); // Reset to default 10 hearts (20 health points)
            player.setHealth(attribute.getValue()); // Set current health to new max
            ConsoleUtil.sendDebug("Reset max health for " + player.getName() + " from " + oldValue + " to " + attribute.getBaseValue());
        } else {
            ConsoleUtil.sendDebug("Failed to reset max health for " + player.getName() + ": Attribute is null");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
//        ConsoleUtil.sendDebug("EntityDeathEvent triggered");
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ConsoleUtil.sendDebug("Dead entity is a player: " + player.getName());

            boolean isActive = isActive(player);
            ConsoleUtil.sendDebug("Is Hardcore modifier active for " + player.getName() + ": " + isActive);

            if (isActive) {
                ConsoleUtil.sendDebug("Player " + player.getName() + " died with Hardcore modifier active. Attempting to quit cycle.");
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    ConsoleUtil.sendDebug("Executing quitCycle for " + player.getName());
                    CycleManager cycleManager = getCycleManager();
                    if (cycleManager != null) {
                        cycleManager.quitCycle(player);
                        ConsoleUtil.sendDebug("quitCycle execution completed for " + player.getName());
                    } else {
                        ConsoleUtil.sendError("CycleManager is null. Cannot quit cycle for " + player.getName());
                    }
                });
            } else {
                ConsoleUtil.sendDebug("Hardcore modifier not active for " + player.getName() + ". No action taken.");
            }
        } else {
            String hello = "hello";
//            ConsoleUtil.sendDebug("Dead entity is not a player. Entity type: " + event.getEntityType());
        }
    }

    private CycleManager getCycleManager() {
        CycleModule cycleModule = plugin.getModuleManager().getModule(CycleModule.class);
        return cycleModule != null ? cycleModule.getCycleManager() : null;
    }
}