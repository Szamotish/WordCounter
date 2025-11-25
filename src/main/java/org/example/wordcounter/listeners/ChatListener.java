package org.example.wordcounter.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.example.wordcounter.WordCounter;
import org.example.wordcounter.cooldown.CooldownManager;
import org.example.wordcounter.preferences.PlayerPreferences;
import org.example.wordcounter.scoreboard.ScoreboardService;
import org.example.wordcounter.words.WordTracker;
import org.example.wordcounter.config.ConfigManager;

import java.util.UUID;

public class ChatListener implements Listener {
    private final WordCounter plugin;
    private final WordTracker tracker;
    private final CooldownManager cooldowns;
    private final ScoreboardService sbManager;
    private final PlayerPreferences prefs;
    private final ConfigManager cfg;

    public ChatListener(WordCounter plugin, WordTracker tracker, CooldownManager cooldowns, ScoreboardService sbManager, PlayerPreferences prefs) {
        this.plugin = plugin;
        this.tracker = tracker;
        this.cooldowns = cooldowns;
        this.sbManager = sbManager;
        this.prefs = prefs;
        this.cfg = plugin.getConfigManager();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        int count = tracker.countMatches(msg);
        if (count <= 0) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long left = cooldowns.checkAndSet(uuid, cfg.getCooldownSeconds());
        if (left > 0) {
            player.sendMessage(cfg.getCooldownMessage().replace("{time}", String.valueOf(left)));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            sbManager.increment(player.getName(), count);
        });
    }
}