package org.kif.reincarceration.cycle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.economy.EconomyManager;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.modifier.core.ModifierManager;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.rank.RankManager;
import org.kif.reincarceration.util.BroadcastUtil;
import org.kif.reincarceration.util.ConsoleUtil;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.util.VaultUtil;

import java.sql.SQLException;

public class CycleManager {
    private final CycleModule cycleModule;
    private final ConfigManager configManager;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    private final RankManager rankManager;
    private final PermissionManager permissionManager;
    private final ModifierManager modifierManager;

    public CycleManager(CycleModule cycleModule, ConfigManager configManager, DataManager dataManager,
                        EconomyManager economyManager, RankManager rankManager, PermissionManager permissionManager, ModifierManager modifierManager) {
        this.cycleModule = cycleModule;
        this.configManager = configManager;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        this.rankManager = rankManager;
        this.permissionManager = permissionManager;
        this.modifierManager = modifierManager;

        ConsoleUtil.sendSuccess("CycleManager initialized with all required components");
    }

    public void startNewCycle(Player player, IModifier modifier) {
        double entryFee = configManager.getEntryFee();
        if (!economyManager.hasEnoughBalance(player, entryFee)) {
            MessageUtil.sendPrefixMessage(player, "&cInsufficent Funds. Entry Fee: " + entryFee );
            return;
        }

        double currentBalance = economyManager.getBalance(player);
        if (economyManager.withdrawMoney(player, entryFee)) {
            try {
                VaultUtil.ensureVaultCleared(player.getUniqueId().toString(), 3);
                dataManager.recordCycleStart(player, modifier.getId());
                dataManager.setPlayerCycleStatus(player, true);
                rankManager.setPlayerRank(player, 0);
                dataManager.setStoredBalance(player, currentBalance - entryFee);
                economyManager.setBalance(player, 0);
                player.setHealth(0.0);
                // apply the modifier after 5 seconds just to not interfere with the player's death
                Bukkit.getScheduler().runTaskLater(cycleModule.getPlugin(), () -> {
                    try {
                        modifierManager.applyModifier(player, modifier);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }, 100L);
//                modifierManager.applyModifier(player, modifier);
                BroadcastUtil.broadcastMessage("§c" + player.getName() + " has been redmitted under the " + modifier.getName() + " modifier");
            } catch (SQLException e) {
                logSevere("Error starting new cycle: " + e.getMessage());

                economyManager.setBalance(player, currentBalance); // Refund the entry fee
                MessageUtil.sendPrefixMessage(player, "&cError during cycle start. Your entry fee has been refunded.");
            }
        } else {
            MessageUtil.sendPrefixMessage(player, "&cAn error occurred while trying to start a new cycle. Please try again later.");
        }
    }

    public void completeCycle(Player player) {
        try {
            if (!dataManager.isPlayerInCycle(player)) {
                MessageUtil.sendPrefixMessage(player, "&cInvalid: Not currently in a cycle.");
                ConsoleUtil.sendError("Player " + player.getName() + " was detected attempting to complete a cycle without being in a cycle. Review player's data and permission setup!");
                return;
            }

            int currentRank = rankManager.getPlayerRank(player);
            int maxRank = configManager.getMaxRank();

            if (currentRank < maxRank) {
                MessageUtil.sendPrefixMessage(player, "&cInsufficent Rank: " + maxRank + " required to complete the cycle.");
                ConsoleUtil.sendError("Player " + player.getName() + " was detected attempting to complete a cycle without reaching the maximum rank. Review player's data and permission setup!");
                return;
            }

            double finalRankUpCost = configManager.getRankUpCost(currentRank);
            if (!economyManager.hasEnoughBalance(player, finalRankUpCost)) {
                MessageUtil.sendPrefixMessage(player, "&cInsufficent Funds: " + finalRankUpCost + " required to complete the cycle.");
                return;
            }

            economyManager.withdrawMoney(player, finalRankUpCost);

            double storedBalance = dataManager.getStoredBalance(player);
            double currentBalance = economyManager.getBalance(player);
            double totalBalance = storedBalance + currentBalance;

            IModifier activeModifier = modifierManager.getActiveModifier(player);

            dataManager.recordCycleCompletion(player, true);
            dataManager.setPlayerCycleStatus(player, false);
            dataManager.incrementPlayerCycleCount(player);
            rankManager.setPlayerRank(player, 0);
            economyManager.setBalance(player, totalBalance);
            dataManager.setStoredBalance(player, 0);

            modifierManager.completeModifier(player, activeModifier);
            int completedModifiersCount = dataManager.getCompletedModifierCount(player);

            // Reset player's group to the initial entry group (e.g., "citizen")
            permissionManager.resetToDefaultGroup(player);
            permissionManager.addCompletionPrefix(player);
            player.setHealth(0.0);
            VaultUtil.ensureVaultCleared(player.getUniqueId().toString(), 3);

            BroadcastUtil.broadcastMessage("§c" + player.getName() + " completed the cycle with the " + activeModifier.getName() + " modifier");

        } catch (SQLException e) {
            logSevere("Error completing cycle: " + e.getMessage());
            MessageUtil.sendPrefixMessage(player, "&cerror occurred while completing the cycle. Please try again later.");
        }
    }

    public void quitCycle(Player player) {
        try {
            if (!dataManager.isPlayerInCycle(player)) {
                MessageUtil.sendPrefixMessage(player, "&cInvalid: Not currently in a cycle.");
                ConsoleUtil.sendError("Player " + player.getName() + " reached quitCycle method without being in a cycle. Review player's data and permission setup!");
                return;
            }

            int currentRank = rankManager.getPlayerRank(player);

            double storedBalance = dataManager.getStoredBalance(player);
            double currentBalance = economyManager.getBalance(player);
            double totalBalance = storedBalance + currentBalance;

            IModifier activeModifier = modifierManager.getActiveModifier(player);

            dataManager.recordCycleCompletion(player, false);
            dataManager.setPlayerCycleStatus(player, false);
            rankManager.setPlayerRank(player, 0);
            economyManager.setBalance(player, totalBalance);
            dataManager.setStoredBalance(player, 0);

            modifierManager.removeModifier(player);
            int completedModifiersCount = dataManager.getCompletedModifierCount(player);

            // Reset player's group to the initial entry group (e.g., "citizen")
            permissionManager.resetToDefaultGroup(player);
            if (completedModifiersCount > 0) {
                permissionManager.addCompletionPrefix(player);
            }
            player.setHealth(0.0);
            VaultUtil.ensureVaultCleared(player.getUniqueId().toString(), 3);

            BroadcastUtil.broadcastMessage("§c" + player.getName() + " has been discharged as a result of their inability to overcome the " + activeModifier.getName() + " modifier");

        } catch (SQLException e) {
            logSevere("Error completing cycle: " + e.getMessage());
            MessageUtil.sendPrefixMessage(player, "&cerror occurred while completing the cycle. Please try again later.");
        }
    }

    public boolean isPlayerInCycle(Player player) {
        try {
            return dataManager.isPlayerInCycle(player);
        } catch (SQLException e) {
            logSevere("Error checking if player is in cycle: " + e.getMessage());
            return false;
        }
    }

    private void log(String message) {
        cycleModule.getPlugin().getLogger().info(message);
    }

    private void logSevere(String message) {
        cycleModule.getPlugin().getLogger().severe(message);
    }
}