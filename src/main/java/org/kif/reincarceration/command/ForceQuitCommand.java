package org.kif.reincarceration.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kif.reincarceration.cycle.CycleManager;
import org.kif.reincarceration.cycle.CycleModule;
import org.kif.reincarceration.util.MessageUtil;

public class ForceQuitCommand implements CommandExecutor {
    private final CycleManager cycleManager;

    public ForceQuitCommand(CycleModule cycleModule) {
        this.cycleManager = cycleModule.getCycleManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length != 1) {
            MessageUtil.sendPrefixMessage((Player) sender, "&cUsage: /forcequit <player>");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            MessageUtil.sendPrefixMessage((Player) sender, "&cPlayer not found or not online.");
            return true;
        }

        if (!cycleManager.isPlayerInCycle(targetPlayer)) {
            MessageUtil.sendPrefixMessage((Player) sender, "&cThe specified player is not currently in a cycle.");
            return true;
        }

        cycleManager.quitCycle(targetPlayer);
        MessageUtil.sendPrefixMessage((Player) sender, "&aYou have forcefully removed " + targetPlayer.getName() + " from their cycle.");
        MessageUtil.sendPrefixMessage(targetPlayer, "&cAn admin has forcefully removed you from your cycle.");

        return true;
    }
}