package com.modelhunt.listeners;

import com.modelhunt.ModelHuntPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LocationSetListener implements Listener {
    private final ModelHuntPlugin plugin;

    public LocationSetListener(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() != Material.BLAZE_ROD) return;
        
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && 
            item.getItemMeta().getDisplayName().equals("§6§lHunt Location Setter")) {
            
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                
                // Set location
                plugin.getLocationCreationManager().setLocation(player, player.getLocation());
                
                // Remove the tool
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().remove(item);
                }
                
                player.sendMessage("§a[Hunt] §7Location set! Return to the GUI to continue setup.");
            }
        }
    }
}