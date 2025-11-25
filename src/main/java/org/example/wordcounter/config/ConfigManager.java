package org.example.wordcounter.config;

import org.example.wordcounter.WordCounter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    private final WordCounter plugin;
    private FileConfiguration cfg;
    private List<String> trackedWords = new ArrayList<>();
    private boolean matchWholeWord = true;
    private String objectiveName = "wordCount";
    private String displayName = "&6Word Count";
    private int maxWordsPerMessage = 2;
    private int cooldownSeconds = 10;
    private String cooldownMessage = "&cYou must wait &e{time}&c more seconds before earning more points!";

    public ConfigManager(WordCounter plugin) {
        this.plugin = plugin;
    }

    public void load() {
        cfg = plugin.getConfig();

        List<String> cfgWords = cfg.getStringList("tracked-words");
        if (cfgWords == null || cfgWords.isEmpty()) {
            cfgWords = List.of("apple", "orange");
            cfg.set("tracked-words", cfgWords);
            plugin.saveConfig();
        }

        trackedWords = cfgWords.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase(Locale.ROOT).trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        matchWholeWord = cfg.getBoolean("match-whole-word", true);
        objectiveName = cfg.getString("objective-name", "wordCount");
        displayName = cfg.getString("display-name", "&6Word Count");
        maxWordsPerMessage = cfg.getInt("max-words-per-message", 2);
        cooldownSeconds = Math.max(0, cfg.getInt("cooldown-seconds", 10));
        cooldownMessage = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("cooldown-message", cooldownMessage));
    }

    public List<String> getTrackedWords() { return List.copyOf(trackedWords); }
    public void addTrackedWord(String w) {
        trackedWords.add(w);
        plugin.getConfig().set("tracked-words", trackedWords);
        plugin.saveConfig();
    }
    public boolean removeTrackedWord(String w) {
        boolean removed = trackedWords.remove(w);
        if (removed) {
            plugin.getConfig().set("tracked-words", trackedWords);
            plugin.saveConfig();
        }
        return removed;
    }

    public boolean isMatchWholeWord() { return matchWholeWord; }
    public String getObjectiveName() { return objectiveName; }
    public String getDisplayName() { return displayName; }
    public int getMaxWordsPerMessage() { return maxWordsPerMessage; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public String getCooldownMessage() { return cooldownMessage; }

    public void setCooldownSeconds(int sec) {
        cooldownSeconds = Math.max(0, sec);
        plugin.getConfig().set("cooldown-seconds", cooldownSeconds);
        plugin.saveConfig();
    }

    public void setMaxWordsPerMessage(int max) {
        maxWordsPerMessage = Math.max(0, max);
        plugin.getConfig().set("max-words-per-message", maxWordsPerMessage);
        plugin.saveConfig();
    }
}