package com.modelhunt.listeners;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.models.HuntLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class GUIListener implements Listener {
    private final ModelHuntPlugin plugin;

    public GUIListener(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        String title = event.getView().getTitle();
        
        if (title.equals("§6Hunt Configuration")) {
            handleMainMenuClick(event, player);
        } else if (title.equals("§6Hunt Locations")) {
            handleLocationListClick(event, player);
        } else if (title.equals("§6Plugin Statistics")) {
            handleStatsClick(event, player);
        } else if (title.startsWith("§6Create Hunt Location")) {
            handleCreationWizardClick(event, player);
        }
    }

    private void handleMainMenuClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        switch (event.getSlot()) {
            case 10: // Create Hunt Location
                player.closeInventory();
                plugin.getHuntConfigGUI().openCreationWizard(player);
                break;
            case 12: // Manage Hunt Locations
                player.closeInventory();
                plugin.getHuntConfigGUI().openLocationsList(player);
                break;
            case 14: // Reload Configuration
                player.closeInventory();
                plugin.getConfigManager().reloadConfig();
                player.sendMessage("§a[Hunt] §7Configuration reloaded successfully!");
                break;
            case 16: // Plugin Statistics
                player.closeInventory();
                plugin.getHuntConfigGUI().openStatsGUI(player);
                break;
        }
    }

    private void handleLocationListClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        if (event.getSlot() == event.getInventory().getSize() - 5) { // Back button
            player.closeInventory();
            plugin.getHuntConfigGUI().openMainMenu(player);
            return;
        }

        if (item.getType() == Material.GREEN_WOOL || item.getType() == Material.RED_WOOL) {
            String locationId = item.getItemMeta().getDisplayName().substring(4); // Remove color codes
            HuntLocation location = plugin.getConfigManager().getHuntLocation(locationId);
            
            if (location == null) return;

            if (event.getClick() == ClickType.LEFT) {
                // Toggle active state
                location.setActive(!location.isActive());
                plugin.getConfigManager().saveHuntLocation(locationId, location);
                plugin.getHuntManager().reloadHunts();
                
                String status = location.isActive() ? "§aenabled" : "§cdisabled";
                player.sendMessage("§a[Hunt] §7Hunt location '" + locationId + "' " + status + "§7!");
                
                // Refresh GUI
                plugin.getHuntConfigGUI().openLocationsList(player);
                
            } else if (event.getClick() == ClickType.RIGHT) {
                // Delete location
                plugin.getConfigManager().removeHuntLocation(locationId);
                plugin.getHuntManager().reloadHunts();
                player.sendMessage("§a[Hunt] §7Hunt location '" + locationId + "' deleted successfully!");
                
                // Refresh GUI
                plugin.getHuntConfigGUI().openLocationsList(player);
                
            } else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                // Teleport to location
                player.closeInventory();
                player.teleport(location.getLocation());
                player.sendMessage("§a[Hunt] §7Teleported to hunt location '" + locationId + "'!");
            }
        }
    }

    private void handleStatsClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        
        if (event.getSlot() == 22) { // Back button
            player.closeInventory();
            plugin.getHuntConfigGUI().openMainMenu(player);
        }
    }

    private void handleCreationWizardClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        switch (event.getSlot()) {
            case 10: // Set Location
                player.closeInventory();
                plugin.getLocationCreationManager().giveLocationSetter(player);
                player.sendMessage("§a[Hunt] §7Right-click anywhere to set the hunt location!");
                player.sendMessage("§7Or use §e/hunt setpos §7to use your current position.");
                break;
                
            case 12: // Set Model ID
                player.closeInventory();
                plugin.getChatInputListener().startModelIdInput(player);
                break;
                
            case 14: // Set MythicMob Type
                player.closeInventory();
                plugin.getChatInputListener().startMythicMobInput(player);
                break;
                
            case 19: // Configure Rewards
                player.closeInventory();
                plugin.getChatInputListener().startRewardsInput(player);
                break;
                
            case 21: // Set Messages
                player.closeInventory();
                plugin.getChatInputListener().startMessagesInput(player);
                break;
                
            case 23: // Advanced Settings
                player.closeInventory();
                plugin.getChatInputListener().startAdvancedInput(player);
                break;
                
            case 31: // Create Hunt Location
                if (plugin.getLocationCreationManager().canCreateLocation(player)) {
                    player.closeInventory();
                    plugin.getChatInputListener().startLocationIdInput(player);
                } else {
                    player.sendMessage("§c[Hunt] §7Please complete all required steps first!");
                }
                break;
                
            case 40: // Cancel
                player.closeInventory();
                plugin.getLocationCreationManager().clearSession(player);
                player.sendMessage("§e[Hunt] §7Hunt creation cancelled.");
                break;
        }
    }
}