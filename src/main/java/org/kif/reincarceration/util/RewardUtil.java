package org.kif.reincarceration.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.rewards.CycleItem;
import org.kif.reincarceration.rewards.CycleReward;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        final List<CycleItem> cycleItems = new ArrayList<>();
        final Set<String> itemNames = items.getKeys(false);
        for (final String itemName : itemNames) {
            CycleItem.CycleItemBuilder builder = CycleItem.builder();
            // getInt defaults to 0 if not found, we want default to 1
            final int amount = items.getInt(itemName + ".amount") == 0 ? 1 : items.getInt(itemName + ".amount");

            builder.name(itemName)
                   .amount(amount)
                   .lore(items.getStringList(itemName + ".lore"));

            final ConfigurationSection enchantmentsPath = items.getConfigurationSection("enchantments");
            if (enchantmentsPath == null) {
                builder.enchantments(Collections.emptyMap());
                cycleItems.add(builder.build());
                continue;
            }

            final HashMap<String, Integer> enchantmentMap = new HashMap<>();
            final Set<String> enchantmentStrings = enchantmentsPath.getKeys(false);
            for (final String enchantment : enchantmentStrings) {
                enchantmentMap
                        .put(
                                enchantment,
                                enchantmentsPath.getInt(enchantment + ".level")
                        );
            }
            builder.enchantments(enchantmentMap);
            cycleItems.add(builder.build());
        }
        return new CycleReward(
                cycleItems,
                money,
                commands
        );
    }


    public static ItemStack buildItemStackFromRewardItem(
            final CycleItem item
    ) {
        final String materialName = item.getName();
        final Material material = Material.getMaterial(materialName);
        if (material == null) {
            ConsoleUtil.sendError("CycleItem reward: " + item.getName() + " has incorrect name. Returning null itemstack.");
            return null;
        }

        final ItemStack itemStack = new ItemStack(material);
        itemStack.setAmount(item.getAmount());

        final List<Component> lore = new ArrayList<>();
        for (final String loreString : item.getLore()) {
            lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(loreString));
        }
        itemStack.lore(lore);

        item.getEnchantments().forEach( (key, value) -> {
            final Enchantment enchantment = Enchantment.getByKey(new NamespacedKey("minecraft", key));
            if (enchantment == null) {
                ConsoleUtil.sendError("Enchantment value: " + key + " does not have a matching minecraft namespace key. Skipping.");
                return;
            }
            itemStack.addUnsafeEnchantment(enchantment, value);
        });

        return itemStack;
    }
}
