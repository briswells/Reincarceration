package org.kif.reincarceration.listener;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.permission.PermissionManager;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.util.MessageUtil;
import org.kif.reincarceration.util.ConsoleUtil;

import java.sql.SQLException;
import java.util.UUID;

public class PayCommandListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;
    private final DataManager dataManager;

    public PayCommandListener(Reincarceration plugin) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
        DataModule dataModule = plugin.getModuleManager().getModule(DataModule.class);
        if (dataModule == null) {
            throw new IllegalStateException("DataModule is not initialized");
        }
        this.dataManager = dataModule.getDataManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().toLowerCase().split("\\s+");
        if (args.length >= 2 && args[0].equals("/pay")) {
            Player sender = event.getPlayer();
            String recipientName = args[1];

            boolean senderAssociated = isAssociatedPlayer(sender);
            boolean recipientAssociated = false;

            try {
                recipientAssociated = isAssociatedOfflinePlayer(recipientName);
            } catch (SQLException e) {
                ConsoleUtil.sendError("Error checking player association status: " + e.getMessage());
                event.setCancelled(true);
                MessageUtil.sendPrefixMessage(sender, "&cAn error occurred while processing the command. Please try again later.");
                return;
            }

            if (senderAssociated || recipientAssociated) {
                event.setCancelled(true);

                if (senderAssociated) {
                    MessageUtil.sendPrefixMessage(sender, "&cYou cannot use this command.");
                } else {
                    MessageUtil.sendPrefixMessage(sender, "&cYou cannot send money to this player.");
                }

                ConsoleUtil.sendDebug("Cancelled /pay command:");
                ConsoleUtil.sendDebug("Sender: " + sender.getName() + " (Associated: " + senderAssociated + ")");
                ConsoleUtil.sendDebug("Recipient: " + recipientName + " (Associated: " + recipientAssociated + ")");
            }
        }
    }

    private boolean isAssociatedPlayer(Player player) {
        return permissionManager.isAssociatedWithBaseGroup(player);
    }

    private boolean isAssociatedOfflinePlayer(String playerName) throws SQLException {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if (offlinePlayer.hasPlayedBefore()) {
            return isAssociatedOfflinePlayerByUUID(offlinePlayer.getUniqueId());
        }
        return false;
    }

    private boolean isAssociatedOfflinePlayerByUUID(UUID playerUUID) throws SQLException {
        // Using DataManager to check if the player is in a cycle
        return dataManager.isPlayerInCycleUUID(playerUUID);
    }
}