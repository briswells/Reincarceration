package org.kif.reincarceration.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.config.ConfigManager;

public class MessageUtil {

    private static Reincarceration plugin;
    private static ConfigManager configManager;

    public static void initialize(Reincarceration plugin) {
        MessageUtil.plugin = plugin;
        MessageUtil.configManager = plugin.getModuleManager().getConfigManager();
    }

    public static void sendMessage(Player player, String message) {
        String formattedMessage = formatMessage(message, false);
        player.sendMessage(formattedMessage);
    }

    public static void sendPrefixMessage(Player player, String message) {
        String formattedMessage = formatMessage(message, true);
        player.sendMessage(formattedMessage);
    }

    private static String formatMessage(String message, Boolean prefix_toggle) {
        if (prefix_toggle) {
            String prefix = configManager.getPrefix();
            return ChatColor.translateAlternateColorCodes('&', prefix + message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }
}