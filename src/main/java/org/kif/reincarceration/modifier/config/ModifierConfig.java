package org.kif.reincarceration.modifier.config;

import org.bukkit.configuration.ConfigurationSection;
import org.kif.reincarceration.Reincarceration;

import java.util.HashMap;
import java.util.Map;

public class ModifierConfig {
    private final Reincarceration plugin;
    private final Map<String, ConfigurationSection> modifierConfigs;

    public ModifierConfig(Reincarceration plugin) {
        this.plugin = plugin;
        this.modifierConfigs = new HashMap<>();
        loadModifierConfigs();
    }

    private void loadModifierConfigs() {
        ConfigurationSection modifiersSection = plugin.getConfig().getConfigurationSection("modifiers");
        if (modifiersSection != null) {
            for (String key : modifiersSection.getKeys(false)) {
                modifierConfigs.put(key, modifiersSection.getConfigurationSection(key));
            }
        }
    }

    public ConfigurationSection getModifierConfig(String modifierId) {
        return modifierConfigs.get(modifierId);
    }

    public boolean isModifierEnabled(String modifierId) {
        ConfigurationSection config = getModifierConfig(modifierId);
        return config != null && config.getBoolean("enabled", true);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        modifierConfigs.clear();
        loadModifierConfigs();
    }

    // Add more methods as needed for specific modifier configurations
}