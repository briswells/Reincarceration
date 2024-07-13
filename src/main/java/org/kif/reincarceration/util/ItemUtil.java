package org.kif.reincarceration.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.kif.reincarceration.Reincarceration;

public class ItemUtil {

    private static final String REINCARCERATION_FLAG_KEY = "reincarFlag";
    private static final String REINCARCERATION_OVERRIDE_FLAG_KEY = "reincarOverrideFlag";
    private static NamespacedKey reincarcerationKey;

    public static void initialize(Reincarceration plugin) {
        reincarcerationKey = new NamespacedKey(plugin, REINCARCERATION_FLAG_KEY);
    }

    public static void addReincarcerationFlag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(reincarcerationKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
    }

    public static boolean hasReincarcerationFlag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Byte value = container.get(reincarcerationKey, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    public static void removeReincarcerationFlag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(reincarcerationKey);
        item.setItemMeta(meta);
    }
}