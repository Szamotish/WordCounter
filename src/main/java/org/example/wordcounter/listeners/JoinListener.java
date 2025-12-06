package org.example.wordcounter.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.example.wordcounter.WordCounter;
import org.example.wordcounter.scoreboard.PlayerScoreboard;
import org.example.wordcounter.scoreboard.ScoreboardService;

import java.util.UUID;

public class JoinListener implements Listener {

    private final WordCounter plugin;
    private final ScoreboardService sb;

    public JoinListener(WordCounter plugin, ScoreboardService sb,
                        org.example.wordcounter.preferences.PlayerPreferences prefs,
                        org.example.wordcounter.data.DataManager ignored) {
        this.plugin = plugin;
        this.sb = sb;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        PlayerScoreboard ps = sb.createFor(uuid);

        for (PlayerScoreboard other : sb.getPlayerScoreboards().values()) {
            other.setWordScore(uuid, plugin.getDataManager().getWordCount(uuid), sb.getDisplayName(uuid));
            other.setDeathScore(uuid, plugin.getDataManager().getDeathCount(uuid), sb.getDisplayName(uuid));
        }

        String pref = plugin.getPreferences().get(uuid);
        if (pref == null) pref = "off";

        switch (pref.toLowerCase()) {
            case "words":
                ps.showWords();
                break;
            case "deaths":
                ps.showDeaths();
                break;
            default:
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                return;
        }

        p.setScoreboard(ps.getScoreboard());
    }
}
