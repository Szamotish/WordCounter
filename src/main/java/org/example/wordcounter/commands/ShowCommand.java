package org.example.wordcounter.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.wordcounter.WordCounter;
import org.example.wordcounter.preferences.PlayerPreferences;
import org.example.wordcounter.scoreboard.ScoreboardService;
import org.example.wordcounter.scoreboard.PlayerScoreboard;

import java.util.UUID;

public class ShowCommand implements CommandExecutor {
    private final WordCounter plugin;
    private final ScoreboardService sbManager;
    private final PlayerPreferences prefs;

    public ShowCommand(WordCounter plugin, ScoreboardService sbManager, PlayerPreferences prefs) {
        this.plugin = plugin;
        this.sbManager = sbManager;
        this.prefs = prefs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        if (!p.hasPermission("wordcounter.use")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length < 1) {
            p.sendMessage(ChatColor.YELLOW + "Usage: /show <words|deaths|off>");
            return true;
        }
        String arg = args[0].toLowerCase();
        UUID uuid = p.getUniqueId();

        switch (arg) {
            case "words":
                PlayerScoreboard ps = sbManager.getFor(uuid);
                if (ps == null) ps = sbManager.createFor(uuid, p.getName());
                ps.setWordsVisible(true);
                ps.setDeathsVisible(false);
                p.setScoreboard(ps.getScoreboard());
                prefs.set(uuid, "words");
                p.sendMessage(ChatColor.GREEN + "Now showing: Word Counter");
                break;

            case "deaths":
                PlayerScoreboard ps2 = sbManager.getFor(uuid);
                if (ps2 == null) ps2 = sbManager.createFor(uuid, p.getName());
                ps2.setDeathsVisible(true);
                ps2.setWordsVisible(false);
                p.setScoreboard(ps2.getScoreboard());
                prefs.set(uuid, "deaths");
                p.sendMessage(ChatColor.RED + "Now showing: Death Counter");
                break;

            case "off":
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                prefs.set(uuid, "off");
                p.sendMessage(ChatColor.GRAY + "Scoreboard hidden.");
                break;

            default:
                p.sendMessage(ChatColor.YELLOW + "Usage: /show <words|deaths|off>");
                break;
        }
        return true;
    }
}
