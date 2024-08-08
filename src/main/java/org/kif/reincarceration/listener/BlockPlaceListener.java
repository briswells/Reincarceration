package org.kif.reincarceration.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.block.Block;
import me.catalysmrl.catamines.api.events.CataMineBlockBreakEvent;
import me.catalysmrl.catamines.api.events.CataMineBlockPlaceEvent;
import org.kif.reincarceration.util.BlockTrackUtil;

public class BlockPlaceListener
{
    @EventHandler
    public void onCataMineBlockBreak(CataMineBlockBreakEvent event) {
        // Handle the event

        System.out.println("CataMineBlockBreakEvent triggered!");
        // Add your custom logic here
    }
    @EventHandler
    public void onCataMineBlockPlace(CataMineBlockPlaceEvent event) {
        // Handle the event
        BlockTrackUtil.addPlacedBlock(event.getCataMineRegion(), event.getBlockPlaceEvent().getBlock().getLocation());
        System.out.println("CataMineBlockPlaceEvent triggered!");
        // Add your custom logic here
    }

}
