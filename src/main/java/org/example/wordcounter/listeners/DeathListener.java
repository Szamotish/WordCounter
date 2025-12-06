package org.example.wordcounter.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.example.wordcounter.WordCounter;
import org.example.wordcounter.scoreboard.ScoreboardService;

import java.util.UUID;

public class DeathListener implements Listener {
    private final WordCounter plugin;
    private final ScoreboardService sb;

    public DeathListener(WordCounter plugin, ScoreboardService sb) {
        this.plugin = plugin;
        this.sb = sb;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();

        // increment plugin data
        plugin.getDataManager().addDeath(uuid, 1);

        // push updated value to all scoreboards
        int newDeaths = plugin.getDataManager().getDeathCount(uuid);
        sb.setDeathScore(uuid, newDeaths);
    }
}
