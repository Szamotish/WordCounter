package org.example.wordcounter;

import org.bukkit.plugin.java.JavaPlugin;
import org.example.wordcounter.config.ConfigManager;
import org.example.wordcounter.cooldown.CooldownManager;
import org.example.wordcounter.data.DataManager;
import org.example.wordcounter.preferences.PlayerPreferences;
import org.example.wordcounter.scoreboard.ScoreboardService;
import org.example.wordcounter.words.WordTracker;
import org.example.wordcounter.listeners.ChatListener;
import org.example.wordcounter.listeners.JoinListener;
import org.example.wordcounter.commands.WordcounterAdminCommand;
import org.example.wordcounter.commands.ShowCommand;
import org.example.wordcounter.commands.CommandTabCompleter;

public final class WordCounter extends JavaPlugin {

    private ConfigManager config;
    private DataManager dataManager;
    private CooldownManager cooldowns;
    private WordTracker wordTracker;
    private ScoreboardService scoreboardManager;
    private PlayerPreferences preferences;

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
        this.scoreboardManager.loadExistingScoresFromMainBoard();

        getServer().getPluginManager().registerEvents(
                new ChatListener(this, wordTracker, cooldowns, scoreboardManager, preferences),
                this
        );
        getServer().getPluginManager().registerEvents(
                new JoinListener(scoreboardManager, preferences, dataManager),
                this
        );

        WordcounterAdminCommand adminCmd = new WordcounterAdminCommand(this, config, wordTracker, scoreboardManager, preferences);
        ShowCommand showCmd = new ShowCommand(this, scoreboardManager, preferences);
        CommandTabCompleter tabCompleter = new CommandTabCompleter(this);

        getCommand("wordcounter").setExecutor(adminCmd);
        getCommand("wordcounter").setTabCompleter(tabCompleter);

        getCommand("show").setExecutor(showCmd);
        getCommand("show").setTabCompleter(tabCompleter);

        getLogger().info("WordCounter enabled. Tracking: " + String.join(", ", config.getTrackedWords()));
    }

    @Override
    public void onDisable() {
        scoreboardManager.persistScoresToMainBoard();
        dataManager.saveData();
        getLogger().info("WordCounter disabled.");
    }

    public ConfigManager getConfigManager() { return config; }
    public DataManager getDataManager() { return dataManager; }
    public ScoreboardService getScoreboardManager() { return scoreboardManager; }
    public PlayerPreferences getPreferences() { return preferences; }
}
