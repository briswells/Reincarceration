package org.kif.reincarceration.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.kif.reincarceration.util.ConsoleUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EconomyManager {
    private final EconomyModule economyModule;

    public EconomyManager(EconomyModule economyModule) {
        this.economyModule = economyModule;
    }

    private Economy getEconomy() {
        Economy economy = economyModule.getEconomy();
        if (economy == null) {
            throw new IllegalStateException("Economy is not available");
        }
        return economy;
    }

    public boolean hasEnoughBalance(Player player, BigDecimal amount) {
        ConsoleUtil.sendDebug("Checking balance for " + player.getName() + ": has " + amount + "?");
        try {
            boolean hasBalance = getEconomy().has(player, amount.doubleValue());
            ConsoleUtil.sendDebug(String.format("Checked balance for %s: has %.2f? %s",
                    player.getName(), amount, hasBalance));
            return hasBalance;
        } catch (IllegalStateException e) {
            logSevere("Failed to check balance: " + e.getMessage());
            return false;
        }
    }

    public boolean withdrawMoney(Player player, BigDecimal amount) {
        ConsoleUtil.sendDebug("Withdrawing " + amount + " from " + player.getName());
        try {
            EconomyResponse response = getEconomy().withdrawPlayer(player, amount.doubleValue());
            if (response.transactionSuccess()) {
                ConsoleUtil.sendDebug(String.format("Withdrew %.2f from %s. New balance: %.2f",
                        amount, player.getName(), response.balance));
                return true;
            } else {
                ConsoleUtil.sendError(String.format("Failed to withdraw %.2f from %s: %s",
                        amount, player.getName(), response.errorMessage));
                return false;
            }
        } catch (IllegalStateException e) {
            logSevere("Failed to withdraw money: " + e.getMessage());
            return false;
        }
    }

    public void depositMoney(OfflinePlayer player, BigDecimal amount) {
        ConsoleUtil.sendDebug("Depositing " + amount + " to " + player.getName());
        try {
            EconomyResponse response = getEconomy().depositPlayer(player, amount.doubleValue());
            if (response.transactionSuccess()) {
                ConsoleUtil.sendDebug(String.format("Deposited %.2f to %s. New balance: %.2f",
                        amount, player.getName(), response.balance));
            } else {
                ConsoleUtil.sendError(String.format("Failed to deposit %.2f to %s: %s",
                        amount, player.getName(), response.errorMessage));
            }
        } catch (IllegalStateException e) {
            logSevere("Failed to deposit money: " + e.getMessage());
        }
    }

    public BigDecimal getBalance(Player player) {
        ConsoleUtil.sendDebug("Retrieving balance for " + player.getName());
        try {
            double balance = getEconomy().getBalance(player);
            ConsoleUtil.sendDebug(String.format("Retrieved balance for %s: %.2f",
                    player.getName(), balance));
            return BigDecimal.valueOf(balance).setScale(2, RoundingMode.CEILING);
        } catch (IllegalStateException e) {
            logSevere("Failed to get balance: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    public void setBalance(Player player, BigDecimal amount) {
        ConsoleUtil.sendDebug("Setting balance for " + player.getName() + " to " + amount);
        try {
            BigDecimal currentBalance = BigDecimal.valueOf(getEconomy().getBalance(player));
            if (currentBalance.compareTo(amount) > 0) {
                withdrawMoney(player, currentBalance.subtract(amount));
            } else if (currentBalance.compareTo(amount) < 0) {
                depositMoney(player, amount.subtract(currentBalance));
            }
            ConsoleUtil.sendDebug(String.format("Set balance for %s to %.2f",
                    player.getName(), amount));
        } catch (IllegalStateException e) {
            logSevere("Failed to set balance: " + e.getMessage());
        }
    }

    private void logSevere(String message) {
        economyModule.getPlugin().getLogger().severe(message);
    }
}