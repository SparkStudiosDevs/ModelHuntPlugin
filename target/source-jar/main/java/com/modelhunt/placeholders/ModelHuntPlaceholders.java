package com.modelhunt.placeholders;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.data.PlayerDataManager;
import com.modelhunt.models.HuntLocation;
import com.modelhunt.models.HuntSession;
import com.modelhunt.utils.MessageUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ModelHuntPlaceholders extends PlaceholderExpansion {
    private final ModelHuntPlugin plugin;

    public ModelHuntPlaceholders(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "modelhunt";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        PlayerDataManager.PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // Basic statistics
        switch (params.toLowerCase()) {
            case "total_hunts":
                return String.valueOf(playerData.getTotalHuntsCompleted());
                
            case "total_rewards":
                return String.valueOf(playerData.getTotalRewardsReceived());
                
            case "first_hunt_time":
                long firstHunt = playerData.getFirstHuntTime();
                return firstHunt > 0 ? MessageUtils.formatTime((System.currentTimeMillis() - firstHunt) / 1000) : "Never";
                
            case "last_hunt_time":
                long lastHunt = playerData.getLastHuntTime();
                return lastHunt > 0 ? MessageUtils.formatTime((System.currentTimeMillis() - lastHunt) / 1000) + " ago" : "Never";
                
            case "session_clicks":
                if (player.isOnline()) {
                    HuntSession session = plugin.getHuntManager().getOrCreateSession((Player) player);
                    return String.valueOf(session.getTotalClicks());
                }
                return "0";
                
            case "active_locations":
                long activeCount = plugin.getConfigManager().getHuntLocations().values()
                        .stream().filter(HuntLocation::isActive).count();
                return String.valueOf(activeCount);
                
            case "total_locations":
                return String.valueOf(plugin.getConfigManager().getHuntLocations().size());
                
            case "spawned_models":
                return String.valueOf(plugin.getHuntManager().getSpawnedEntities().size());
        }

        // Hunt-specific completions: %modelhunt_hunt_<location_id>%
        if (params.startsWith("hunt_")) {
            String locationId = params.substring(5);
            return String.valueOf(playerData.getHuntCompletions(locationId));
        }

        // Cooldown for specific hunt: %modelhunt_cooldown_<location_id>%
        if (params.startsWith("cooldown_")) {
            if (player.isOnline()) {
                String locationId = params.substring(9);
                HuntSession session = plugin.getHuntManager().getOrCreateSession((Player) player);
                int cooldown = plugin.getConfigManager().getCooldownSeconds();
                long timeLeft = session.getTimeUntilNextClick(locationId, cooldown);
                return timeLeft > 0 ? MessageUtils.formatTime(timeLeft) : "Ready";
            }
            return "Unknown";
        }

        // Last completion time for specific hunt: %modelhunt_last_<location_id>%
        if (params.startsWith("last_")) {
            String locationId = params.substring(5);
            long lastCompletion = playerData.getLastHuntCompletion(locationId);
            return lastCompletion > 0 ? MessageUtils.formatTime((System.currentTimeMillis() - lastCompletion) / 1000) + " ago" : "Never";
        }

        return null;
    }
}