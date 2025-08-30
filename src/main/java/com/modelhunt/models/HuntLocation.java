package com.modelhunt.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class HuntLocation {
    private final String id;
    private final Location location;
    private final String modelId;
    private final String mythicMobType;
    private final List<String> rewards;
    private final List<String> commands;
    private final String clickMessage;
    private final String completionMessage;
    private final int respawnTimeSeconds;
    private final double clickRadius;
    private boolean isActive;

    public HuntLocation(String id, Location location, String modelId, String mythicMobType,
                       List<String> rewards, List<String> commands, String clickMessage,
                       String completionMessage, int respawnTimeSeconds, double clickRadius) {
        this.id = id;
        this.location = location;
        this.modelId = modelId;
        this.mythicMobType = mythicMobType;
        this.rewards = new ArrayList<>(rewards);
        this.commands = new ArrayList<>(commands);
        this.clickMessage = clickMessage;
        this.completionMessage = completionMessage;
        this.respawnTimeSeconds = respawnTimeSeconds;
        this.clickRadius = clickRadius;
        this.isActive = true;
    }

    public static HuntLocation fromConfig(ConfigurationSection section) {
        try {
            String worldName = section.getString("world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }

            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw", 0.0);
            float pitch = (float) section.getDouble("pitch", 0.0);

            Location location = new Location(world, x, y, z, yaw, pitch);
            String modelId = section.getString("model-id");
            String mythicMobType = section.getString("mythic-mob-type");
            List<String> rewards = section.getStringList("rewards");
            List<String> commands = section.getStringList("commands");
            String clickMessage = section.getString("click-message", "§aYou found a hidden model!");
            String completionMessage = section.getString("completion-message", "§6Hunt completed!");
            int respawnTime = section.getInt("respawn-time-seconds", 300);
            double clickRadius = section.getDouble("click-radius", 3.0);

            return new HuntLocation("", location, modelId, mythicMobType, rewards, commands,
                    clickMessage, completionMessage, respawnTime, clickRadius);
        } catch (Exception e) {
            return null;
        }
    }

    public void saveToConfig(org.bukkit.configuration.file.FileConfiguration config, String path) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        config.set(path + ".model-id", modelId);
        config.set(path + ".mythic-mob-type", mythicMobType);
        config.set(path + ".rewards", rewards);
        config.set(path + ".commands", commands);
        config.set(path + ".click-message", clickMessage);
        config.set(path + ".completion-message", completionMessage);
        config.set(path + ".respawn-time-seconds", respawnTimeSeconds);
        config.set(path + ".click-radius", clickRadius);
    }

    // Getters
    public String getId() { return id; }
    public Location getLocation() { return location; }
    public String getModelId() { return modelId; }
    public String getMythicMobType() { return mythicMobType; }
    public List<String> getRewards() { return new ArrayList<>(rewards); }
    public List<String> getCommands() { return new ArrayList<>(commands); }
    public String getClickMessage() { return clickMessage; }
    public String getCompletionMessage() { return completionMessage; }
    public int getRespawnTimeSeconds() { return respawnTimeSeconds; }
    public double getClickRadius() { return clickRadius; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}