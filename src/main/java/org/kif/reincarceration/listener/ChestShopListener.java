package org.kif.reincarceration.listener;

import com.Acrobot.ChestShop.Events.Economy.CurrencyTransferEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kif.reincarceration.Reincarceration;
import org.kif.reincarceration.data.DataManager;
import org.kif.reincarceration.data.DataModule;
import org.kif.reincarceration.permission.PermissionManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

public class ChestShopListener implements Listener {
    private final Reincarceration plugin;
    private final PermissionManager permissionManager;
    private final DataManager dataManager;

    public ChestShopListener(
            final Reincarceration plugin,
            final DataModule dataModule
    ) {
        this.plugin = plugin;
        this.permissionManager = new PermissionManager(plugin);
        this.dataManager = new DataManager(dataModule);
    }

    @EventHandler
    public void onCurrencyTransfer(final CurrencyTransferEvent event) {
        final UUID seller = event.getReceiver();
        boolean isAssociated = permissionManager.isAssociatedWithBaseGroup(seller, seller.toString());

        if (!isAssociated) {
            return;
        }

        final BigDecimal amountGoingToOwner = event.getAmountReceived();
        final BigDecimal storedBalance;
        try {
            storedBalance = dataManager.getStoredBalance(seller);

            // Money should not be given to those in reincarceration - store it for later.
            event.setAmountReceived(BigDecimal.ZERO);
            final BigDecimal newBalance = storedBalance.add(amountGoingToOwner);
            dataManager.setStoredBalance(seller, newBalance);
        } catch (SQLException e) {
            event.setHandled(false);
            plugin.getLogger().severe("Error in CurrencyTransferEvent: " + e.getMessage());
        }
    }
}
