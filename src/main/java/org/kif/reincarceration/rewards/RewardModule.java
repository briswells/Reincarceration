package org.kif.reincarceration.rewards;

import lombok.Getter;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.util.ConsoleUtil;

import java.sql.SQLException;

public class RewardModule implements Module {
    private final Reincarceration plugin;
    @Getter
    private RewardManager rewardManager;

    public RewardModule(final Reincarceration plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() throws SQLException {
        this.rewardManager = new RewardManager(plugin);
        ConsoleUtil.sendSuccess("Reward Module enabled");
    }

    @Override
    public void onDisable() {
        ConsoleUtil.sendSuccess("Reward Module disabled");
    }
}
