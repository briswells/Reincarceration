package org.kif.reincarceration.rewards;

import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.core.Module;
import org.kif.reincarceration.util.ConsoleUtil;

import java.sql.SQLException;

public class RewardModule implements Module {
    private RewardManager rewardManager;

    @Override
    public void onEnable() throws SQLException {
        this.rewardManager = new RewardManager(this);
        ConsoleUtil.sendSuccess("Reward Module enabled");
    }

    @Override
    public void onDisable() {
        ConsoleUtil.sendSuccess("Reward Module disabled");
    }
}
