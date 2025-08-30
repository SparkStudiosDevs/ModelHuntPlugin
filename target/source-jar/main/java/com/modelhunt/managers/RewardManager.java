package com.modelhunt.managers;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewardManager {
    private final ModelHuntPlugin plugin;
    private Economy economy = null;
    private static final Pattern ITEM_PATTERN = Pattern.compile("(\\w+)(?::(\\d+))?(?:\\*(\\d+))?");

    public RewardManager(ModelHuntPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }
    
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
    }

    public void giveRewards(Player player, List<String> rewards) {
        for (String reward : rewards) {
            processReward(player, reward);
        }
        
        // Update player data
        plugin.getPlayerDataManager().getPlayerData(player).incrementTotalRewards();
    }

    private void processReward(Player player, String reward) {
        reward = reward.trim();
        
        if (reward.startsWith("money:")) {
            handleMoneyReward(player, reward);
        } else if (reward.startsWith("exp:")) {
            handleExpReward(player, reward);
        } else if (reward.startsWith("item:")) {
            handleItemReward(player, reward.substring(5));
        } else if (reward.startsWith("command:")) {
            handleCommandReward(player, reward.substring(8));
        } else {
            // Assume it's an item if no prefix
            handleItemReward(player, reward);
        }
    }

    private void handleMoneyReward(Player player, String reward) {
        try {
            double amount = Double.parseDouble(reward.substring(6));
            
            if (economy != null) {
                economy.depositPlayer(player, amount);
                String message = "§a[Hunt] §7You received §2$" + String.format("%.2f", amount) + "§7!";
                player.sendMessage(MessageUtils.colorizeWithPlaceholders(player, message));
            } else {
                String message = "§a[Hunt] §7You would receive §2$" + String.format("%.2f", amount) + " §7(Economy plugin not found)";
                player.sendMessage(MessageUtils.colorizeWithPlaceholders(player, message));
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid money reward format: " + reward);
        }
    }

    private void handleExpReward(Player player, String reward) {
        try {
            int amount = Integer.parseInt(reward.substring(4));
            player.giveExp(amount);
            String message = "§a[Hunt] §7You received §b" + amount + " experience§7!";
            player.sendMessage(MessageUtils.colorizeWithPlaceholders(player, message));
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid exp reward format: " + reward);
        }
    }

    private void handleItemReward(Player player, String itemString) {
        Matcher matcher = ITEM_PATTERN.matcher(itemString.toUpperCase());
        if (!matcher.matches()) {
            plugin.getLogger().warning("Invalid item format: " + itemString);
            return;
        }

        String materialName = matcher.group(1);
        String dataStr = matcher.group(2);
        String amountStr = matcher.group(3);

        try {
            Material material = Material.valueOf(materialName);
            int amount = amountStr != null ? Integer.parseInt(amountStr) : 1;
            
            ItemStack item = new ItemStack(material, amount);
            
            // Add custom display name if configured
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§6Hunt Reward");
                item.setItemMeta(meta);
            }
            
            // Give item to player
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
                String message = "§a[Hunt] §7You received §e" + amount + "x " + 
                        material.name().toLowerCase().replace("_", " ") + "§7!";
                player.sendMessage(MessageUtils.colorizeWithPlaceholders(player, message));
            } else {
                player.getWorld().dropItem(player.getLocation(), item);
                String message = "§a[Hunt] §7You received §e" + amount + "x " + 
                        material.name().toLowerCase().replace("_", " ") + " §7(dropped on ground)!";
                player.sendMessage(MessageUtils.colorizeWithPlaceholders(player, message));
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + materialName);
        }
    }

    private void handleCommandReward(Player player, String command) {
        String processedCommand = command.replace("{player}", player.getName());
        
        // Apply PlaceholderAPI if available
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            processedCommand = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processedCommand);
        }
        
        final String finalCommand = processedCommand;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        });
    }
}