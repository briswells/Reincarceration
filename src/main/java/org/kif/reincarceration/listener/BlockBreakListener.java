package org.kif.reincarceration.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public BlockBreakListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);

        ConsoleUtil.sendDebug("Player " + player.getName() + " broke a block. Associated with base group: " + isAssociated);

        if (isAssociated) {
            handleBlockBreak(event);
            handleSpecialCases(event);
        }
    }

    private void handleBlockBreak(BlockBreakEvent event) {
        List<ItemStack> containerContents = new ArrayList<>();

        // Check if the block is a container (has an inventory)
        BlockState state = event.getBlock().getState();
        if (state instanceof Container) {
            Container container = (Container) state;
            for (ItemStack item : container.getInventory().getContents()) {
                if (item != null) {
                    containerContents.add(item.clone());
                }
            }
            container.getInventory().clear();
        }

        if (event.isCancelled()) {
            return;
        }
        // Cancel normal drops
        event.setDropItems(false);

        // Flag and drop the block itself
        for (ItemStack drop : event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand())) {
            if (drop != null) {
                ItemUtil.addReincarcerationFlag(drop);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
                ConsoleUtil.sendDebug("Dropped flagged block item: " + drop.getType().name());
            }
        }

        // Drop container contents without flagging
        for (ItemStack item : containerContents) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
            ConsoleUtil.sendDebug("Dropped container item: " + item.getType().name() +
                    ", Flagged: " + ItemUtil.hasReincarcerationFlag(item));
        }
    }

    private void handleSpecialCases(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CACTUS || block.getType() == Material.SUGAR_CANE) {
            flagConnectedBlocks(block, block.getType());
        }
    }

    private void flagConnectedBlocks(Block startBlock, Material material) {
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
}