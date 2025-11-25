package org.example.wordcounter.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.example.wordcounter.preferences.PlayerPreferences;
import org.example.wordcounter.scoreboard.PlayerScoreboard;
import org.example.wordcounter.scoreboard.ScoreboardService;
import org.example.wordcounter.data.DataManager;

import java.util.Map;
import java.util.UUID;

public class JoinListener implements Listener {

    private final ScoreboardService sbManager;
    private final PlayerPreferences prefs;
    private final DataManager dataManager;

    public JoinListener(ScoreboardService sbManager, PlayerPreferences prefs, DataManager dataManager) {
        this.sbManager = sbManager;
        this.prefs = prefs;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String pref = prefs.get(uuid);

        PlayerScoreboard ps = sbManager.createFor(uuid, player.getName());

        for (Map.Entry<UUID, Integer> entry : dataManager.getAllWordCounts().entrySet()) {
            UUID otherUUID = entry.getKey();
            String name = Bukkit.getPlayer(otherUUID) != null
                    ? Bukkit.getPlayer(otherUUID).getName()
                    : otherUUID.toString();
            ps.setWordScore(name, entry.getValue());
        }

        for (PlayerScoreboard otherPs : sbManager.getPlayerScoreboards().values()) {
            if (otherPs != ps) {
                otherPs.setWordScore(player.getName(), dataManager.getWordCount(uuid));
            }
        }

        int deaths = player.getStatistic(Statistic.DEATHS);
        ps.setDeathScore(player.getName(), deaths);

        switch (pref.toLowerCase()) {
            case "words":
                ps.setWordsVisible(true);
                ps.setDeathsVisible(false);
                player.setScoreboard(ps.getScoreboard());
                break;
            case "deaths":
                ps.setWordsVisible(false);
                ps.setDeathsVisible(true);
                player.setScoreboard(ps.getScoreboard());
                break;
            case "off":
            default:
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                break;
        }
    }
}
