package org.example.wordcounter.words;

import org.example.wordcounter.config.ConfigManager;
import org.example.wordcounter.data.DataManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordTracker {
    private final ConfigManager cfg;
    private final DataManager dataManager;
    private List<Pattern> patterns;

    public WordTracker(ConfigManager cfg, DataManager dataManager) {
        this.cfg = cfg;
        this.dataManager = dataManager;
        rebuild();
    }

    public synchronized void rebuild() {
        this.patterns = new WordPatternBuilder(cfg).buildPatterns();
    }

    /** Count matches in a message and clamp to maxWordsPerMessage */
    public int countMatches(String message) {
        if (message == null || message.isEmpty()) return 0;
        int count = 0;
        String lower = message.toLowerCase();
        for (Pattern p : patterns) {
            Matcher m = p.matcher(lower);
            while (m.find()) count++;
        }
        int max = cfg.getMaxWordsPerMessage();
        return Math.min(count, Math.max(0, max));
    }

    /** Increment player word count in DataManager */
    public void addWords(String playerName, int amount) {
        dataManager.addWordCount(dataManager.getUUID(playerName), amount);
    }
}
