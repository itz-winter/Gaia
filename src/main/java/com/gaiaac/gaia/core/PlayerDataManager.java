package com.gaiaac.gaia.core;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(player));
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return playerDataMap;
    }

    public void cleanup() {
        playerDataMap.clear();
    }
}
