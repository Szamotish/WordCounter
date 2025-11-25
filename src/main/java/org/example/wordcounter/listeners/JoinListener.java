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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        String pref = prefs.get(uuid);

        PlayerScoreboard ps = sbManager.getFor(uuid);
        if (ps == null) {
            ps = sbManager.createFor(uuid, playerName);
        }

        sbManager.loadExistingScoresForPlayer(uuid);

        for (Map.Entry<UUID, Integer> entry : dataManager.getAllWordCounts().entrySet()) {
            String otherName = dataManager.getNameFromUUID(entry.getKey());
            if (otherName != null) {
                Player otherPlayer = Bukkit.getPlayer(entry.getKey());
                int deaths = otherPlayer != null ? otherPlayer.getStatistic(Statistic.DEATHS) : 0;
                ps.setDeathScore(otherName, deaths);
            }
        }

        int playerDeaths = player.getStatistic(Statistic.DEATHS);
        for (PlayerScoreboard otherPs : sbManager.getPlayerScoreboards().values()) {
            if (otherPs != ps) {
                otherPs.setWordScore(playerName, dataManager.getWordCount(uuid));
                otherPs.setDeathScore(playerName, playerDeaths);
            }
        }

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
