package org.example.wordcounter.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.wordcounter.WordCounter;
import org.example.wordcounter.config.ConfigManager;
import org.example.wordcounter.preferences.PlayerPreferences;
import org.example.wordcounter.scoreboard.PlayerScoreboard;
import org.example.wordcounter.scoreboard.ScoreboardService;
import org.example.wordcounter.words.WordTracker;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class WordcounterAdminCommand implements CommandExecutor {

    private final WordCounter plugin;
    private final ConfigManager cfg;
    private final WordTracker tracker;
    private final ScoreboardService sbManager;
    private final PlayerPreferences prefs;

    public WordcounterAdminCommand(WordCounter plugin, ConfigManager cfg, WordTracker tracker, ScoreboardService sbManager, PlayerPreferences prefs) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.tracker = tracker;
        this.sbManager = sbManager;
        this.prefs = prefs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wordcounter.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "WordCounter admin commands:");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter reload" + ChatColor.WHITE + " - Reload config");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter getcooldown" + ChatColor.WHITE + " - Show cooldown");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter setcooldown <sec>" + ChatColor.WHITE + " - Set cooldown");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter addword <word>" + ChatColor.WHITE + " - Add tracked word");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter removeword <word>" + ChatColor.WHITE + " - Remove tracked word");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter setmaxwords <n>" + ChatColor.WHITE + " - Max count per message");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter reset <player|all>" + ChatColor.WHITE + " - Reset scores");
            sender.sendMessage(ChatColor.AQUA + "/wordcounter setscore <player> <score>" + ChatColor.WHITE + " - Set player's word score");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload":
                plugin.getConfig().options().copyDefaults(true);
                plugin.reloadConfig();
                cfg.load();
                tracker.rebuild();
                sender.sendMessage(ChatColor.GREEN + "WordCounter config reloaded.");
                break;

            case "getcooldown":
                sender.sendMessage(ChatColor.GREEN + "Cooldown: " + cfg.getCooldownSeconds() + " second(s).");
                break;

            case "setcooldown":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /wordcounter setcooldown <seconds>");
                    break;
                }
                try {
                    int sec = Math.max(0, Integer.parseInt(args[1]));
                    cfg.setCooldownSeconds(sec);
                    sender.sendMessage(ChatColor.GREEN + "Cooldown updated to " + sec + " second(s).");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please enter a valid number.");
                }
                break;

            case "addword":
                if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Usage: /wordcounter addword <word>"); break; }
                String add = args[1].toLowerCase(Locale.ROOT).trim();
                if (add.isEmpty()) { sender.sendMessage(ChatColor.RED + "Word cannot be empty."); break; }
                if (!cfg.getTrackedWords().contains(add)) {
                    cfg.addTrackedWord(add);
                    tracker.rebuild();
                    sender.sendMessage(ChatColor.GREEN + "Added tracked word: " + add);
                } else sender.sendMessage(ChatColor.YELLOW + "That word is already tracked.");
                break;

            case "removeword":
                if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Usage: /wordcounter removeword <word>"); break; }
                String rem = args[1].toLowerCase(Locale.ROOT).trim();
                if (cfg.removeTrackedWord(rem)) {
                    tracker.rebuild();
                    sender.sendMessage(ChatColor.GREEN + "Removed tracked word: " + rem);
                } else sender.sendMessage(ChatColor.YELLOW + "That word was not tracked.");
                break;

            case "setmaxwords":
                if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Usage: /wordcounter setmaxwords <n>"); break; }
                try {
                    int max = Math.max(0, Integer.parseInt(args[1]));
                    cfg.setMaxWordsPerMessage(max);
                    sender.sendMessage(ChatColor.GREEN + "Max words per message set to " + max);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please enter a valid number.");
                }
                break;

            case "reset":
                if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Usage: /wordcounter reset <player|all>"); break; }
                String target = args[1];
                if ("all".equalsIgnoreCase(target)) {
                    sbManager.clearAllScores();
                    sender.sendMessage(ChatColor.GREEN + "All scores have been reset.");
                } else {
                    sbManager.clearPlayerScore(target);
                    sender.sendMessage(ChatColor.GREEN + "Reset score for player: " + target);
                }
                break;

            case "setscore":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /wordcounter setscore <player> <score>");
                    break;
                }
                String targetPlayer = args[1];
                try {
                    int newScore = Math.max(0, Integer.parseInt(args[2]));
                    UUID uuid = sbManager.getDataManager().getUUID(targetPlayer);
                    if (uuid == null) {
                        sender.sendMessage(ChatColor.RED + "Player '" + targetPlayer + "' has never joined the server!");
                        return true;
                    }

                    sbManager.setScore(targetPlayer, newScore);
                    sender.sendMessage(ChatColor.GREEN + "Set score of " + targetPlayer + " to " + newScore + ".");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please enter a valid number.");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /wordcounter for help.");
                break;
        }
        return true;
    }
}
