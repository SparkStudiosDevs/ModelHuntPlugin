package com.modelhunt.utils;

import com.modelhunt.ModelHuntPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

public class MessageUtils {
    private static ModelHuntPlugin plugin;
    
    public static void init(ModelHuntPlugin plugin) {
        MessageUtils.plugin = plugin;
    }
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static String colorizeWithPlaceholders(Player player, String message) {
        String processed = message;
        
        // Apply PlaceholderAPI if available
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            processed = PlaceholderAPI.setPlaceholders(player, processed);
        }
        
        return colorize(processed);
    }
    
    public static void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(colorizeWithPlaceholders(player, title), 
                        colorizeWithPlaceholders(player, subtitle), 10, 70, 20);
    }
    
    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(colorizeWithPlaceholders(player, message));
    }
    
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
    
    public static String formatLocation(org.bukkit.Location location) {
        return String.format("%s: %.1f, %.1f, %.1f", 
                location.getWorld().getName(), 
                location.getX(), 
                location.getY(), 
                location.getZ());
    }
}