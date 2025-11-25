package org.example.wordcounter.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;
import org.example.wordcounter.data.DataManager;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A per-player scoreboard holder. Keeps its own Scoreboard and objectives.
 */
public class PlayerScoreboard {

    private final Scoreboard scoreboard;
    private final Objective wordObjective;
    private final Objective deathObjective;
    private final Set<String> trackedEntries = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> wordScores = new ConcurrentHashMap<>();

    public PlayerScoreboard(org.example.wordcounter.config.ConfigManager cfg, String playerName) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        this.wordObjective = scoreboard.registerNewObjective(
                cfg.getObjectiveName(), "dummy",
                ChatColor.translateAlternateColorCodes('&', cfg.getDisplayName())
        );
        this.deathObjective = scoreboard.registerNewObjective(
                "deathCount", Criteria.DEATH_COUNT,
                ChatColor.RED + "Deaths"
        );
    }

    public Scoreboard getScoreboard() { return scoreboard; }

    public void setWordScore(String entry, int score) {
        trackedEntries.add(entry);
        wordScores.put(entry, score);
        wordObjective.getScore(entry).setScore(score);
    }

    public void setDeathScore(String entry, int score) {
        deathObjective.getScore(entry).setScore(score);
    }

    public void setWordsVisible(boolean visible) {
        if (visible) wordObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        else if (wordObjective.getDisplaySlot() == DisplaySlot.SIDEBAR) wordObjective.setDisplaySlot(null);
    }

    public void setDeathsVisible(boolean visible) {
        if (visible) deathObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        else if (deathObjective.getDisplaySlot() == DisplaySlot.SIDEBAR) deathObjective.setDisplaySlot(null);
    }

    public void clearWordScores() {
        for (String entry : trackedEntries) {
            wordObjective.getScore(entry).setScore(0);
        }
        trackedEntries.clear();
        wordScores.clear();
    }

    public Set<String> getTrackedEntries() {
        return trackedEntries;
    }

    public String getNameForUUID(UUID uuid, DataManager dataManager) {
        String cachedName = dataManager.getNameFromUUID(uuid);
        if (trackedEntries.contains(cachedName)) return cachedName;

        if (trackedEntries.contains(uuid.toString())) return uuid.toString();

        return cachedName;
    }

    public int getWordScore(String entry) {
        return wordScores.getOrDefault(entry, 0);
    }
}
