package com.modelhunt.listeners;

import com.modelhunt.ModelHuntPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatInputListener implements Listener {
    private final ModelHuntPlugin plugin;
    private final Map<UUID, InputSession> inputSessions = new ConcurrentHashMap<>();

    public ChatInputListener(ModelHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        InputSession session = inputSessions.get(player.getUniqueId());
        
        if (session == null) return;
        
        event.setCancelled(true);
        String input = event.getMessage().trim();
        
        // Handle cancellation
        if (input.equalsIgnoreCase("cancel")) {
            inputSessions.remove(player.getUniqueId());
            player.sendMessage("§e[Hunt] §7Input cancelled.");
            
            // Return to GUI
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getHuntConfigGUI().openCreationWizard(player);
                }
            }.runTask(plugin);
            return;
        }
        
        // Process input based on session type
        switch (session.getType()) {
            case MODEL_ID:
                handleModelIdInput(player, input);
                break;
            case MYTHIC_MOB:
                handleMythicMobInput(player, input);
                break;
            case REWARDS:
                handleRewardsInput(player, input);
                break;
            case MESSAGES:
                handleMessagesInput(player, input, session);
                break;
            case ADVANCED:
                handleAdvancedInput(player, input, session);
                break;
            case LOCATION_ID:
                handleLocationIdInput(player, input);
                break;
        }
    }

    public void startModelIdInput(Player player) {
        inputSessions.put(player.getUniqueId(), new InputSession(InputType.MODEL_ID));
        player.sendMessage("§6[Hunt] §7Enter the ModelEngine model ID:");
        player.sendMessage("§7Format: §bmodelengine:model_name §7or just §bmodel_name");
        player.sendMessage("§7Type §ccancel §7to abort.");
        
        startTimeout(player);
    }

    public void startMythicMobInput(Player player) {
        inputSessions.put(player.getUniqueId(), new InputSession(InputType.MYTHIC_MOB));
        player.sendMessage("§6[Hunt] §7Enter the MythicMobs mob type:");
        player.sendMessage("§7Format: §dmythicmobs:mob_name §7or just §dmob_name");
        player.sendMessage("§7Type §ccancel §7to abort.");
        
        startTimeout(player);
    }

    public void startRewardsInput(Player player) {
        inputSessions.put(player.getUniqueId(), new InputSession(InputType.REWARDS));
        player.sendMessage("§6[Hunt] §7Enter rewards (one per line, or all on one line separated by spaces):");
        player.sendMessage("§7Examples:");
        player.sendMessage("§7- §eitem:DIAMOND*3");
        player.sendMessage("§7- §eexp:100");
        player.sendMessage("§7- §emoney:50.0");
        player.sendMessage("§7- §ecommand:give {player} special_item");
        player.sendMessage("§7Type §ccancel §7to abort.");
        
        startTimeout(player);
    }

    public void startMessagesInput(Player player) {
        InputSession session = new InputSession(InputType.MESSAGES);
        session.setStep(0); // Start with click message
        inputSessions.put(player.getUniqueId(), session);
        
        player.sendMessage("§6[Hunt] §7Enter the click message (shown when player clicks the model):");
        player.sendMessage("§7Use {player} for player name placeholder");
        player.sendMessage("§7Type §ccancel §7to abort.");
        
        startTimeout(player);
    }

    public void startAdvancedInput(Player player) {
        InputSession session = new InputSession(InputType.ADVANCED);
        session.setStep(0); // Start with respawn time
        inputSessions.put(player.getUniqueId(), session);
        
        player.sendMessage("§6[Hunt] §7Enter respawn time in seconds (default: 300):");
        player.sendMessage("§7This is how long before the model respawns after being clicked");
        player.sendMessage("§7Type §ccancel §7to abort.");
        
        startTimeout(player);
    }

    public void startLocationIdInput(Player player) {
        inputSessions.put(player.getUniqueId(), new InputSession(InputType.LOCATION_ID));
        player.sendMessage("§6[Hunt] §7Enter a unique ID for this hunt location:");
        player.sendMessage("§7Use only letters, numbers, and underscores");
        player.sendMessage("§7Type §ccancel §7to abort.");
        
        startTimeout(player);
    }

    private void handleModelIdInput(Player player, String input) {
        String modelId = parseModelId(input);
        plugin.getLocationCreationManager().setModelId(player, modelId);
        
        inputSessions.remove(player.getUniqueId());
        
        // Return to GUI
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getHuntConfigGUI().openCreationWizard(player);
            }
        }.runTask(plugin);
    }

    private void handleMythicMobInput(Player player, String input) {
        String mobType = parseMythicMobType(input);
        plugin.getLocationCreationManager().setMythicMobType(player, mobType);
        
        inputSessions.remove(player.getUniqueId());
        
        // Return to GUI
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getHuntConfigGUI().openCreationWizard(player);
            }
        }.runTask(plugin);
    }

    private void handleRewardsInput(Player player, String input) {
        List<String> rewards = parseRewards(input);
        plugin.getLocationCreationManager().setRewards(player, rewards);
        
        inputSessions.remove(player.getUniqueId());
        
        // Return to GUI
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getHuntConfigGUI().openCreationWizard(player);
            }
        }.runTask(plugin);
    }

    private void handleMessagesInput(Player player, String input, InputSession session) {
        if (session.getStep() == 0) {
            // Click message
            session.setClickMessage(input);
            session.setStep(1);
            player.sendMessage("§a[Hunt] §7Click message set!");
            player.sendMessage("§6[Hunt] §7Now enter the completion message (shown after rewards are given):");
        } else {
            // Completion message
            plugin.getLocationCreationManager().setMessages(player, session.getClickMessage(), input);
            inputSessions.remove(player.getUniqueId());
            
            // Return to GUI
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getHuntConfigGUI().openCreationWizard(player);
                }
            }.runTask(plugin);
        }
    }

    private void handleAdvancedInput(Player player, String input, InputSession session) {
        if (session.getStep() == 0) {
            // Respawn time
            try {
                int respawnTime = Integer.parseInt(input);
                if (respawnTime < 1) {
                    player.sendMessage("§c[Hunt] §7Respawn time must be at least 1 second!");
                    return;
                }
                session.setRespawnTime(respawnTime);
                session.setStep(1);
                player.sendMessage("§a[Hunt] §7Respawn time set to " + respawnTime + " seconds!");
                player.sendMessage("§6[Hunt] §7Now enter click radius in blocks (default: 3.0):");
            } catch (NumberFormatException e) {
                player.sendMessage("§c[Hunt] §7Please enter a valid number for respawn time!");
            }
        } else {
            // Click radius
            try {
                double clickRadius = Double.parseDouble(input);
                if (clickRadius <= 0) {
                    player.sendMessage("§c[Hunt] §7Click radius must be greater than 0!");
                    return;
                }
                plugin.getLocationCreationManager().setAdvancedSettings(player, session.getRespawnTime(), clickRadius);
                inputSessions.remove(player.getUniqueId());
                
                // Return to GUI
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getHuntConfigGUI().openCreationWizard(player);
                    }
                }.runTask(plugin);
            } catch (NumberFormatException e) {
                player.sendMessage("§c[Hunt] §7Please enter a valid number for click radius!");
            }
        }
    }

    private void handleLocationIdInput(Player player, String input) {
        if (!input.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage("§c[Hunt] §7Location ID can only contain letters, numbers, and underscores!");
            return;
        }

        boolean success = plugin.getLocationCreationManager().createHuntLocation(player, input);
        inputSessions.remove(player.getUniqueId());
        
        if (success) {
            // Return to main menu
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getHuntConfigGUI().openMainMenu(player);
                }
            }.runTask(plugin);
        }
    }

    private String parseModelId(String input) {
        if (input.startsWith("modelengine:")) {
            return input.substring(12);
        }
        return input;
    }

    private String parseMythicMobType(String input) {
        if (input.startsWith("mythicmobs:")) {
            return input.substring(11);
        }
        return input;
    }

    private List<String> parseRewards(String input) {
        List<String> rewards = new ArrayList<>();
        String[] parts = input.split("\\s+");
        
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                rewards.add(part.trim());
            }
        }
        
        return rewards;
    }

    private void startTimeout(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                InputSession session = inputSessions.remove(player.getUniqueId());
                if (session != null) {
                    player.sendMessage("§c[Hunt] §7Input timed out. Please try again.");
                }
            }
        }.runTaskLater(plugin, 1200L); // 60 seconds timeout
    }

    // Input session management
    private enum InputType {
        MODEL_ID, MYTHIC_MOB, REWARDS, MESSAGES, ADVANCED, LOCATION_ID
    }

    private static class InputSession {
        private final InputType type;
        private int step = 0;
        private String clickMessage;
        private int respawnTime;

        public InputSession(InputType type) {
            this.type = type;
        }

        public InputType getType() { return type; }
        public int getStep() { return step; }
        public void setStep(int step) { this.step = step; }
        public String getClickMessage() { return clickMessage; }
        public void setClickMessage(String clickMessage) { this.clickMessage = clickMessage; }
        public int getRespawnTime() { return respawnTime; }
        public void setRespawnTime(int respawnTime) { this.respawnTime = respawnTime; }
    }
}