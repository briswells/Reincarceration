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
import org.kif.reincarceration.modifier.core.ModifierModule;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.ItemUtil;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.modifier.core.ModifierManager;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;
    private final ModifierManager modifierManager;

    public BlockBreakListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);

        ModifierModule modifierModule = plugin.getModuleManager().getModule(ModifierModule.class);
        this.modifierManager = modifierModule.getModifierManager();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(player);

        ConsoleUtil.sendDebug("Player " + player.getName() + " broke a block. Associated with base group: " + isAssociated);

        if (isAssociated) {
            try {
                IModifier activeModifier = modifierManager.getActiveModifier(player);
                if (activeModifier != null && activeModifier.handleBlockBreak(event)) {
                    // The modifier handled the event, so we're done
                    return;
                }

                // If we get here, either there was no active modifier or it didn't handle the event
                handleDefaultBreak(event);
            } catch (Exception e) {
                ConsoleUtil.sendError("Error handling block break: " + e.getMessage());
                event.setCancelled(true);
            }
        }
    }

    private void handleDefaultBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        List<ItemStack> containerContents = new ArrayList<>();

        // Check if the block is a container (has an inventory)
        BlockState state = block.getState();
        if (state instanceof Container) {
            Container container = (Container) state;
            for (ItemStack item : container.getInventory().getContents()) {
                if (item != null) {
                    containerContents.add(item.clone());
                }
            }
            container.getInventory().clear();
        }

        // Cancel normal drops
        event.setDropItems(false);

        // Get and flag the drops
        List<ItemStack> drops = new ArrayList<>(block.getDrops(player.getInventory().getItemInMainHand()));
        for (ItemStack drop : drops) {
            ItemUtil.addReincarcerationFlag(drop);
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
            ConsoleUtil.sendDebug("Dropped flagged block item: " + drop.getType().name());
        }

        // Drop container contents without flagging
        for (ItemStack item : containerContents) {
            block.getWorld().dropItemNaturally(block.getLocation(), item);
            ConsoleUtil.sendDebug("Dropped container item: " + item.getType().name() +
                    ", Flagged: " + ItemUtil.hasReincarcerationFlag(item));
        }

        // Handle special cases
        handleSpecialCases(block);
    }

    private void handleSpecialCases(Block block) {
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