package org.example.wordcounter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.wordcounter.commands.CommandTabCompleter;
import org.example.wordcounter.commands.ShowCommand;
import org.example.wordcounter.commands.WordcounterAdminCommand;
import org.example.wordcounter.config.ConfigManager;
import org.example.wordcounter.cooldown.CooldownManager;
import org.example.wordcounter.data.DataManager;
import org.example.wordcounter.listeners.ChatListener;
import org.example.wordcounter.listeners.DeathListener;
import org.example.wordcounter.listeners.JoinListener;
import org.example.wordcounter.preferences.PlayerPreferences;
import org.example.wordcounter.scoreboard.PlayerScoreboard;
import org.example.wordcounter.scoreboard.ScoreboardService;
import org.example.wordcounter.words.WordTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WordCounter extends JavaPlugin {

    private ConfigManager config;
    private DataManager dataManager;
    private CooldownManager cooldowns;
    private WordTracker wordTracker;
    private ScoreboardService scoreboardManager;
    private PlayerPreferences preferences;

    private final Map<UUID, String> lastDisplayNames = new HashMap<>();

    @Override
    public void onEnable() {

        saveDefaultConfig();

        this.config = new ConfigManager(this);
        this.config.load();

        this.dataManager = new DataManager(this);

        this.cooldowns = new CooldownManager();
        this.wordTracker = new WordTracker(config, dataManager);
        this.preferences = new PlayerPreferences(this);

        this.scoreboardManager = new ScoreboardService(this, config, dataManager);

        Bukkit.getScheduler().runTask(this, () -> {
            for (UUID uuid : dataManager.getAllWordCounts().keySet()) {
                scoreboardManager.createFor(uuid);
            }
        });

        getServer().getPluginManager().registerEvents(
                new DeathListener(this, scoreboardManager),
                this
        );

        getServer().getPluginManager().registerEvents(
                new ChatListener(this, wordTracker, cooldowns, scoreboardManager, preferences),
                this
        );

        getServer().getPluginManager().registerEvents(
                new JoinListener(this, scoreboardManager, preferences, dataManager),
                this
        );

        WordcounterAdminCommand adminCmd =
                new WordcounterAdminCommand(this, config, wordTracker, scoreboardManager, preferences);

        ShowCommand showCmd = new ShowCommand(this, scoreboardManager, preferences);

        CommandTabCompleter tab = new CommandTabCompleter(this);

        getCommand("wordcounter").setExecutor(adminCmd);
        getCommand("wordcounter").setTabCompleter(tab);

        getCommand("show").setExecutor(showCmd);
        getCommand("show").setTabCompleter(tab);

        getLogger().info("WordCounter enabled.");

        Bukkit.getScheduler().runTaskTimer(this, () -> {

            for (Player p : Bukkit.getOnlinePlayers()) {
                UUID uuid = p.getUniqueId();

                String newName = scoreboardManager.getDisplayName(uuid);
                String oldName = lastDisplayNames.get(uuid);

                if (oldName == null || !oldName.equals(newName)) {

                    scoreboardManager.updateEntryName(uuid, oldName, newName);
                    lastDisplayNames.put(uuid, newName);

                    int w = dataManager.getWordCount(uuid);
                    int d = dataManager.getDeathCount(uuid);

                    for (PlayerScoreboard ps : scoreboardManager.getPlayerScoreboards().values()) {
                        ps.setWordScore(uuid, w, newName);
                        ps.setDeathScore(uuid, d, newName);
                    }
                }

                String pref = preferences.get(uuid);
                if (pref == null) pref = "off";

                PlayerScoreboard ps = scoreboardManager.createFor(uuid);

                switch (pref.toLowerCase()) {
                    case "words":
                        ps.showWords();
                        p.setScoreboard(ps.getScoreboard());
                        break;

                    case "deaths":
                        ps.showDeaths();
                        p.setScoreboard(ps.getScoreboard());
                        break;

                    default:
                        ps.hideAll();
                        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }
            }

        }, 40L, 40L);
    }

    @Override
    public void onDisable() {
        dataManager.saveData();
        getLogger().info("WordCounter disabled.");
    }

    public ConfigManager getConfigManager() { return config; }
    public DataManager getDataManager() { return dataManager; }
    public ScoreboardService getScoreboardManager() { return scoreboardManager; }
    public PlayerPreferences getPreferences() { return preferences; }
    public Map<UUID, String> getLastDisplayNames() { return lastDisplayNames; }

    public String getEntryName(Player player) {
        return config.useNicknames() ? player.getDisplayName() : player.getName();
    }
}
