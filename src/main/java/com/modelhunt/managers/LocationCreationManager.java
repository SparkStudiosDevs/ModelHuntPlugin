package com.modelhunt.managers;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.models.HuntLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocationCreationManager {
    private final ModelHuntPlugin plugin;
    private final Map<UUID, CreationSession> sessions = new ConcurrentHashMap<>();

    public LocationCreationManager(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void giveLocationSetter(Player player) {
        ItemStack setter = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = setter.getItemMeta();
        meta.setDisplayName("§6§lHunt Location Setter");
        meta.setLore(Arrays.asList(
                "§7Right-click anywhere to set",
                "§7the hunt location position",
                "",
                "§eRight-click to set location"
        ));
        setter.setItemMeta(meta);
        
        player.getInventory().addItem(setter);
        player.sendMessage("§a[Hunt] §7Location setter tool added to your inventory!");
    }

    public void setLocation(Player player, Location location) {
        CreationSession session = getOrCreateSession(player);
        session.setLocation(location);
        player.sendMessage("§a[Hunt] §7Location set at: §f" + formatLocation(location));
    }

    public void setCurrentPosition(Player player) {
        setLocation(player, player.getLocation());
    }

    public void setModelId(Player player, String modelId) {
        CreationSession session = getOrCreateSession(player);
        session.setModelId(modelId);
        player.sendMessage("§a[Hunt] §7Model ID set to: §b" + modelId);
    }

    public void setMythicMobType(Player player, String mobType) {
        CreationSession session = getOrCreateSession(player);
        session.setMythicMobType(mobType);
        player.sendMessage("§a[Hunt] §7MythicMob type set to: §d" + mobType);
    }

    public void setRewards(Player player, List<String> rewards) {
        CreationSession session = getOrCreateSession(player);
        session.setRewards(rewards);
        player.sendMessage("§a[Hunt] §7Rewards configured: §e" + rewards.size() + " rewards");
    }

    public void setMessages(Player player, String clickMessage, String completionMessage) {
        CreationSession session = getOrCreateSession(player);
        session.setClickMessage(clickMessage);
        session.setCompletionMessage(completionMessage);
        player.sendMessage("§a[Hunt] §7Messages configured successfully!");
    }

    public void setAdvancedSettings(Player player, int respawnTime, double clickRadius) {
        CreationSession session = getOrCreateSession(player);
        session.setRespawnTime(respawnTime);
        session.setClickRadius(clickRadius);
        player.sendMessage("§a[Hunt] §7Advanced settings configured!");
    }

    public boolean createHuntLocation(Player player, String locationId) {
        CreationSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.isComplete()) {
            player.sendMessage("§c[Hunt] §7Please complete all required configuration steps first!");
            return false;
        }

        if (plugin.getConfigManager().getHuntLocation(locationId) != null) {
            player.sendMessage("§c[Hunt] §7A hunt location with ID '" + locationId + "' already exists!");
            return false;
        }

        HuntLocation huntLocation = new HuntLocation(
                locationId,
                session.getLocation(),
                session.getModelId(),
                session.getMythicMobType(),
                session.getRewards(),
                new ArrayList<>(), // Commands can be added later
                session.getClickMessage(),
                session.getCompletionMessage(),
                session.getRespawnTime(),
                session.getClickRadius()
        );

        plugin.getConfigManager().saveHuntLocation(locationId, huntLocation);
        plugin.getHuntManager().reloadHunts();
        
        clearSession(player);
        player.sendMessage("§a[Hunt] §7Hunt location '" + locationId + "' created successfully!");
        return true;
    }

    public boolean canCreateLocation(Player player) {
        CreationSession session = sessions.get(player.getUniqueId());
        return session != null && session.isComplete();
    }

    public void clearSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    // Getters for GUI status checks
    public boolean hasLocation(Player player) {
        CreationSession session = sessions.get(player.getUniqueId());
        return session != null && session.getLocation() != null;
    }

    public boolean hasModelId(Player player) {
        CreationSession session = sessions.get(player.getUniqueId());
        return session != null && session.getModelId() != null;
    }

    public boolean hasMythicMobType(Player player) {
        CreationSession session = sessions.get(player.getUniqueId());
        return session != null && session.getMythicMobType() != null;
    }

    public String getModelId(Player player) {
        CreationSession session = sessions.get(player.getUniqueId());
        return session != null ? session.getModelId() : null;
    }

    public String getMythicMobType(Player player) {
        CreationSession session = sessions.get(player.getUniqueId());
        return session != null ? session.getMythicMobType() : null;
    }

    public List<String> getRewards(Player player) {
        CreationSession session = sessions.get(player.getUniqueId());
        return session != null ? session.getRewards() : null;
    }

    private CreationSession getOrCreateSession(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), k -> new CreationSession());
    }

    private String formatLocation(Location loc) {
        return String.format("%s: %.1f, %.1f, %.1f", 
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    // Inner class for creation sessions
    private static class CreationSession {
        private Location location;
        private String modelId;
        private String mythicMobType;
        private List<String> rewards = new ArrayList<>();
        private String clickMessage = "§aYou found a hidden model!";
        private String completionMessage = "§6Hunt completed!";
        private int respawnTime = 300; // 5 minutes default
        private double clickRadius = 3.0;

        public boolean isComplete() {
            return location != null && modelId != null && mythicMobType != null;
        }

        // Getters and setters
        public Location getLocation() { return location; }
        public void setLocation(Location location) { this.location = location; }
        
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
        
        public String getMythicMobType() { return mythicMobType; }
        public void setMythicMobType(String mythicMobType) { this.mythicMobType = mythicMobType; }
        
        public List<String> getRewards() { return new ArrayList<>(rewards); }
        public void setRewards(List<String> rewards) { this.rewards = new ArrayList<>(rewards); }
        
        public String getClickMessage() { return clickMessage; }
        public void setClickMessage(String clickMessage) { this.clickMessage = clickMessage; }
        
        public String getCompletionMessage() { return completionMessage; }
        public void setCompletionMessage(String completionMessage) { this.completionMessage = completionMessage; }
        
        public int getRespawnTime() { return respawnTime; }
        public void setRespawnTime(int respawnTime) { this.respawnTime = respawnTime; }
        
        public double getClickRadius() { return clickRadius; }
        public void setClickRadius(double clickRadius) { this.clickRadius = clickRadius; }
    }
}