package org.example.wordcounter.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.example.wordcounter.WordCounter;
import org.example.wordcounter.config.ConfigManager;
import org.example.wordcounter.data.DataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class ScoreboardService {

    private final WordCounter plugin;
    private final ConfigManager cfg;
    private final DataManager dataManager;

    private final Map<UUID, PlayerScoreboard> playerScoreboards = new ConcurrentHashMap<>();

    public ScoreboardService(WordCounter plugin, ConfigManager cfg, DataManager dataManager) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.dataManager = dataManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public Map<UUID, PlayerScoreboard> getPlayerScoreboards() {
        return playerScoreboards;
    }

    public PlayerScoreboard createFor(UUID uuid, String playerName) {
        PlayerScoreboard ps = new PlayerScoreboard(cfg, playerName);
        int count = dataManager.getWordCount(uuid);
        ps.setWordScore(playerName, count);
        playerScoreboards.put(uuid, ps);
        return ps;
    }

    public PlayerScoreboard getFor(UUID uuid) {
        return playerScoreboards.get(uuid);
    }

    public void removeFor(UUID uuid) {
        playerScoreboards.remove(uuid);
    }

    public void loadExistingScoresFromMainBoard() {
        for (Map.Entry<UUID, Integer> entry : dataManager.getAllWordCounts().entrySet()) {
            String name = dataManager.getNameFromUUID(entry.getKey());
            int score = entry.getValue();

            if (name != null) {
                for (PlayerScoreboard ps : playerScoreboards.values()) {
                    ps.setWordScore(name, score);
                }
            }
        }
    }

    public void persistScoresToMainBoard() {
        for (Map.Entry<UUID, PlayerScoreboard> entry : playerScoreboards.entrySet()) {
            PlayerScoreboard ps = entry.getValue();
            for (String playerName : ps.getTrackedEntries()) {
                int score = ps.getWordScore(playerName);
                dataManager.setWordCount(dataManager.getUUID(playerName), score);
            }
        }
        dataManager.saveData();
    }
    public synchronized void setScore(String targetPlayer, int newScore) {
        UUID uuid = dataManager.getUUID(targetPlayer);

        if (uuid == null) {
            return;
        }
        dataManager.setWordCount(uuid, newScore);

        String displayName = dataManager.getNameFromUUID(uuid);
        if (displayName != null) {
            for (PlayerScoreboard ps : playerScoreboards.values()) {
                ps.setWordScore(displayName, newScore);
            }
        }
    }

    public synchronized void increment(String playerName, int delta) {
        UUID uuid = dataManager.getUUID(playerName);
        dataManager.addWordCount(uuid, delta);

        int newVal = dataManager.getWordCount(uuid);

        String displayName = dataManager.getNameFromUUID(uuid);
        if (displayName != null) {
            for (PlayerScoreboard ps : playerScoreboards.values()) {
                ps.setWordScore(displayName, newVal);
            }
        }
    }

    public void clearAllScores() {
        dataManager.clearAllWordCounts();
        for (PlayerScoreboard ps : playerScoreboards.values()) {
            if (ps != null) ps.clearWordScores();
        }
    }

    public void clearPlayerScore(String playerName) {
        UUID uuid = dataManager.getUUID(playerName);
        String displayName = dataManager.getNameFromUUID(uuid);
        if (displayName != null) {
            for (PlayerScoreboard ps : playerScoreboards.values()) {
                ps.setWordScore(displayName, 0);
            }
        }
    }

    public void cleanupInvalidUUIDs() {
        Map<UUID, Integer> map = dataManager.getAllWordCounts();

        List<UUID> toRemove = new ArrayList<>();

        for (UUID uuid : map.keySet()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);

            if (op.getName() == null) {
                toRemove.add(uuid);
            }
        }

        for (UUID uuid : toRemove) {
            map.remove(uuid);
            plugin.getLogger().info("Removed invalid UUID from data: " + uuid);
        }
    }

    public void loadExistingScoresForPlayer(UUID playerUUID) {
        PlayerScoreboard ps = playerScoreboards.get(playerUUID);
        if (ps == null) return;

        for (Map.Entry<UUID, Integer> entry : dataManager.getAllWordCounts().entrySet()) {
            UUID otherUUID = entry.getKey();
            String name = dataManager.getNameFromUUID(otherUUID);
            if (name == null) continue;

            ps.setWordScore(name, entry.getValue());

            int deaths = 0;
            if (Bukkit.getPlayer(otherUUID) != null) {
                deaths = Bukkit.getPlayer(otherUUID).getStatistic(Statistic.DEATHS);
            }
            ps.setDeathScore(name, deaths);
        }
    }
}
