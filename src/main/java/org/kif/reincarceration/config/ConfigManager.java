package org.kif.reincarceration.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.kif.reincarceration.Reincarceration;

import java.util.List;
import java.util.Objects;

public class ConfigManager {
    private final Reincarceration plugin;
    private final FileConfiguration config;

    public ConfigManager(Reincarceration plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public double getEntryFee() {
        return config.getDouble("economy.entry-fee", 1000);
    }

    public List<Integer> getRankUpCosts() {
        return config.getIntegerList("economy.rank-up-costs");
    }

    public double getRankUpCost(int rank) {
        List<Integer> costs = getRankUpCosts();
        return rank < costs.size() ? costs.get(rank) : Double.MAX_VALUE;
    }

    public String getRankName(int rank) {
        return config.getString("ranks." + rank + ".name", "Rank " + rank);
    }

    public String getRankPermissionGroup(int rank) {
        return config.getString("ranks." + rank + ".permission-group", "reoffender_rank_" + rank);
    }

    public String getDatabaseFilename() {
        return config.getString("database.filename", "reincarceration_data.db");
    }

    public String getEntryGroup() {
        return config.getString("permissions.entry-group", "citizen");
    }

    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&8[&6Reincarceration&8] &r"));
    }

    public boolean isDebugMode() {
        return config.getBoolean("debug-mode", false);
    }

    public FileConfiguration getRawConfig() {
        return config;
    }

    public int getMaxRank() {
        return Objects.requireNonNull(config.getConfigurationSection("ranks")).getKeys(false).size() - 1;
    }

    public boolean isMaxRank(int rank) {
        return rank == getMaxRank();
    }
}