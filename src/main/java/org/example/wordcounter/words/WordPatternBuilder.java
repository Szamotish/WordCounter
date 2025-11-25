package org.example.wordcounter.words;

import org.example.wordcounter.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WordPatternBuilder {
    private final ConfigManager cfg;

    public WordPatternBuilder(ConfigManager cfg) { this.cfg = cfg; }

    public List<Pattern> buildPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        for (String w : cfg.getTrackedWords()) {
            String escaped = Pattern.quote(w);
            Pattern p = cfg.isMatchWholeWord()
                    ? Pattern.compile("\\b" + escaped + "\\b", Pattern.CASE_INSENSITIVE)
                    : Pattern.compile(escaped, Pattern.CASE_INSENSITIVE);
            patterns.add(p);
        }
        return patterns;
    }
}