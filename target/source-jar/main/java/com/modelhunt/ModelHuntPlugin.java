package com.modelhunt;

import com.modelhunt.commands.ModelHuntCommand;
import com.modelhunt.config.ConfigManager;
import com.modelhunt.data.PlayerDataManager;
import com.modelhunt.gui.HuntConfigGUI;
import com.modelhunt.listeners.ChatInputListener;
import com.modelhunt.listeners.GUIListener;
import com.modelhunt.listeners.LocationSetListener;
import com.modelhunt.listeners.ModelClickListener;
import com.modelhunt.managers.LocationCreationManager;
import com.modelhunt.managers.HuntManager;
import com.modelhunt.managers.RewardManager;
import com.modelhunt.placeholders.ModelHuntPlaceholders;
import com.modelhunt.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Bukkit;
public final class ModelHuntPlugin extends JavaPlugin {
    private static ModelHuntPlugin instance;
    private ConfigManager configManager;
    private HuntManager huntManager;
    private RewardManager rewardManager;
    private LocationCreationManager locationCreationManager;
    private HuntConfigGUI huntConfigGUI;
    private ChatInputListener chatInputListener;
    private PlayerDataManager playerDataManager;
    private ModelHuntPlaceholders placeholders;

    @Override
    public void onEnable() {
        // Initialize message utils first
        MessageUtils.init(this);
        
        instance = this;
        
        // Check for required dependencies
        if (!checkDependencies()) {
            getLogger().severe("Required dependencies not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize managers
        configManager = new ConfigManager(this);
        playerDataManager = new PlayerDataManager(this);
        rewardManager = new RewardManager(this);
        locationCreationManager = new LocationCreationManager(this);
        huntManager = new HuntManager(this);
        huntConfigGUI = new HuntConfigGUI(this);
        chatInputListener = new ChatInputListener(this);
        
        // Load configuration
        configManager.loadConfig();
        
        // Register events
        getServer().getPluginManager().registerEvents(new ModelClickListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new LocationSetListener(this), this);
        getServer().getPluginManager().registerEvents(chatInputListener, this);
        
        // Register commands
        getCommand("modelhunt").setExecutor(new ModelHuntCommand(this));
        
        // Register PlaceholderAPI expansion if available
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders = new ModelHuntPlaceholders(this);
            placeholders.register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
        
        // Check for required dependencies
        if (getServer().getPluginManager().getPlugin("ModelEngine") == null) {
            getLogger().warning("ModelEngine not found! Some features may not work properly.");
        }
        
        if (getServer().getPluginManager().getPlugin("MythicMobs") == null) {
            getLogger().warning("MythicMobs not found! Some features may not work properly.");
        }
        
        // Spawn hunt models after everything is loaded
        getServer().getScheduler().runTaskLater(this, () -> {
            huntManager.spawnHuntModels();
        }, 20L); // Wait 1 second for everything to load
        
        getLogger().info("ModelHunt plugin has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (playerDataManager != null) {
            Bukkit.getOnlinePlayers().forEach(player -> 
                playerDataManager.savePlayerData(player.getUniqueId()));
        }
        
        if (placeholders != null) {
            placeholders.unregister();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
        }
        if (huntManager != null) {
            huntManager.cleanup();
        }
        getLogger().info("ModelHunt plugin has been disabled!");
    }
    
    private boolean checkDependencies() {
        boolean hasModelEngine = getServer().getPluginManager().getPlugin("ModelEngine") != null;
        boolean hasMythicMobs = getServer().getPluginManager().getPlugin("MythicMobs") != null;
        
        if (!hasModelEngine) {
            getLogger().severe("ModelEngine not found! Please install ModelEngine.");
        }
        if (!hasMythicMobs) {
            getLogger().severe("MythicMobs not found! Please install MythicMobs.");
        }
        
        return hasModelEngine && hasMythicMobs;
    }

    public static ModelHuntPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HuntManager getHuntManager() {
        return huntManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public LocationCreationManager getLocationCreationManager() {
        return locationCreationManager;
    public HuntConfigGUI getHuntConfigGUI() {
        return huntConfigGUI;
    }
    }
    public ChatInputListener getChatInputListener() {
        return chatInputListener;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}