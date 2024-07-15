package org.kif.reincarceration.util;

import com.drtshock.playervaults.vaultmanagement.VaultManager;
import com.drtshock.playervaults.vaultmanagement.VaultOperations;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.kif.reincarceration.Reincarceration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VaultUtil {
    private static final Logger LOGGER = Bukkit.getLogger();
    private static Reincarceration plugin;

    public static void initialize(Reincarceration plugin) {
        VaultUtil.plugin = plugin;
    }

    public static void clearVaultContents(String playerUUID) {
        int vaultNumber = plugin.getModuleManager().getConfigManager().getReoffenderVaultNumber();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                VaultManager vaultManager = VaultManager.getInstance();
                int vaultSize = VaultOperations.getMaxVaultSize(playerUUID);
                Inventory vault = vaultManager.loadOtherVault(playerUUID, vaultNumber, vaultSize);

                if (vault == null) {
                    ConsoleUtil.sendError(String.format("Vault %d not found for player %s.", vaultNumber, playerUUID));
                    return;
                }

                vault.clear();
                vaultManager.saveVault(vault, playerUUID, vaultNumber);
                ConsoleUtil.sendInfo(String.format("Cleared vault %d for player %s.", vaultNumber, playerUUID));
            } catch (Exception e) {
                ConsoleUtil.sendError(String.format("Failed to clear vault %d for player %s", vaultNumber, playerUUID));
                LOGGER.log(Level.SEVERE, "Error details:", e);
            }
        });
    }

    public static void isVaultEmpty(String playerUUID, VaultCheckCallback callback) {
        int vaultNumber = plugin.getModuleManager().getConfigManager().getReoffenderVaultNumber();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int vaultSize = VaultOperations.getMaxVaultSize(playerUUID);
                Inventory vault = VaultManager.getInstance().loadOtherVault(playerUUID, vaultNumber, vaultSize);

                boolean isEmpty = vault == null || vault.isEmpty();
                callback.onCheckComplete(isEmpty);
                ConsoleUtil.sendDebug(String.format("Checked if vault %d for player %s is empty: %s", vaultNumber, playerUUID, isEmpty));
            } catch (Exception e) {
                ConsoleUtil.sendError(String.format("Failed to check if vault %d for player %s is empty", vaultNumber, playerUUID));
                LOGGER.log(Level.SEVERE, "Error details:", e);
                callback.onCheckComplete(false);
            }
        });
    }

    public static void ensureVaultCleared(String playerUUID, int attempts) {
        if (attempts <= 0) {
            plugin.getLogger().warning(String.format("Failed to clear vault for player %s after maximum attempts.", playerUUID));
            return;
        }

        isVaultEmpty(playerUUID, isEmpty -> {
            int vaultNumber = plugin.getModuleManager().getConfigManager().getReoffenderVaultNumber();
            if (isEmpty) {
                ConsoleUtil.sendDebug(String.format("Vault %d for player %s is empty.", vaultNumber, playerUUID));
            } else {
                ConsoleUtil.sendDebug(String.format("Vault %d for player %s is not empty. Attempting to clear...", vaultNumber, playerUUID));
                clearVaultContents(playerUUID);
                // Schedule another check after a delay (5 seconds = 100 ticks)
                Bukkit.getScheduler().runTaskLater(plugin, () -> ensureVaultCleared(playerUUID, attempts - 1), 100L);
            }
        });
    }

    @FunctionalInterface
    public interface VaultCheckCallback {
        void onCheckComplete(boolean isEmpty);
    }

    private String getLocationKey(Block block) {
        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        return world + ";;" + x + ";;" + y + ";;" + z;
    }

    private int getChestNumberFromSign(String locationKey) {
        File signsFile = new File(plugin.getDataFolder().getParentFile(), "PlayerVaults/signs.yml");
        if (!signsFile.exists()) {
            plugin.getLogger().severe("signs.yml file not found!");
            return -1;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(signsFile);
        if (config.contains(locationKey)) {
            return config.getInt(locationKey + ".chest", -1);
        }
        plugin.getLogger().warning("Chest number not found for sign at " + locationKey);
        return -1;
    }

    private int getAllowedChestNumber(String playerName) {
        return plugin.getModuleManager().getConfigManager().getReoffenderVaultNumber();
    }
}