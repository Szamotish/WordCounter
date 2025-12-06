package org.example.wordcounter.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.example.wordcounter.WordCounter;
import org.example.wordcounter.config.ConfigManager;
import org.example.wordcounter.data.DataManager;
import org.example.wordcounter.util.ColorUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ScoreboardService {

    private final WordCounter plugin;
    private final ConfigManager cfg;
    private final DataManager data;
    private final Map<UUID, PlayerScoreboard> boards = new ConcurrentHashMap<>();

    public ScoreboardService(WordCounter plugin, ConfigManager cfg, DataManager data) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.data = data;
    }

    public Map<UUID, PlayerScoreboard> getPlayerScoreboards() {
        return boards;
    }

    public PlayerScoreboard getFor(UUID uuid) {
        return boards.get(uuid);
    }

    public PlayerScoreboard createFor(UUID uuid) {
        return boards.computeIfAbsent(uuid, id -> {
            PlayerScoreboard ps = new PlayerScoreboard(cfg);
            loadAllInto(ps);
            return ps;
        });
    }

    public void loadAllInto(PlayerScoreboard ps) {
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
            if (!op.hasPlayedBefore()) continue;
            UUID playerId = op.getUniqueId();
            if (data.getAllDeathCounts().containsKey(playerId)) continue;

            int deaths = 0;
            try {
                if (op.isOnline() && op instanceof Player) {
                    deaths = ((Player) op).getStatistic(Statistic.DEATHS);
                } else {
                    deaths = op.getStatistic(Statistic.DEATHS);
                }
            } catch (Exception ignored) {
            }

            data.setDeathCount(playerId, deaths);
        }

        for (UUID id : data.getAllWordCounts().keySet()) {
            ps.setWordScore(id, data.getWordCount(id), getDisplayName(id));
        }
        for (UUID id : data.getAllDeathCounts().keySet()) {
            ps.setDeathScore(id, data.getDeathCount(id), getDisplayName(id));
        }
    }

    public void updateEntryName(UUID uuid, String oldName, String newName) {
        String display = ColorUtil.translateColors(newName);
        plugin.getLastDisplayNames().put(uuid, display);

        for (PlayerScoreboard ps : boards.values()) {
            ps.setWordScore(uuid, data.getWordCount(uuid), display);
            ps.setDeathScore(uuid, data.getDeathCount(uuid), display);
        }
    }

    public void setWordScore(UUID uuid, int score) {
        data.setWordCount(uuid, score);
        String display = ColorUtil.translateColors(getDisplayName(uuid));

        plugin.getLogger().log(Level.INFO, "setWordScore: uuid={0} entry display={1} score={2}",
                new Object[]{uuid, ChatColor.stripColor(display), score});

        for (PlayerScoreboard ps : boards.values()) {
            ps.setWordScore(uuid, score, display);
        }
    }

    public void incrementWordScore(UUID uuid, int delta) {
        data.addWordCount(uuid, delta);
        setWordScore(uuid, data.getWordCount(uuid));
    }

    public void setDeathScore(UUID uuid, int score) {
        data.setDeathCount(uuid, score);
        String display = ColorUtil.translateColors(getDisplayName(uuid));

        plugin.getLogger().log(Level.INFO, "setDeathScore: uuid={0} display={1} score={2}",
                new Object[]{uuid, ChatColor.stripColor(display), score});

        for (PlayerScoreboard ps : boards.values()) {
            ps.setDeathScore(uuid, score, display);
        }
    }

    public String getDisplayName(UUID uuid) {

        String cached = plugin.getLastDisplayNames().get(uuid);
        if (cached != null) return cached;

        String essNick = getEssentialsNickname(uuid);
        if (essNick != null && !essNick.isEmpty()) {
            String translated = ColorUtil.translateColors(essNick);
            plugin.getLastDisplayNames().put(uuid, translated);
            return translated;
        }

        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            String dn = ColorUtil.translateColors(p.getDisplayName());
            plugin.getLastDisplayNames().put(uuid, dn);
            return dn;
        }

        String stored = data.getNameFromUUID(uuid);
        if (stored != null)
            return ColorUtil.translateColors(stored);

        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        if (op.getName() != null)
            return op.getName();

        return "Unknown";
    }

    public DataManager getDataManager() {
        return data;
    }

    public void clearAllScores() {
        data.clearAllWordCounts();
        data.clearAllDeathCounts();
        boards.values().forEach(PlayerScoreboard::clearAllScores);
    }

    public void clearPlayerScore(UUID uuid) {
        data.setWordCount(uuid, 0);
        data.setDeathCount(uuid, 0);
        String display = getDisplayName(uuid);

        for (PlayerScoreboard ps : boards.values()) {
            ps.setWordScore(uuid, 0, display);
            ps.setDeathScore(uuid, 0, display);
        }
    }

    private String getEssentialsNickname(UUID uuid) {
        Plugin ess = Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null) return null;

        try {
            Object essentials = ess;
            Method getUser = essentials.getClass().getMethod("getUser", UUID.class);
            Object user = getUser.invoke(essentials, uuid);
            if (user == null) return null;

            Method getNick = user.getClass().getMethod("getNickname");
            Object nick = getNick.invoke(user);

            return nick == null ? null : nick.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
