package org.kif.reincarceration.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;

public class ReloadTagsCommand implements CommandExecutor {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;

    public ReloadTagsCommand(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(this.plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Must provide a username!");
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player != null) {
            permissionManager.addCompletionPrefix(player);
            return true;
        } else {
            sender.sendMessage("Invalid username!");
        }
        return false;

    }
}
