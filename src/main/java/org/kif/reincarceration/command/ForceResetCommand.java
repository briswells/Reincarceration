package org.kif.reincarceration.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kif.reincarceration.cycle.CycleManager;
import org.kif.reincarceration.cycle.CycleModule;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.util.ConsoleUtil;

public class ForceResetCommand implements CommandExecutor {
    private final CycleManager cycleManager;
    private final DataManager dataManager;

    public ForceResetCommand(CycleModule cycleModule, DataModule dataModule) {
        this.cycleManager = cycleModule.getCycleManager();
        this.dataManager = dataModule.getDataManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 1) {
            MessageUtil.sendPrefixMessage((Player) sender, "&cUsage: /forcereset <player>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            MessageUtil.sendPrefixMessage((Player) sender, "&cPlayer not found or not online.");
            return true;
        }

        try {
            ConsoleUtil.sendDebug("Attempting to force reset player: " + targetPlayer.getName() + " (UUID: " + targetPlayer.getUniqueId() + ")");

            if (cycleManager.isPlayerInCycle(targetPlayer)) {
                ConsoleUtil.sendDebug("Player is in cycle. Quitting cycle for: " + targetPlayer.getName());
                cycleManager.quitCycle(targetPlayer);
            } else {
                ConsoleUtil.sendDebug("Player is not in a cycle: " + targetPlayer.getName());
            }

            ConsoleUtil.sendDebug("Clearing player data for: " + targetPlayer.getName());
            dataManager.clearPlayerData(targetPlayer);
            ConsoleUtil.sendDebug("Reinitializing player data for: " + targetPlayer.getName());
            dataManager.createPlayerData(targetPlayer);

            MessageUtil.sendPrefixMessage((Player) sender, "&aYou have forcefully reset " + targetPlayer.getName() + "'s data and removed them from the reincarceration system.");
            MessageUtil.sendPrefixMessage(targetPlayer, "&cAn admin has forcefully reset your reincarceration data.");
            ConsoleUtil.sendInfo("Admin " + sender.getName() + " has forcefully reset " + targetPlayer.getName() + "'s reincarceration data.");

        } catch (Exception e) {
            MessageUtil.sendPrefixMessage((Player) sender, "&cAn error occurred while resetting the player's data.");
            ConsoleUtil.sendError("Error in ForceResetCommand for player " + targetPlayer.getName() + " (UUID: " + targetPlayer.getUniqueId() + "): " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}