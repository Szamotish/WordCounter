package org.example.wordcounter.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.example.wordcounter.WordCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter implements TabCompleter {

    private final WordCounter plugin;

    public CommandTabCompleter(WordCounter plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (command.getName().equalsIgnoreCase("wordcounter")) {
                Collections.addAll(completions,
                        "reload", "getcooldown", "setcooldown",
                        "addword", "removeword", "setmaxwords",
                        "reset", "setscore");
            } else if (command.getName().equalsIgnoreCase("show")) {
                Collections.addAll(completions, "words", "deaths", "off");
            }
            completions.removeIf(s -> !s.toLowerCase().startsWith(args[0].toLowerCase()));
            return completions;
        }

        if (args.length == 2) {
            if (command.getName().equalsIgnoreCase("wordcounter") &&
                    (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("setscore"))) {
                for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
                completions.removeIf(s -> !s.toLowerCase().startsWith(args[1].toLowerCase()));
                return completions;
            }
        }

        return Collections.emptyList();
    }

}
