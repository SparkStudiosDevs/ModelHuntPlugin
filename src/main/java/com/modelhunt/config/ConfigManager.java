package com.modelhunt.config;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.models.HuntLocation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final ModelHuntPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private final Map<String, HuntLocation> huntLocations = new HashMap<>();

    public ConfigManager(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        // Create plugin folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");

        // Create default config if it doesn't exist
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        loadHuntLocations();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config file!", e);
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        huntLocations.clear();
        loadHuntLocations();
        plugin.getHuntManager().reloadHunts();
    }

    private void loadHuntLocations() {
        ConfigurationSection locationsSection = config.getConfigurationSection("hunt-locations");
        if (locationsSection == null) {
            plugin.getLogger().info("No hunt locations found in config.");
            return;
        }

        for (String key : locationsSection.getKeys(false)) {
            ConfigurationSection locationSection = locationsSection.getConfigurationSection(key);
            if (locationSection != null) {
                HuntLocation location = HuntLocation.fromConfig(locationSection);
                if (location != null) {
                    huntLocations.put(key, location);
                    plugin.getLogger().info("Loaded hunt location: " + key);
                }
            }
        }
    }

    public void saveHuntLocation(String id, HuntLocation location) {
        location.saveToConfig(config, "hunt-locations." + id);
        huntLocations.put(id, location);
        saveConfig();
    }

    public void removeHuntLocation(String id) {
        huntLocations.remove(id);
        config.set("hunt-locations." + id, null);
        saveConfig();
    }

    public Map<String, HuntLocation> getHuntLocations() {
        return new HashMap<>(huntLocations);
    }

    public HuntLocation getHuntLocation(String id) {
        return huntLocations.get(id);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMessage(String path, String defaultValue) {
        return config.getString("messages." + path, defaultValue);
    }

    public int getCooldownSeconds() {
        return config.getInt("cooldown-seconds", 300); // 5 minutes default
    }

    public boolean isDebugMode() {
        return config.getBoolean("debug", false);
    }
}