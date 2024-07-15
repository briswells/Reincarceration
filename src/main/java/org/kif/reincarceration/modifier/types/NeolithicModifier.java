package org.kif.reincarceration.modifier.types;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.MessageUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NeolithicModifier extends AbstractModifier implements Listener {
    private final Reincarceration plugin;
    private final Set<Material> allowedTools;
    private final Set<Material> allowedBlocks;
    private final double doubleDropChance;

    public NeolithicModifier(Reincarceration plugin) {
        super("neolithic", "Neolithic", "Limits players to basic tools and provides unique gathering bonuses");
        this.plugin = plugin;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.neolithic");
        this.allowedTools = loadMaterialSet(config, "allowed_tools");
        this.allowedBlocks = loadMaterialSet(config, "allowed_blocks");
        this.doubleDropChance = config.getDouble("double_drop_chance", 0.2);
    }

    private Set<Material> loadMaterialSet(ConfigurationSection config, String path) {
        Set<Material> materials = new HashSet<>();
        List<String> materialNames = config.getStringList(path);
        for (String name : materialNames) {
            Material material = Material.getMaterial(name.toUpperCase());
            if (material != null) {
                materials.add(material);
            } else {
                ConsoleUtil.sendError("Invalid material in neolithic config: " + name);
            }
        }
        return materials;
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
//        MessageUtil.sendPrefixMessage(player, "&6You've entered with the Neolithic modifier. Only basic tools are available, but your gathering skills are enhanced!");
        ConsoleUtil.sendDebug("Applied Neolithic Modifier to " + player.getName());
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        ConsoleUtil.sendDebug("Removed Neolithic Modifier from " + player.getName());
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!isActive(player)) return;

        ItemStack result = event.getRecipe().getResult();
        if (!allowedTools.contains(result.getType()) && isTool(result.getType())) {
            event.setCancelled(true);
            MessageUtil.sendPrefixMessage(player, "&cYou can't craft advanced tools!");
            ConsoleUtil.sendDebug(player.getName() + " attempted to craft " + result.getType() + " but was prevented by Neolithic Modifier");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isActive(player)) return;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!allowedTools.contains(itemInHand.getType()) && isTool(itemInHand.getType())) {
            event.setCancelled(true);
            MessageUtil.sendPrefixMessage(player, "&cYou can't use advanced tools!");
            ConsoleUtil.sendDebug(player.getName() + " attempted to use " + itemInHand.getType() + " but was prevented by Neolithic Modifier");
            return;
        }

        if (allowedBlocks.contains(event.getBlock().getType())) {
            if (Math.random() < doubleDropChance) {
                for (ItemStack drop : event.getBlock().getDrops(itemInHand)) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
                }
                MessageUtil.sendPrefixMessage(player, "&6Your Neolithic skills have yielded extra resources!");
                ConsoleUtil.sendDebug(player.getName() + " received double drops from " + event.getBlock().getType());
            }
        }
    }

    private boolean isTool(Material material) {
        return material.name().endsWith("_PICKAXE") || material.name().endsWith("_AXE") ||
                material.name().endsWith("_SHOVEL") || material.name().endsWith("_HOE") ||
                material.name().endsWith("_SWORD");
    }
}