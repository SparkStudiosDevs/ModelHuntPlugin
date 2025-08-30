package com.modelhunt.data;

import com.modelhunt.ModelHuntPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataManager {
    private final ModelHuntPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();

    public PlayerDataManager(ModelHuntPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        
    public PlayerData getPlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data == null) {
            data = loadPlayerData(playerId);
            playerDataCache.put(playerId, data);
        }
        return data;
    }

    public void savePlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data != null) {
            savePlayerDataToFile(playerId, data);
        }
    }

    public void saveAllPlayerData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            savePlayerDataToFile(entry.getKey(), entry.getValue());
        }
    }

    private PlayerData loadPlayerData(UUID playerId) {
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        
        if (!playerFile.exists()) {
            return new PlayerData();
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            PlayerData data = new PlayerData();
            
            data.setTotalHuntsCompleted(config.getInt("total-hunts-completed", 0));
            data.setTotalRewardsReceived(config.getInt("total-rewards-received", 0));
            data.setFirstHuntTime(config.getLong("first-hunt-time", 0));
            data.setLastHuntTime(config.getLong("last-hunt-time", 0));
            
            // Load hunt-specific data
            if (config.contains("hunt-data")) {
                for (String huntId : config.getConfigurationSection("hunt-data").getKeys(false)) {
                    int completions = config.getInt("hunt-data." + huntId + ".completions", 0);
                    long lastCompletion = config.getLong("hunt-data." + huntId + ".last-completion", 0);
                    data.setHuntCompletions(huntId, completions);
                    data.setLastHuntCompletion(huntId, lastCompletion);
                }
            }
            
            return data;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player data for " + playerId, e);
            return new PlayerData();
        }
    }

    private void savePlayerDataToFile(UUID playerId, PlayerData data) {
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        
        try {
            FileConfiguration config = new YamlConfiguration();
            
            config.set("total-hunts-completed", data.getTotalHuntsCompleted());
            config.set("total-rewards-received", data.getTotalRewardsReceived());
            config.set("first-hunt-time", data.getFirstHuntTime());
            config.set("last-hunt-time", data.getLastHuntTime());
            
            // Save hunt-specific data
            for (Map.Entry<String, Integer> entry : data.getHuntCompletions().entrySet()) {
                String huntId = entry.getKey();
                config.set("hunt-data." + huntId + ".completions", entry.getValue());
                config.set("hunt-data." + huntId + ".last-completion", data.getLastHuntCompletion(huntId));
            }
            
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + playerId, e);
        }
    }

    public void unloadPlayerData(UUID playerId) {
        savePlayerData(playerId);
        playerDataCache.remove(playerId);
    }

    // Player data class
    public static class PlayerData {
        private int totalHuntsCompleted = 0;
        private int totalRewardsReceived = 0;
        private long firstHuntTime = 0;
        private long lastHuntTime = 0;
        private final Map<String, Integer> huntCompletions = new HashMap<>();
        private final Map<String, Long> lastHuntCompletions = new HashMap<>();

        public int getTotalHuntsCompleted() { return totalHuntsCompleted; }
        public void setTotalHuntsCompleted(int total) { this.totalHuntsCompleted = total; }
        public void incrementTotalHunts() { this.totalHuntsCompleted++; }

        public int getTotalRewardsReceived() { return totalRewardsReceived; }
        public void setTotalRewardsReceived(int total) { this.totalRewardsReceived = total; }
        public void incrementTotalRewards() { this.totalRewardsReceived++; }

        public long getFirstHuntTime() { return firstHuntTime; }
        public void setFirstHuntTime(long time) { 
            if (this.firstHuntTime == 0) {
                this.firstHuntTime = time; 
            }
        }

        public long getLastHuntTime() { return lastHuntTime; }
        public void setLastHuntTime(long time) { this.lastHuntTime = time; }

        public Map<String, Integer> getHuntCompletions() { return new HashMap<>(huntCompletions); }
        public int getHuntCompletions(String huntId) { return huntCompletions.getOrDefault(huntId, 0); }
        public void setHuntCompletions(String huntId, int completions) { huntCompletions.put(huntId, completions); }
        public void incrementHuntCompletions(String huntId) { 
            huntCompletions.put(huntId, huntCompletions.getOrDefault(huntId, 0) + 1); 
        }

        public long getLastHuntCompletion(String huntId) { return lastHuntCompletions.getOrDefault(huntId, 0L); }
        public void setLastHuntCompletion(String huntId, long time) { lastHuntCompletions.put(huntId, time); }
    }
}