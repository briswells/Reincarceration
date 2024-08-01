package org.kif.reincarceration.rewards;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CycleItem {
    final String name;
    final Map<String, Integer> enchantments;
    final List<String> lore;
}
