package org.kif.reincarceration.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.config.ConfigManager;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.rewards.CycleReward;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RewardUtil {
    public static CycleReward getCycleRewardForModifier(
            IModifier modifier,
            Reincarceration plugin
    ) {
        final ConfigurationSection modifierConfig = plugin.getConfig().getConfigurationSection("modifiers." + modifier.getId());
        if (modifierConfig == null) {
            ConsoleUtil.sendError("Error parsing rewards: ");
            ConsoleUtil.sendError("Modifier " + modifier.getName() + "does not have a matching config block for it's id.");
            return null;
        }
        final ConfigurationSection rewardsConfig = modifierConfig.getConfigurationSection("rewards");
        if (rewardsConfig == null) {
            return null;
        }
        final BigDecimal money = BigDecimal.valueOf(rewardsConfig.getDouble("money"));
        final List<String> commands = rewardsConfig.getStringList("commands");

        final ConfigurationSection items = rewardsConfig.getConfigurationSection("items");
        if (items == null) {
            return new CycleReward(
                    Collections.emptyList(),
                    money,
                    commands
            );
        }


        return new CycleReward(
                Collections.emptyList(),
                money,
                commands
        );
    }
}
