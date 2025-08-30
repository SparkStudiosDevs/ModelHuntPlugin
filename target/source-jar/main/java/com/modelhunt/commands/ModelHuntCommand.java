package com.modelhunt.commands;

import com.modelhunt.ModelHuntPlugin;
import com.modelhunt.data.PlayerDataManager;
import com.modelhunt.models.HuntLocation;
import com.modelhunt.models.HuntSession;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ModelHuntCommand implements CommandExecutor, TabCompleter {
    private final ModelHuntPlugin plugin;

    public ModelHuntCommand(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "gui":
                return handleGUI(sender);
            case "setpos":
                return handleSetPos(sender);
            case "delete":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "stats":
                return handleStats(sender, args);
            case "toggle":
                return handleToggle(sender, args);
            default:
                sendHelpMessage(sender);
                return true;
        }
    }

    private boolean handleGUI(CommandSender sender) {
        if (!sender.hasPermission("hunt.admin.use")) {
            sender.sendMessage("§cYou don't have permission to use the admin GUI!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use the GUI!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getHuntConfigGUI().openMainMenu(player);
        return true;
    }

    private boolean handleSetPos(CommandSender sender) {
        if (!sender.hasPermission("hunt.admin.create")) {
            sender.sendMessage("§cYou don't have permission to set hunt locations!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can set positions!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getLocationCreationManager().setCurrentPosition(player);
        return true;
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hunt.admin.create")) {
            sender.sendMessage("§cYou don't have permission to create hunt locations!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can create hunt locations!");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage("§cUsage: /hunt create <id> <model-id> <mythic-mob-type> [rewards...]");
            return true;
        }

        Player player = (Player) sender;
        String id = args[1];
        String modelId = args[2];
        String mythicMobType = args[3];
        
        List<String> rewards = new ArrayList<>();
        if (args.length > 4) {
            rewards.addAll(Arrays.asList(args).subList(4, args.length));
        }

        // Create hunt location at player's position
        Location location = player.getLocation();
        HuntLocation huntLocation = new HuntLocation(
                id, location, modelId, mythicMobType, rewards, new ArrayList<>(),
                "§aYou found a hidden model!", "§6Hunt completed!",
                300, 3.0
        );

        plugin.getConfigManager().saveHuntLocation(id, huntLocation);
        plugin.getHuntManager().spawnHuntModels();
        
        sender.sendMessage("§a[Hunt] §7Hunt location '" + id + "' created successfully!");
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hunt.admin.delete")) {
            sender.sendMessage("§cYou don't have permission to delete hunt locations!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /hunt delete <id>");
            return true;
        }

        String id = args[1];
        if (plugin.getConfigManager().getHuntLocation(id) == null) {
            sender.sendMessage("§c[Hunt] §7Hunt location '" + id + "' does not exist!");
            return true;
        }

        plugin.getConfigManager().removeHuntLocation(id);
        plugin.getHuntManager().reloadHunts();
        
        sender.sendMessage("§a[Hunt] §7Hunt location '" + id + "' deleted successfully!");
        return true;
    }

    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("hunt.admin.list")) {
            sender.sendMessage("§cYou don't have permission to list hunt locations!");
            return true;
        }

        Map<String, HuntLocation> locations = plugin.getConfigManager().getHuntLocations();
        if (locations.isEmpty()) {
            sender.sendMessage("§e[Hunt] §7No hunt locations configured.");
            return true;
        }

        sender.sendMessage("§6[Hunt] §7Hunt Locations:");
        for (Map.Entry<String, HuntLocation> entry : locations.entrySet()) {
            HuntLocation loc = entry.getValue();
            String status = loc.isActive() ? "§aActive" : "§cInactive";
            sender.sendMessage("§7- §e" + entry.getKey() + " §7(" + status + "§7) - Model: §b" + 
                    loc.getModelId() + " §7at §f" + formatLocation(loc.getLocation()));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("hunt.admin.reload")) {
            sender.sendMessage("§cYou don't have permission to reload the plugin!");
            return true;
        }

        plugin.getConfigManager().reloadConfig();
        sender.sendMessage("§a[Hunt] §7Plugin configuration reloaded successfully!");
        return true;
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hunt.admin.teleport")) {
            sender.sendMessage("§cYou don't have permission to teleport to hunt locations!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can teleport!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /hunt tp <id>");
            return true;
        }

        Player player = (Player) sender;
        String id = args[1];
        HuntLocation location = plugin.getConfigManager().getHuntLocation(id);
        
        if (location == null) {
            sender.sendMessage("§c[Hunt] §7Hunt location '" + id + "' does not exist!");
            return true;
        }

        player.teleport(location.getLocation());
        sender.sendMessage("§a[Hunt] §7Teleported to hunt location '" + id + "'!");
        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hunt.stats")) {
            sender.sendMessage("§cYou don't have permission to view hunt statistics!");
            return true;
        }

        Player target = null;
        if (args.length > 1) {
            if (!sender.hasPermission("hunt.admin.use")) {
                sender.sendMessage("§cYou don't have permission to view other players' stats!");
                return true;
            }
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§c[Hunt] §7Player '" + args[1] + "' not found!");
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§cYou must specify a player name when running from console!");
            return true;
        }

        HuntSession session = plugin.getHuntManager().getOrCreateSession(target);
        PlayerDataManager.PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(target);
        
        sender.sendMessage("§6[Hunt] §7Statistics for §e" + target.getName() + "§7:");
        sender.sendMessage("§7Total hunts completed: §b" + playerData.getTotalHuntsCompleted());
        sender.sendMessage("§7Total rewards received: §b" + playerData.getTotalRewardsReceived());
        sender.sendMessage("§7Session clicks: §b" + session.getTotalClicks());
        
        Map<String, HuntLocation> locations = plugin.getConfigManager().getHuntLocations();
        sender.sendMessage("§7Hunt completions by location:");
        for (Map.Entry<String, HuntLocation> entry : locations.entrySet()) {
            int completions = playerData.getHuntCompletions(entry.getKey());
            sender.sendMessage("§7- §e" + entry.getKey() + "§7: §b" + completions + " §7completions");
        }
        return true;
    }

    private boolean handleToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hunt.admin.edit")) {
            sender.sendMessage("§cYou don't have permission to toggle hunt locations!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /hunt toggle <id>");
            return true;
        }

        String id = args[1];
        HuntLocation location = plugin.getConfigManager().getHuntLocation(id);
        
        if (location == null) {
            sender.sendMessage("§c[Hunt] §7Hunt location '" + id + "' does not exist!");
            return true;
        }

        location.setActive(!location.isActive());
        plugin.getConfigManager().saveHuntLocation(id, location);
        plugin.getHuntManager().reloadHunts();
        
        String status = location.isActive() ? "§aenabled" : "§cdisabled";
        sender.sendMessage("§a[Hunt] §7Hunt location '" + id + "' " + status + "§7!");
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6[ModelHunt] §7Available Commands:");
        
        if (sender.hasPermission("hunt.stats")) {
            sender.sendMessage("§7- §e/hunt stats §8[player] §7- View hunt statistics");
        }
        
        if (sender.hasPermission("hunt.admin.use")) {
            sender.sendMessage("§7- §e/hunt gui §7- Open admin configuration GUI");
        }
        
        if (sender.hasPermission("hunt.admin.create")) {
            sender.sendMessage("§7- §e/hunt create <id> <model> <mob> [rewards...] §7- Create hunt location");
            sender.sendMessage("§7- §e/hunt setpos §7- Set hunt location to current position");
        }
        
        if (sender.hasPermission("hunt.admin.delete")) {
            sender.sendMessage("§7- §e/hunt delete <id> §7- Delete hunt location");
        }
        
        if (sender.hasPermission("hunt.admin.list")) {
            sender.sendMessage("§7- §e/hunt list §7- List all hunt locations");
        }
        
        if (sender.hasPermission("hunt.admin.teleport")) {
            sender.sendMessage("§7- §e/hunt tp <id> §7- Teleport to hunt location");
        }
        
        if (sender.hasPermission("hunt.admin.edit")) {
            sender.sendMessage("§7- §e/hunt toggle <id> §7- Toggle hunt location active state");
        }
        
        if (sender.hasPermission("hunt.admin.reload")) {
            sender.sendMessage("§7- §e/hunt reload §7- Reload plugin configuration");
        }
    }

    private String formatLocation(Location loc) {
        return String.format("%s: %.1f, %.1f, %.1f", 
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("create", "delete", "list", "reload", "tp", "stats", "toggle", "gui", "setpos");
            for (String subCmd : subCommands) {
                if (subCmd.startsWith(args[0].toLowerCase())) {
                    completions.add(subCmd);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("delete") || subCommand.equals("tp") || subCommand.equals("toggle")) {
                // Tab complete hunt location IDs
                for (String locationId : plugin.getConfigManager().getHuntLocations().keySet()) {
                    if (locationId.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(locationId);
                    }
                }
            } else if (subCommand.equals("stats")) {
                // Tab complete player names
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        
        return completions;
    }
}