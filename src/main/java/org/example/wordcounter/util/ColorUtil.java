package org.example.wordcounter.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {}

    /**
     * Convert both custom hex tokens (&#RRGGBB) and & color codes into native Minecraft § format.
     * Example: "&#54DAF4Hello &aFriend" -> "§x§5§4§D§A§F§4Hello §aFriend" -> ChatColor translated
     */
    public static String translateColors(String input) {
        if (input == null || input.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}