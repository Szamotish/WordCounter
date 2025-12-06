package org.example.wordcounter.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerScoreboard {

    private final Scoreboard board;
    private final Objective wordObj;
    private final Objective deathObj;

    private final Map<UUID, String> entryMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> wordScores = new ConcurrentHashMap<>();
    private final Map<String, Integer> deathScores = new ConcurrentHashMap<>();

    public PlayerScoreboard(org.example.wordcounter.config.ConfigManager cfg) {
        board = Bukkit.getScoreboardManager().getNewScoreboard();

        board.getEntries().forEach(entry -> {
            Team t = board.getEntryTeam(entry);
            if (t != null) t.unregister();
            board.resetScores(entry);
        });

        wordObj = board.registerNewObjective(
                cfg.getObjectiveName(),
                "dummy",
                ChatColor.translateAlternateColorCodes('&', cfg.getDisplayName())
        );

        deathObj = board.registerNewObjective(
                "deathCount",
                "dummy",
                ChatColor.RED + "Deaths"
        );
    }

    public Scoreboard getScoreboard() { return board; }

    public Objective getWordObjective() { return wordObj; }
    public Objective getDeathObjective() { return deathObj; }

    public void setWordScore(UUID uuid, int score, String display) {
        String entry = getOrCreateEntry(uuid);
        wordScores.put(entry, score);
        setScore(entry, score, display, wordObj);
    }

    public void setDeathScore(UUID uuid, int score, String display) {
        String entry = getOrCreateEntry(uuid);
        deathScores.put(entry, score);
        setScore(entry, score, display, deathObj);
    }

    private void setScore(String entry, int score, String display, Objective obj) {
        if (entry == null) return;

        Team t = getOrCreateTeam(entry);

        String[] parts = splitDisplay(display);
        t.setPrefix(parts[0]);
        t.setSuffix(parts[1]);

        obj.getScore(entry).setScore(score);
    }

    private String getOrCreateEntry(UUID uuid) {
        return entryMap.computeIfAbsent(uuid, id -> makeHiddenEntry(uuid));
    }

    private String makeHiddenEntry(UUID uuid) {
        String raw = uuid.toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8 && i < raw.length(); i++) {
            char ch = raw.charAt(i);
            sb.append('\u00A7').append(ch);
        }
        return sb.toString();
    }

    private Team getOrCreateTeam(String entry) {
        String id = "t_" + entry.replace("§", "");
        if (id.length() > 16) id = id.substring(0, 16);

        Team t = board.getTeam(id);
        if (t == null) t = board.registerNewTeam(id);
        if (!t.hasEntry(entry)) t.addEntry(entry);

        t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        return t;
    }

    private String[] splitDisplay(String text) {
        if (text == null) text = "";
        String raw = ChatColor.translateAlternateColorCodes('&', text);

        String prefix = "";
        String suffix = "";

        int visible = 0;
        StringBuilder sb = new StringBuilder();

        char[] arr = raw.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];

            if (c == '§') {

                if (i + 13 <= arr.length
                        && arr[i + 1] == 'x'
                        && arr[i + 2] == '§') {

                    String hex = raw.substring(i, i + 14);
                    sb.append(hex);
                    i += 13;
                    continue;
                }

                if (i + 1 < arr.length) {
                    sb.append(c).append(arr[i + 1]);
                    i++;
                    continue;
                }
            }

            if (visible < 16) {
                sb.append(c);
                visible++;
            } else {
                suffix = raw.substring(i);
                break;
            }
        }

        prefix = sb.toString();

        String lastColors = ChatColor.getLastColors(prefix);
        if (!suffix.isEmpty() && !lastColors.isEmpty()) {
            suffix = lastColors + suffix;
        }

        prefix = trimVisibleHexSafe(prefix, 16);
        suffix = trimVisibleHexSafe(suffix, 16);

        return new String[]{prefix, suffix};
    }

    private String trimVisibleHexSafe(String input, int max) {
        int visible = 0;
        StringBuilder out = new StringBuilder();

        char[] arr = input.toCharArray();
        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];

            if (c == '§') {
                if (i + 13 <= arr.length && arr[i + 1] == 'x' && arr[i + 2] == '§') {
                    out.append(input, i, i + 14);
                    i += 13;
                    continue;
                }

                if (i + 1 < arr.length) {
                    out.append(c).append(arr[i + 1]);
                    i++;
                    continue;
                }
            }

            if (visible < max) {
                out.append(c);
                visible++;
            } else break;
        }

        return out.toString();
    }

    public void showWords() {
        wordObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        deathObj.setDisplaySlot(null);
    }

    public void showDeaths() {
        deathObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        wordObj.setDisplaySlot(null);
    }

    public void hideAll() {
        wordObj.setDisplaySlot(null);
        deathObj.setDisplaySlot(null);
    }

    public void clearAllScores() {
        wordScores.clear();
        deathScores.clear();
    }
}
