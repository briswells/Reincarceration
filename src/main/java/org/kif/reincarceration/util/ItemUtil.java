package org.kif.reincarceration.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.kif.reincarceration.Reincarceration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemUtil {

    private static final String REINCARCERATION_FLAG_KEY = "reincarcerationFlag";
    private static NamespacedKey reincarcerationKey;
    private static Reincarceration plugin;
    private static boolean useWhitelist;
    private static Set<Material> itemList;

    public static void initialize(Reincarceration plugin) {
        ItemUtil.plugin = plugin;
        reincarcerationKey = new NamespacedKey(plugin, REINCARCERATION_FLAG_KEY);
        loadConfig();
        ConsoleUtil.sendDebug("ItemUtil initialized with key: " + reincarcerationKey.toString());
    }

    private static void loadConfig() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("item_flagging");
        if (config == null) {
            ConsoleUtil.sendError("Item flagging configuration not found. Using default values.");
            useWhitelist = false;
            itemList = new HashSet<>();
            return;
        }

        useWhitelist = config.getBoolean("use_whitelist", false);
        List<String> items = useWhitelist ? config.getStringList("whitelist") : config.getStringList("blacklist");
        itemList = new HashSet<>();

        for (String item : items) {
            try {
                Material material = Material.valueOf(item.toUpperCase());
                itemList.add(material);
            } catch (IllegalArgumentException e) {
                ConsoleUtil.sendError("Invalid material in item list: " + item);
            }
        }

        ConsoleUtil.sendDebug("Item flagging config loaded. Using " + (useWhitelist ? "whitelist" : "blacklist") +
                " with " + itemList.size() + " items.");
    }

    public static void addReincarcerationFlag(ItemStack item) {
        if (item == null) {
            ConsoleUtil.sendDebug("Cannot add flag: Item is null");
            return;
        }

        if (!canFlagItem(item.getType())) {
            ConsoleUtil.sendDebug("Cannot add flag: Item " + item.getType().name() + " is " +
                    (useWhitelist ? "not in whitelist" : "in blacklist"));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            ConsoleUtil.sendDebug("ItemMeta is null for " + item.getType().name() + ", cannot add flag");
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(reincarcerationKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        ConsoleUtil.sendDebug("Flag added to item: " + item.getType().name() +
                ", Flag present after adding: " + hasReincarcerationFlag(item));
    }

    public static boolean hasReincarcerationFlag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            ConsoleUtil.sendDebug("Cannot check flag: Item is null or has no meta");
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            ConsoleUtil.sendDebug("ItemMeta is null for " + item.getType().name());
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        Byte value = container.get(reincarcerationKey, PersistentDataType.BYTE);
        boolean hasFlag = (value != null && value == (byte) 1);
        ConsoleUtil.sendDebug("Checking flag for item: " + item.getType().name() + ", Has flag: " + hasFlag);
        return hasFlag;
    }

    public static void removeReincarcerationFlag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(reincarcerationKey);
        item.setItemMeta(meta);
    }

    private static boolean canFlagItem(Material material) {
        if (useWhitelist) {
            return itemList.contains(material);
        } else {
            return !itemList.contains(material);
        }
    }

    public static void reloadConfig() {
        loadConfig();
    }
}