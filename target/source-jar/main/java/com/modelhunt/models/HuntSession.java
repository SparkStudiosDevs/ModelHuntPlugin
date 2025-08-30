package com.modelhunt.models;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HuntSession {
    private final UUID playerId;
    private final Map<String, Long> lastClickTimes;
    private final Map<String, Integer> clickCounts;
    private long sessionStartTime;

    public HuntSession(UUID playerId) {
        this.playerId = playerId;
        this.lastClickTimes = new HashMap<>();
        this.clickCounts = new HashMap<>();
        this.sessionStartTime = System.currentTimeMillis();
    }

    public boolean canClick(String locationId, int cooldownSeconds) {
        Long lastClick = lastClickTimes.get(locationId);
        if (lastClick == null) {
            return true;
        }
        
        long timeSinceLastClick = (System.currentTimeMillis() - lastClick) / 1000;
        return timeSinceLastClick >= cooldownSeconds;
    }

    public void recordClick(String locationId) {
        lastClickTimes.put(locationId, System.currentTimeMillis());
        clickCounts.put(locationId, clickCounts.getOrDefault(locationId, 0) + 1);
    }

    public int getClickCount(String locationId) {
        return clickCounts.getOrDefault(locationId, 0);
    }

    public long getTimeUntilNextClick(String locationId, int cooldownSeconds) {
        Long lastClick = lastClickTimes.get(locationId);
        if (lastClick == null) {
            return 0;
        }
        
        long timeSinceLastClick = (System.currentTimeMillis() - lastClick) / 1000;
        return Math.max(0, cooldownSeconds - timeSinceLastClick);
    }

    public int getTotalClicks() {
        return clickCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    // Getters
    public UUID getPlayerId() { return playerId; }
    public long getSessionStartTime() { return sessionStartTime; }
}