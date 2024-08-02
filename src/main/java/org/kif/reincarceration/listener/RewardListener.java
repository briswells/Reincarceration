package org.kif.reincarceration.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.rewards.RewardManager;
import org.kif.reincarceration.rewards.RewardModule;
import org.kif.reincarceration.util.MessageUtil;

public class RewardListener implements Listener {
    private final RewardManager rewardManager;

    public RewardListener(Reincarceration plugin) {
        this.rewardManager = plugin.getModuleManager().getModule(RewardModule.class).getRewardManager();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!rewardManager.isPlayerNeededReward(event.getPlayer())) {
            return;
        }

        rewardManager.rewardPlayer(event.getPlayer());
        MessageUtil.sendPrefixMessage(event.getPlayer(), "Congratulations! Here are your rewards.");
    }
}
