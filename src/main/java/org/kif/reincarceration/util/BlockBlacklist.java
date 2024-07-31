package org.kif.reincarceration.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.kif.reincarceration.Reincarceration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockBlacklist {
    private static final Set<Material> blacklistedBlocks = new HashSet<>();
    private static Reincarceration plugin;

    public static void initialize(Reincarceration plugin) {
        BlockBlacklist.plugin = plugin;
        loadBlacklist();
    }

    public static void loadBlacklist() {
        blacklistedBlocks.clear();
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("block_blacklist");
        if (config != null) {
            List<String> blacklist = config.getStringList("blocks");
            for (String block : blacklist) {
                try {
                    Material material = Material.valueOf(block.toUpperCase());
                    blacklistedBlocks.add(material);
                } catch (IllegalArgumentException e) {
                    ConsoleUtil.sendError("Invalid material in block blacklist: " + block);
                }
            }
        }
        ConsoleUtil.sendDebug("Loaded " + blacklistedBlocks.size() + " blacklisted blocks");
    }

    public static boolean isBlacklisted(Material material) {
        return blacklistedBlocks.contains(material);
    }

    public static Set<Material> getBlacklistedBlocks() {
        return new HashSet<>(blacklistedBlocks);
    }

    public static void reloadBlacklist() {
        loadBlacklist();
    }
}