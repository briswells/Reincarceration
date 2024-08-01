package org.kif.reincarceration.rewards;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data @Builder
public class CycleItem {
    final String name;
    final Map<String, Integer> enchantments;
    final List<String> lore;
    final Integer amount;
}
