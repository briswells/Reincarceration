package org.kif.reincarceration.modifier.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
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

    public NeolithicModifier(Reincarceration plugin) {
        super("neolithic", "Neolithic", "Limits players to basic tools and provides unique gathering bonuses");
        this.plugin = plugin;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modifiers.neolithic");
        assert config != null;
        this.allowedTools = loadMaterialSet(config, "allowed_tools");
    }

    @Override
    public boolean handleBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!allowedTools.contains(itemInHand.getType()) && isTool(itemInHand.getType())) {
            event.setCancelled(true);
            MessageUtil.sendPrefixMessage(player, "&cYou can only use basic tools!");
            ConsoleUtil.sendDebug(player.getName() + " attempted to use " + itemInHand.getType() + " but was prevented by Neolithic Modifier");
            return true;
        }

        // Handle block breaking
        handleBlockBreak(event, block, player);

        return true; // We've handled this event
    }

    private void handleBlockBreak(BlockBreakEvent event, Block block, Player player) {
        // Cancel normal drops
        event.setDropItems(false);

        // Handle special cases first
        if (handleSpecialCases(event)) {
            return; // If it was a special case, we're done
        }

        // If not a special case, handle normal drops
        List<ItemStack> drops = new ArrayList<>(block.getDrops(player.getInventory().getItemInMainHand()));
        for (ItemStack drop : drops) {
            ItemUtil.addReincarcerationFlag(drop);
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
            ConsoleUtil.sendDebug("Dropped flagged block item: " + drop.getType().name());
        }

        // Handle container contents if necessary
        handleContainerContents(block);
    }

    private boolean handleSpecialCases(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CACTUS || block.getType() == Material.SUGAR_CANE) {
            if (!event.isCancelled()) {
                breakConnectedBlocks(block, block.getType(), event.getPlayer());
                return true; // We've handled this special case
            } else {
                ConsoleUtil.sendDebug("Block break was cancelled. Not handling connected blocks for " + block.getType());
            }
        }
        return false; // Not a special case
    }

    private void breakConnectedBlocks(Block startBlock, Material material, Player player) {
        List<Block> connectedBlocks = new ArrayList<>();
        connectedBlocks.add(startBlock);

        Block above = startBlock.getRelative(BlockFace.UP);
        while (above.getType() == material && connectedBlocks.size() < 3) {
            connectedBlocks.add(above);
            above = above.getRelative(BlockFace.UP);
        }

        // Drop items for all connected blocks
        for (Block block : connectedBlocks) {
            dropFlaggedItem(block.getLocation(), material, player);
            block.setType(Material.AIR);
        }

        ConsoleUtil.sendDebug("Broke and dropped " + connectedBlocks.size() + " connected " + material.name() + " blocks");
    }

    private void dropFlaggedItem(org.bukkit.Location location, Material material, Player player) {
        ItemStack drop = new ItemStack(material);
        ItemUtil.addReincarcerationFlag(drop);
        player.getWorld().dropItemNaturally(location, drop);
        ConsoleUtil.sendDebug("Dropped flagged item: " + material.name());
    }

    private void handleContainerContents(Block block) {
        if (block.getState() instanceof Container) {
            Container container = (Container) block.getState();
            for (ItemStack item : container.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                    ConsoleUtil.sendDebug("Dropped container item: " + item.getType().name() +
                            ", Flagged: " + ItemUtil.hasReincarcerationFlag(item));
                }
            }
            container.getInventory().clear();
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