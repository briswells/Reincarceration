package org.kif.reincarceration.rewards;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CycleReward {
    final List<CycleItem> items;
    final BigDecimal money;
    final List<String> commands;
}
