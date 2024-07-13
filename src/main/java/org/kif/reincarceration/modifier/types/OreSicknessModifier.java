package org.kif.reincarceration.modifier.types;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;

import java.util.*;

public class OreSicknessModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private final Map<Material, PotionEffectType> oreEffects = new HashMap<>();
    private final boolean effectOnBreak;
    private final boolean effectOnSight;
    private final int effectDuration;
    private final int sightCheckRadius;
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    public OreSicknessModifier(Reincarceration plugin) {
        super("ore_sickness", "Ore Sickness", "Applies effects when breaking or seeing certain ores");
        this.plugin = plugin;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.ore_sickness");
        this.effectOnBreak = config.getBoolean("effect_on_break", true);
        this.effectOnSight = config.getBoolean("effect_on_sight", true);
        this.effectDuration = config.getInt("effect_duration", 200);
        this.sightCheckRadius = config.getInt("sight_check_radius", 5);

        ConfigurationSection oresConfig = config.getConfigurationSection("ore_effects");
        for (String oreName : oresConfig.getKeys(false)) {
            Material oreMaterial = Material.getMaterial(oreName);
            String effectName = oresConfig.getString(oreName);
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            if (oreMaterial != null && effectType != null) {
                oreEffects.put(oreMaterial, effectType);
            }
        }

        // Log the configuration
        StringBuilder configSummary = new StringBuilder("OreSicknessModifier Configuration:\n");
        configSummary.append("Effect on Break: ").append(effectOnBreak).append("\n");
        configSummary.append("Effect on Sight: ").append(effectOnSight).append("\n");
        configSummary.append("Effect Duration: ").append(effectDuration).append(" ticks\n");
        configSummary.append("Sight Check Radius: ").append(sightCheckRadius).append("\n");
        configSummary.append("Ore Effects:\n");
        for (Map.Entry<Material, PotionEffectType> entry : oreEffects.entrySet()) {
            configSummary.append("  ").append(entry.getKey()).append(" -> ").append(entry.getValue().getName()).append("\n");
        }
        ConsoleUtil.sendDebug(configSummary.toString());
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        ConsoleUtil.sendDebug("Applying Ore Sickness to " + player.getName());
        if (effectOnSight) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    checkPlayerSight(player);
                }
            };
            task.runTaskTimer(plugin, 0L, 20L);
            activeTasks.put(player.getUniqueId(), task);
            ConsoleUtil.sendDebug("Started sight checking task for " + player.getName());
        }
    }

    @Override
    public void remove(Player player) {
        ConsoleUtil.sendDebug("Removing Ore Sickness from " + player.getName());
        super.remove(player);
        BukkitRunnable task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            ConsoleUtil.sendDebug("Cancelled sight checking task for " + player.getName());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ConsoleUtil.sendDebug("Block break event");
        if (!effectOnBreak) return;

        Player player = event.getPlayer();
        if (!isActive(player)) return;

        ConsoleUtil.sendDebug("Player " + player.getName() + " broke a block");
        Block block = event.getBlock();
        ConsoleUtil.sendDebug("Block type: " + block.getType());
        PotionEffectType effectType = oreEffects.get(block.getType());
        ConsoleUtil.sendDebug("Effect type: " + effectType);
        if (effectType != null) {
            ConsoleUtil.sendDebug("Applying " + effectType.getName() + " effect to " + player.getName());
            PotionEffect currentEffect = player.getPotionEffect(effectType);
            int amplifier = (currentEffect != null) ? currentEffect.getAmplifier() + 1 : 0;
            player.addPotionEffect(new PotionEffect(effectType, effectDuration, amplifier));
        }
    }

    private boolean hasLineOfSight(Player player, Block block) {
        Location eyeLocation = player.getEyeLocation();
        @NotNull Vector direction = block.getLocation().add(0.5, 0.5, 0.5).subtract(eyeLocation).toVector().normalize();
        double maxDistance = eyeLocation.distance(block.getLocation());

        for (double d = 0; d <= maxDistance; d += 0.5) {
            Location checkLocation = eyeLocation.clone().add(direction.clone().multiply(d));
            Block checkBlock = checkLocation.getBlock();

            if (checkBlock.equals(block)) {
                ConsoleUtil.sendDebug("Player can see " + block.getType() + " at " + block.getLocation());
                return true;
            }

            if (checkBlock.getType().isOccluding() && !checkBlock.equals(block)) {
                return false;
            }
        }

        return false;
    }

    private void checkPlayerSight(Player player) {
        ConsoleUtil.sendDebug("Checking sight for " + player.getName());
        Map<PotionEffectType, Integer> visibleOreEffects = new HashMap<>();
        Map<Material, Integer> oresFound = new HashMap<>();
        Map<Material, Integer> oresVisible = new HashMap<>();
        int blocksChecked = 0;

        for (int x = -sightCheckRadius; x <= sightCheckRadius; x++) {
            for (int y = -sightCheckRadius; y <= sightCheckRadius; y++) {
                for (int z = -sightCheckRadius; z <= sightCheckRadius; z++) {
                    blocksChecked++;
                    Block block = player.getLocation().getBlock().getRelative(x, y, z);
                    Material blockType = block.getType();

                    // Check if this block type is in our oreEffects map
                    if (oreEffects.containsKey(blockType)) {
                        oresFound.merge(blockType, 1, Integer::sum);
                        ConsoleUtil.sendDebug("Found " + blockType + " at " + block.getLocation());

                        if (hasLineOfSight(player, block)) {
                            PotionEffectType effectType = oreEffects.get(blockType);
                            visibleOreEffects.merge(effectType, 1, Integer::sum);
                            oresVisible.merge(blockType, 1, Integer::sum);
                            ConsoleUtil.sendDebug("Player can see " + blockType + " at " + block.getLocation());
                        } else {
                            ConsoleUtil.sendDebug("Player cannot see " + blockType + " at " + block.getLocation());
                        }
                    }
                }
            }
        }

        // Apply effects
        for (Map.Entry<PotionEffectType, Integer> entry : visibleOreEffects.entrySet()) {
            player.addPotionEffect(new PotionEffect(entry.getKey(), effectDuration, entry.getValue() - 1));
            ConsoleUtil.sendDebug("Applied effect " + entry.getKey().getName() + " to " + player.getName());
        }

        // Compile summary
        StringBuilder summary = new StringBuilder();
        summary.append("Ore Sickness Summary for ").append(player.getName()).append(":\n");
        summary.append("Blocks checked: ").append(blocksChecked).append("\n");
        summary.append("Ores found: ").append(oresFound.values().stream().mapToInt(Integer::intValue).sum()).append("\n");
        summary.append("Ores visible: ").append(oresVisible.values().stream().mapToInt(Integer::intValue).sum()).append("\n");
        summary.append("Ores breakdown:\n");
        for (Material ore : oreEffects.keySet()) {
            summary.append("  ").append(ore).append(": Found ").append(oresFound.getOrDefault(ore, 0))
                    .append(", Visible ").append(oresVisible.getOrDefault(ore, 0)).append("\n");
        }
        summary.append("Effects applied:\n");
        for (Map.Entry<PotionEffectType, Integer> entry : visibleOreEffects.entrySet()) {
            summary.append("  ").append(entry.getKey().getName())
                    .append(" (Amplifier: ").append(entry.getValue() - 1).append(")\n");
        }

        ConsoleUtil.sendDebug(summary.toString());
    }
}