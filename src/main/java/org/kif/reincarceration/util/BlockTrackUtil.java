package org.kif.reincarceration.util;

import org.bukkit.Location;
import me.catalysmrl.catamines.mine.components.region.CataMineRegion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockTrackUtil {
    private static final Map<CataMineRegion, Set<Location>> regionBlockCoordinates = new HashMap<>();

    public static void addPlacedBlock(CataMineRegion region, Location location) {
        regionBlockCoordinates
                .computeIfAbsent(region, k -> new HashSet<>())
                .add(location);
    }

    public static boolean isBlockPlaced(CataMineRegion region, Location location) {
        return regionBlockCoordinates.containsKey(region) && regionBlockCoordinates.get(region).contains(location);
    }

    public static void removePlacedBlock(CataMineRegion region, Location location) {
        if (regionBlockCoordinates.containsKey(region)) {
            regionBlockCoordinates.get(region).remove(location);
            if (regionBlockCoordinates.get(region).isEmpty()) {
                regionBlockCoordinates.remove(region);
            }
        }
    }

    public static void clearPlacedBlocks(CataMineRegion region) {
        regionBlockCoordinates.remove(region);
    }
}