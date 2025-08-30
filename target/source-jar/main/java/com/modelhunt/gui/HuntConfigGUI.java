package com.modelhunt.gui;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.models.HuntLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HuntConfigGUI {
    private final ModelHuntPlugin plugin;
    private static final String GUI_TITLE = "§6Hunt Configuration";

    public HuntConfigGUI(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Create Hunt Location
        ItemStack createItem = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createItem.getItemMeta();
        createMeta.setDisplayName("§a§lCreate Hunt Location");
        createMeta.setLore(Arrays.asList(
                "§7Click to start creating a new",
                "§7hunt location with guided setup",
                "",
                "§eClick to continue"
        ));
        createItem.setItemMeta(createMeta);
        gui.setItem(10, createItem);

        // List Hunt Locations
        ItemStack listItem = new ItemStack(Material.BOOK);
        ItemMeta listMeta = listItem.getItemMeta();
        listMeta.setDisplayName("§b§lManage Hunt Locations");
        listMeta.setLore(Arrays.asList(
                "§7View and manage existing",
                "§7hunt locations",
                "",
                "§eClick to view"
        ));
        listItem.setItemMeta(listMeta);
        gui.setItem(12, listItem);

        // Reload Configuration
        ItemStack reloadItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta reloadMeta = reloadItem.getItemMeta();
        reloadMeta.setDisplayName("§e§lReload Configuration");
        reloadMeta.setLore(Arrays.asList(
                "§7Reload the plugin configuration",
                "§7and respawn all hunt models",
                "",
                "§eClick to reload"
        ));
        reloadItem.setItemMeta(reloadMeta);
        gui.setItem(14, reloadItem);

        // Plugin Statistics
        ItemStack statsItem = new ItemStack(Material.PAPER);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName("§d§lPlugin Statistics");
        statsMeta.setLore(Arrays.asList(
                "§7View plugin statistics",
                "§7and active hunt locations",
                "",
                "§eClick to view"
        ));
        statsItem.setItemMeta(statsMeta);
        gui.setItem(16, statsItem);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    public void openLocationsList(Player player) {
        Map<String, HuntLocation> locations = plugin.getConfigManager().getHuntLocations();
        int size = Math.max(9, ((locations.size() + 8) / 9) * 9);
        size = Math.min(size, 54);

        Inventory gui = Bukkit.createInventory(null, size, "§6Hunt Locations");

        int slot = 0;
        for (Map.Entry<String, HuntLocation> entry : locations.entrySet()) {
            if (slot >= size - 9) break; // Leave space for navigation

            String locationId = entry.getKey();
            HuntLocation location = entry.getValue();

            ItemStack item = new ItemStack(location.isActive() ? Material.GREEN_WOOL : Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e§l" + locationId);
            meta.setLore(Arrays.asList(
                    "§7Status: " + (location.isActive() ? "§aActive" : "§cInactive"),
                    "§7Model: §b" + location.getModelId(),
                    "§7MythicMob: §d" + location.getMythicMobType(),
                    "§7World: §f" + location.getLocation().getWorld().getName(),
                    "§7Coordinates: §f" + String.format("%.1f, %.1f, %.1f", 
                            location.getLocation().getX(), 
                            location.getLocation().getY(), 
                            location.getLocation().getZ()),
                    "§7Rewards: §6" + location.getRewards().size() + " items",
                    "",
                    "§aLeft Click: §7Toggle Active State",
                    "§cRight Click: §7Delete Location",
                    "§eShift+Click: §7Teleport to Location"
            ));
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§c§lBack to Main Menu");
        backItem.setItemMeta(backMeta);
        gui.setItem(size - 5, backItem);

        player.openInventory(gui);
    }

    public void openStatsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6Plugin Statistics");

        Map<String, HuntLocation> locations = plugin.getConfigManager().getHuntLocations();
        Map<String, org.bukkit.entity.Entity> spawnedEntities = plugin.getHuntManager().getSpawnedEntities();

        // Active Locations
        ItemStack activeItem = new ItemStack(Material.LIME_DYE);
        ItemMeta activeMeta = activeItem.getItemMeta();
        activeMeta.setDisplayName("§a§lActive Locations");
        long activeCount = locations.values().stream().filter(HuntLocation::isActive).count();
        activeMeta.setLore(Arrays.asList(
                "§7Currently active hunt locations",
                "",
                "§e" + activeCount + " §7active locations"
        ));
        activeItem.setItemMeta(activeMeta);
        gui.setItem(11, activeItem);

        // Spawned Models
        ItemStack spawnedItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta spawnedMeta = spawnedItem.getItemMeta();
        spawnedMeta.setDisplayName("§b§lSpawned Models");
        spawnedMeta.setLore(Arrays.asList(
                "§7Currently spawned hunt models",
                "",
                "§e" + spawnedEntities.size() + " §7spawned models"
        ));
        spawnedItem.setItemMeta(spawnedMeta);
        gui.setItem(13, spawnedItem);

        // Total Locations
        ItemStack totalItem = new ItemStack(Material.MAP);
        ItemMeta totalMeta = totalItem.getItemMeta();
        totalMeta.setDisplayName("§e§lTotal Locations");
        totalMeta.setLore(Arrays.asList(
                "§7Total configured hunt locations",
                "",
                "§e" + locations.size() + " §7total locations"
        ));
        totalItem.setItemMeta(totalMeta);
        gui.setItem(15, totalItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§c§lBack to Main Menu");
        backItem.setItemMeta(backMeta);
        gui.setItem(22, backItem);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    public void openCreationWizard(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, "§6Create Hunt Location - Step by Step");

        // Step 1: Set Location
        ItemStack locationItem = new ItemStack(Material.COMPASS);
        ItemMeta locationMeta = locationItem.getItemMeta();
        locationMeta.setDisplayName("§e§lStep 1: Set Location");
        
        boolean hasLocation = plugin.getLocationCreationManager().hasLocation(player);
        if (hasLocation) {
            locationMeta.setLore(Arrays.asList(
                    "§a✓ Location set successfully!",
                    "§7Click to change location",
                    "",
                    "§eClick to modify"
            ));
            locationItem.setType(Material.RECOVERY_COMPASS);
        } else {
            locationMeta.setLore(Arrays.asList(
                    "§7Set the hunt location by:",
                    "§71. Getting the location setter tool",
                    "§72. Right-clicking where you want the hunt",
                    "§7Or use your current position",
                    "",
                    "§eClick to get location setter"
            ));
        }
        locationItem.setItemMeta(locationMeta);
        gui.setItem(10, locationItem);

        // Step 2: Set Model ID
        ItemStack modelItem = new ItemStack(Material.ARMOR_STAND);
        ItemMeta modelMeta = modelItem.getItemMeta();
        modelMeta.setDisplayName("§e§lStep 2: Set Model ID");
        
        boolean hasModel = plugin.getLocationCreationManager().hasModelId(player);
        if (hasModel) {
            String modelId = plugin.getLocationCreationManager().getModelId(player);
            modelMeta.setLore(Arrays.asList(
                    "§a✓ Model ID: §b" + modelId,
                    "§7Click to change model",
                    "",
                    "§eClick to modify"
            ));
            modelItem.setType(Material.PLAYER_HEAD);
        } else {
            modelMeta.setLore(Arrays.asList(
                    "§7Set the ModelEngine model ID",
                    "§7Format: §bmodelengine:model_name",
                    "§7Or just: §bmodel_name",
                    "",
                    "§eClick to set model"
            ));
        }
        modelItem.setItemMeta(modelMeta);
        gui.setItem(12, modelItem);

        // Step 3: Set MythicMob Type
        ItemStack mobItem = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta mobMeta = mobItem.getItemMeta();
        mobMeta.setDisplayName("§e§lStep 3: Set MythicMob Type");
        
        boolean hasMob = plugin.getLocationCreationManager().hasMythicMobType(player);
        if (hasMob) {
            String mobType = plugin.getLocationCreationManager().getMythicMobType(player);
            mobMeta.setLore(Arrays.asList(
                    "§a✓ MythicMob: §d" + mobType,
                    "§7Click to change mob type",
                    "",
                    "§eClick to modify"
            ));
            mobItem.setType(Material.CREEPER_HEAD);
        } else {
            mobMeta.setLore(Arrays.asList(
                    "§7Set the MythicMobs mob type",
                    "§7Format: §dmythicmobs:mob_name",
                    "§7Or just: §dmob_name",
                    "",
                    "§eClick to set mob type"
            ));
        }
        mobItem.setItemMeta(mobMeta);
        gui.setItem(14, mobItem);

        // Step 4: Configure Rewards
        ItemStack rewardItem = new ItemStack(Material.CHEST);
        ItemMeta rewardMeta = rewardItem.getItemMeta();
        rewardMeta.setDisplayName("§e§lStep 4: Configure Rewards");
        
        List<String> rewards = plugin.getLocationCreationManager().getRewards(player);
        if (rewards != null && !rewards.isEmpty()) {
            rewardMeta.setLore(Arrays.asList(
                    "§a✓ " + rewards.size() + " rewards configured",
                    "§7Click to modify rewards",
                    "",
                    "§eClick to modify"
            ));
            rewardItem.setType(Material.ENDER_CHEST);
        } else {
            rewardMeta.setLore(Arrays.asList(
                    "§7Configure hunt rewards",
                    "§7Examples:",
                    "§7- §eitem:DIAMOND*3",
                    "§7- §eexp:100",
                    "§7- §emoney:50.0",
                    "",
                    "§eClick to configure"
            ));
        }
        rewardItem.setItemMeta(rewardMeta);
        gui.setItem(19, rewardItem);

        // Step 5: Set Messages
        ItemStack messageItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta messageMeta = messageItem.getItemMeta();
        messageMeta.setDisplayName("§e§lStep 5: Set Messages");
        messageMeta.setLore(Arrays.asList(
                "§7Configure click and completion messages",
                "§7These will be shown to players",
                "",
                "§eClick to configure"
        ));
        messageItem.setItemMeta(messageMeta);
        gui.setItem(21, messageItem);

        // Step 6: Advanced Settings
        ItemStack advancedItem = new ItemStack(Material.REDSTONE);
        ItemMeta advancedMeta = advancedItem.getItemMeta();
        advancedMeta.setDisplayName("§e§lStep 6: Advanced Settings");
        advancedMeta.setLore(Arrays.asList(
                "§7Configure respawn time, click radius,",
                "§7and other advanced options",
                "",
                "§eClick to configure"
        ));
        advancedItem.setItemMeta(advancedMeta);
        gui.setItem(23, advancedItem);

        // Create Button (only if all required steps are complete)
        boolean canCreate = hasLocation && hasModel && hasMob;
        ItemStack createButton = new ItemStack(canCreate ? Material.GREEN_CONCRETE : Material.RED_CONCRETE);
        ItemMeta createButtonMeta = createButton.getItemMeta();
        createButtonMeta.setDisplayName(canCreate ? "§a§lCreate Hunt Location" : "§c§lIncomplete Setup");
        
        if (canCreate) {
            createButtonMeta.setLore(Arrays.asList(
                    "§7All required steps completed!",
                    "§7Click to create the hunt location",
                    "",
                    "§aClick to create"
            ));
        } else {
            createButtonMeta.setLore(Arrays.asList(
                    "§7Complete steps 1-3 to create",
                    "§7the hunt location",
                    "",
                    "§cComplete required steps first"
            ));
        }
        createButton.setItemMeta(createButtonMeta);
        gui.setItem(31, createButton);

        // Cancel Button
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName("§c§lCancel Creation");
        cancelMeta.setLore(Arrays.asList(
                "§7Cancel the creation process",
                "§7and return to main menu",
                "",
                "§cClick to cancel"
        ));
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(40, cancelItem);

        // Fill empty slots
        for (int i = 0; i < 45; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }

    public void openLocationsList(Player player) {
        Map<String, HuntLocation> locations = plugin.getConfigManager().getHuntLocations();
        int size = Math.max(9, ((locations.size() + 8) / 9) * 9);
        size = Math.min(size, 54);

        Inventory gui = Bukkit.createInventory(null, size, "§6Hunt Locations");

        int slot = 0;
        for (Map.Entry<String, HuntLocation> entry : locations.entrySet()) {
            if (slot >= size - 9) break; // Leave space for navigation

            String locationId = entry.getKey();
            HuntLocation location = entry.getValue();

            ItemStack item = new ItemStack(location.isActive() ? Material.GREEN_WOOL : Material.RED_WOOL);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e§l" + locationId);
            meta.setLore(Arrays.asList(
                    "§7Status: " + (location.isActive() ? "§aActive" : "§cInactive"),
                    "§7Model: §b" + location.getModelId(),
                    "§7MythicMob: §d" + location.getMythicMobType(),
                    "§7World: §f" + location.getLocation().getWorld().getName(),
                    "§7Coordinates: §f" + String.format("%.1f, %.1f, %.1f", 
                            location.getLocation().getX(), 
                            location.getLocation().getY(), 
                            location.getLocation().getZ()),
                    "§7Rewards: §6" + location.getRewards().size() + " items",
                    "",
                    "§aLeft Click: §7Toggle Active State",
                    "§cRight Click: §7Delete Location",
                    "§eShift+Click: §7Teleport to Location"
            ));
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }

        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§c§lBack to Main Menu");
        backItem.setItemMeta(backMeta);
        gui.setItem(size - 5, backItem);

        player.openInventory(gui);
    }

    public void openStatsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6Plugin Statistics");

        Map<String, HuntLocation> locations = plugin.getConfigManager().getHuntLocations();
        Map<String, org.bukkit.entity.Entity> spawnedEntities = plugin.getHuntManager().getSpawnedEntities();

        // Active Locations
        ItemStack activeItem = new ItemStack(Material.LIME_DYE);
        ItemMeta activeMeta = activeItem.getItemMeta();
        activeMeta.setDisplayName("§a§lActive Locations");
        long activeCount = locations.values().stream().filter(HuntLocation::isActive).count();
        activeMeta.setLore(Arrays.asList(
                "§7Currently active hunt locations",
                "",
                "§e" + activeCount + " §7active locations"
        ));
        activeItem.setItemMeta(activeMeta);
        gui.setItem(11, activeItem);

        // Spawned Models
        ItemStack spawnedItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta spawnedMeta = spawnedItem.getItemMeta();
        spawnedMeta.setDisplayName("§b§lSpawned Models");
        spawnedMeta.setLore(Arrays.asList(
                "§7Currently spawned hunt models",
                "",
                "§e" + spawnedEntities.size() + " §7spawned models"
        ));
        spawnedItem.setItemMeta(spawnedMeta);
        gui.setItem(13, spawnedItem);

        // Total Locations
        ItemStack totalItem = new ItemStack(Material.MAP);
        ItemMeta totalMeta = totalItem.getItemMeta();
        totalMeta.setDisplayName("§e§lTotal Locations");
        totalMeta.setLore(Arrays.asList(
                "§7Total configured hunt locations",
                "",
                "§e" + locations.size() + " §7total locations"
        ));
        totalItem.setItemMeta(totalMeta);
        gui.setItem(15, totalItem);

        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§c§lBack to Main Menu");
        backItem.setItemMeta(backMeta);
        gui.setItem(22, backItem);

        // Fill empty slots
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
    }
}