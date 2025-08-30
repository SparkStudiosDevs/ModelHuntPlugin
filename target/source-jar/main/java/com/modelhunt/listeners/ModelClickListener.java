package com.modelhunt.listeners;

import com.modelhunt.ModelHuntPlugin;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ModelClickListener implements Listener {
    private final ModelHuntPlugin plugin;

    public ModelClickListener(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        
        // Check if the entity has a ModelEngine model
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entity.getUniqueId());
        if (modeledEntity == null) {
            return;
        }
        
        // Let HuntManager handle the click
        boolean handled = plugin.getHuntManager().handleModelClick(player, entity, player.getLocation());
        
        if (handled) {
            event.setCancelled(true); // Prevent default interaction
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up player session when they leave
        plugin.getPlayerDataManager().savePlayerData(event.getPlayer().getUniqueId());
        plugin.getPlayerDataManager().unloadPlayerData(event.getPlayer().getUniqueId());
        plugin.getHuntManager().removeSession(event.getPlayer().getUniqueId());
    }
}