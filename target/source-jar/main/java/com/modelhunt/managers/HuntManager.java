package com.modelhunt.managers;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.data.PlayerDataManager;
import com.modelhunt.models.HuntLocation;
import com.modelhunt.models.HuntSession;
import com.modelhunt.utils.MessageUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.lumine.mythic.api.MythicAPI;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HuntManager {
    private final ModelHuntPlugin plugin;
    private final Map<UUID, HuntSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, Entity> spawnedEntities = new ConcurrentHashMap<>();
    private final Map<String, Long> respawnTimers = new ConcurrentHashMap<>();

    public HuntManager(ModelHuntPlugin plugin) {
        this.plugin = plugin;
        startRespawnTask();
    }

    public void spawnHuntModels() {
        Map<String, HuntLocation> locations = plugin.getConfigManager().getHuntLocations();
        
        for (Map.Entry<String, HuntLocation> entry : locations.entrySet()) {
            String locationId = entry.getKey();
            HuntLocation huntLocation = entry.getValue();
            
            if (huntLocation.isActive() && !spawnedEntities.containsKey(locationId)) {
                spawnModel(locationId, huntLocation);
            }
        }
    }

    private void spawnModel(String locationId, HuntLocation huntLocation) {
        try {
            // Spawn MythicMob entity first
            ActiveMob mythicMob = MythicBukkit.inst().getMobManager()
                    .spawnMob(huntLocation.getMythicMobType(), huntLocation.getLocation());
            
            if (mythicMob != null) {
                Entity entity = mythicMob.getEntity().getBukkitEntity();
                
                // Apply ModelEngine model to the entity
                ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
                if (modeledEntity != null) {
                    ActiveModel model = ModelEngineAPI.createActiveModel(huntLocation.getModelId());
                    if (model != null) {
                        modeledEntity.addModel(model, true);
                        spawnedEntities.put(locationId, entity);
                        
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().info("Spawned hunt model '" + huntLocation.getModelId() 
                                    + "' at location " + locationId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn model at location " + locationId + ": " + e.getMessage());
        }
    }

    public boolean handleModelClick(Player player, Entity entity, Location clickLocation) {
        // Check if player has permission
        if (!player.hasPermission("hunt.click")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission", 
                    "§cYou don't have permission to participate in hunts!"));
            return false;
        }

        // Find the corresponding hunt location
        String locationId = findLocationForEntity(entity);
        if (locationId == null) {
            return false;
        }

        HuntLocation huntLocation = plugin.getConfigManager().getHuntLocation(locationId);
        if (huntLocation == null || !huntLocation.isActive()) {
            return false;
        }

        // Check distance
        if (clickLocation.distance(huntLocation.getLocation()) > huntLocation.getClickRadius()) {
            return false;
        }

        // Get or create player session
        HuntSession session = getOrCreateSession(player);
        
        // Check cooldown
        int cooldown = plugin.getConfigManager().getCooldownSeconds();
        if (!session.canClick(locationId, cooldown)) {
            long timeLeft = session.getTimeUntilNextClick(locationId, cooldown);
            player.sendMessage(plugin.getConfigManager().getMessage("cooldown", 
                    "§cYou must wait " + timeLeft + " seconds before clicking this model again!"));
            return false;
        }

        // Process the click
        processHuntClick(player, locationId, huntLocation, session);
        return true;
    }

    private void processHuntClick(Player player, String locationId, HuntLocation huntLocation, HuntSession session) {
        // Record the click
        session.recordClick(locationId);
        
        // Update player data
        PlayerDataManager.PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        playerData.incrementTotalHunts();
        playerData.incrementHuntCompletions(locationId);
        playerData.setLastHuntTime(System.currentTimeMillis());
        playerData.setFirstHuntTime(System.currentTimeMillis());
        playerData.setLastHuntCompletion(locationId, System.currentTimeMillis());
        
        // Send click message
        if (huntLocation.getClickMessage() != null && !huntLocation.getClickMessage().isEmpty()) {
            String message = huntLocation.getClickMessage().replace("{player}", player.getName());
            player.sendMessage(MessageUtils.colorizeWithPlaceholders(player, message));
        }
        
        // Give rewards
        plugin.getRewardManager().giveRewards(player, huntLocation.getRewards());
        
        // Execute commands
        executeCommands(player, huntLocation.getCommands());
        
        // Send completion message
        if (huntLocation.getCompletionMessage() != null && !huntLocation.getCompletionMessage().isEmpty()) {
            String message = huntLocation.getCompletionMessage().replace("{player}", player.getName());
            player.sendMessage(MessageUtils.colorizeWithPlaceholders(player, message));
        }
        
        // Remove the entity and start respawn timer
        removeEntity(locationId);
        startRespawnTimer(locationId, huntLocation.getRespawnTimeSeconds());
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Player " + player.getName() + " clicked hunt model at " + locationId);
        }
    }

    private void executeCommands(Player player, List<String> commands) {
        for (String command : commands) {
            String processedCommand = command.replace("{player}", player.getName());
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
                }
            }.runTask(plugin);
        }
    }

    private String findLocationForEntity(Entity entity) {
        for (Map.Entry<String, Entity> entry : spawnedEntities.entrySet()) {
            if (entry.getValue().equals(entity)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void removeEntity(String locationId) {
        Entity entity = spawnedEntities.remove(locationId);
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }

    private void startRespawnTimer(String locationId, int respawnSeconds) {
        long respawnTime = System.currentTimeMillis() + (respawnSeconds * 1000L);
        respawnTimers.put(locationId, respawnTime);
    }

    private void startRespawnTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                List<String> toRespawn = new ArrayList<>();
                
                for (Map.Entry<String, Long> entry : respawnTimers.entrySet()) {
                    if (currentTime >= entry.getValue()) {
                        toRespawn.add(entry.getKey());
                    }
                }
                
                for (String locationId : toRespawn) {
                    respawnTimers.remove(locationId);
                    HuntLocation location = plugin.getConfigManager().getHuntLocation(locationId);
                    if (location != null && location.isActive()) {
                        spawnModel(locationId, location);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }

    public HuntSession getOrCreateSession(Player player) {
        return playerSessions.computeIfAbsent(player.getUniqueId(), k -> new HuntSession(player.getUniqueId()));
    }

    public void removeSession(UUID playerId) {
        playerSessions.remove(playerId);
    }

    public void reloadHunts() {
        // Clear existing spawned entities
        for (Entity entity : spawnedEntities.values()) {
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
        spawnedEntities.clear();
        respawnTimers.clear();
        
        // Respawn all models
        spawnHuntModels();
    }

    public void cleanup() {
        for (Entity entity : spawnedEntities.values()) {
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
        spawnedEntities.clear();
        playerSessions.clear();
        respawnTimers.clear();
    }

    public Map<String, Entity> getSpawnedEntities() {
        return new HashMap<>(spawnedEntities);
    }

    public Map<String, Long> getRespawnTimers() {
        return new HashMap<>(respawnTimers);
    }
}