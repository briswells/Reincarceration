package org.kif.reincarceration.modifier.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.AbstractModifier;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.util.ItemUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NeolithicModifier extends AbstractModifier {
    private final Reincarceration plugin;
    private final Set<Material> allowedTools;
    private final Set<Material> allowedBlocks;

    public NeolithicModifier(Reincarceration plugin) {
        super("neolithic", "Neolithic", "Limits players to basic tools and provides unique gathering bonuses");
        this.plugin = plugin;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.neolithic");
        this.allowedTools = loadMaterialSet(config, "allowed_tools");
        this.allowedBlocks = loadMaterialSet(config, "allowed_blocks");
    }

    @Override
    public boolean handleBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!allowedTools.contains(itemInHand.getType()) && isTool(itemInHand.getType())) {
            event.setCancelled(true);
            MessageUtil.sendPrefixMessage(player, "&cYou can't use advanced tools!");
            ConsoleUtil.sendDebug(player.getName() + " attempted to use " + itemInHand.getType() + " but was prevented by Neolithic Modifier");
            return true;
        }

        if (!allowedBlocks.contains(block.getType())) {
            event.setCancelled(true);
            MessageUtil.sendPrefixMessage(player, "&cYou can't break this type of block!");
            return true;
        }

        // Handle normal block breaking
        event.setDropItems(false);
        for (ItemStack drop : block.getDrops(itemInHand)) {
            ItemUtil.addReincarcerationFlag(drop);
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }

        // Handle special cases (cactus and sugar cane)
        if (block.getType() == Material.CACTUS || block.getType() == Material.SUGAR_CANE) {
            handleConnectedBlocks(block);
        }

        return true; // We've handled this event
    }

    private void handleConnectedBlocks(Block startBlock) {
        Material material = startBlock.getType();
        Block above = startBlock.getRelative(0, 1, 0);
        while (above.getType() == material) {
            ItemStack drop = new ItemStack(material);
            ItemUtil.addReincarcerationFlag(drop);
            above.setType(Material.AIR);
            above.getWorld().dropItemNaturally(above.getLocation(), drop);
            ConsoleUtil.sendDebug("Dropped flagged connected block: " + material.name());
            above = above.getRelative(0, 1, 0);
        }
    }

    private boolean isTool(Material material) {
        return material.name().endsWith("_PICKAXE") || material.name().endsWith("_AXE") ||
                material.name().endsWith("_SHOVEL") || material.name().endsWith("_HOE") ||
                material.name().endsWith("_SWORD");
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
}