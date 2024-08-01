package org.kif.reincarceration.rewards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kif.reincarceration.modifier.core.IModifier;
import org.kif.reincarceration.util.ConsoleUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RewardManager {
    private final RewardModule rewardModule;
    private final ConcurrentHashMap<UUID, IModifier> playersNeedingRewards;

    public RewardManager(RewardModule module) {
        this.rewardModule = module;
        playersNeedingRewards = new ConcurrentHashMap<>();
    }

    public void setPlayerNeedsReward(@NotNull final Player player, @NotNull final IModifier modifier) {
        playersNeedingRewards.put(player.getUniqueId(), modifier);
    }

    public void rewardPlayer(@NotNull final Player player) {
        final IModifier modifier = playersNeedingRewards.get(player.getUniqueId());

    }

    public void rewardPlayer(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            ConsoleUtil.sendError("Tried to reward player but they don't exist!");
            ConsoleUtil.sendError("UUID: " + uuid);
            ConsoleUtil.sendError("Modifier completed: " +
                    playersNeedingRewards.get(uuid).getName()
            );
            return;
        }

        rewardPlayer(player);
    }
}
