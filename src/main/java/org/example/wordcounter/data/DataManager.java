package org.example.wordcounter.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final JavaPlugin plugin;
    private final Gson gson = new Gson();

    private final File dataFile;
    private final Map<UUID, String> playerPreferences = new HashMap<>();
    private final Map<UUID, Integer> wordCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> deathCounts = new ConcurrentHashMap<>();
    private final Map<String, UUID> nameToUUID = new ConcurrentHashMap<>();

    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.json");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        loadData();
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        if (!dataFile.exists()) return;

        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> json = gson.fromJson(reader, type);
            if (json == null) return;

            Object wcObj = json.get("wordCounts");
            if (wcObj instanceof Map) {
                Map<?, ?> wc = (Map<?, ?>) wcObj;
                wc.forEach((k, v) -> {
                    try {
                        UUID uuid = UUID.fromString(k.toString());
                        int val = ((Number) v).intValue();
                        wordCounts.put(uuid, val);
                        cacheName(uuid);
                    } catch (Exception ignored) {}
                });
            }

            Object dcObj = json.get("deathCounts");
            if (dcObj instanceof Map) {
                Map<?, ?> dc = (Map<?, ?>) dcObj;
                dc.forEach((k, v) -> {
                    try {
                        UUID uuid = UUID.fromString(k.toString());
                        int val = ((Number) v).intValue();
                        deathCounts.put(uuid, val);
                        cacheName(uuid);
                    } catch (Exception ignored) {}
                });
            }

            Object prefsObj = json.get("playerPreferences");
            if (prefsObj instanceof Map) {
                Map<?, ?> prefs = (Map<?, ?>) prefsObj;
                prefs.forEach((k, v) -> {
                    try {
                        UUID uuid = UUID.fromString(k.toString());
                        playerPreferences.put(uuid, v.toString());
                    } catch (Exception ignored) {}
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, Object> json = new HashMap<>();

            Map<String, Integer> counts = new HashMap<>();
            wordCounts.forEach((uuid, count) -> counts.put(uuid.toString(), count));
            json.put("wordCounts", counts);

            Map<String, Integer> deaths = new HashMap<>();
            deathCounts.forEach((uuid, count) -> deaths.put(uuid.toString(), count));
            json.put("deathCounts", deaths);

            Map<String, String> prefs = new HashMap<>();
            playerPreferences.forEach((uuid, pref) -> prefs.put(uuid.toString(), pref));
            json.put("playerPreferences", prefs);

            gson.toJson(json, writer);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getWordCount(UUID uuid) {
        return wordCounts.getOrDefault(uuid, 0);
    }

    public void setWordCount(UUID uuid, int count) {
        wordCounts.put(uuid, count);
        cacheName(uuid);
    }

    public void addWordCount(UUID uuid, int increment) {
        wordCounts.put(uuid, getWordCount(uuid) + increment);
        cacheName(uuid);
    }

    public Map<UUID, Integer> getAllWordCounts() {
        return new HashMap<>(wordCounts);
    }

    public synchronized void clearAllWordCounts() {
        wordCounts.clear();
    }

    public int getDeathCount(UUID uuid) {
        return deathCounts.getOrDefault(uuid, 0);
    }

    public void setDeathCount(UUID uuid, int count) {
        deathCounts.put(uuid, count);
        cacheName(uuid);
    }

    public void addDeath(UUID uuid, int increment) {
        deathCounts.put(uuid, getDeathCount(uuid) + increment);
        cacheName(uuid);
    }

    public Map<UUID, Integer> getAllDeathCounts() {
        return new HashMap<>(deathCounts);
    }

    public synchronized void clearAllDeathCounts() {
        deathCounts.clear();
    }

    public String getPreference(UUID uuid) {
        return playerPreferences.getOrDefault(uuid, "words");
    }

    public void setPreference(UUID uuid, String mode) {
        playerPreferences.put(uuid, mode.toLowerCase());
    }

    private void cacheName(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (p != null && p.getName() != null) {
            nameToUUID.put(p.getName(), uuid);
        }
    }

    public UUID getUUID(String playerName) {
        UUID cached = nameToUUID.get(playerName);
        if (cached != null) return cached;

        OfflinePlayer p = Bukkit.getOfflinePlayerIfCached(playerName);
        if (p != null) {
            UUID uuid = p.getUniqueId();
            nameToUUID.put(playerName, uuid);
            return uuid;
        }

        return null;
    }

    public String getNameFromUUID(UUID uuid) {
        for (Map.Entry<String, UUID> entry : nameToUUID.entrySet()) {
            if (entry.getValue().equals(uuid)) return entry.getKey();
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (p != null && p.hasPlayedBefore()) {
            String name = p.getName();
            if (name != null) {
                nameToUUID.put(name, uuid);
                return name;
            }
        }

        return null;
    }

    public Map<String, UUID> getAllNameUUIDPairs() {
        Map<String, UUID> nameUUID = new HashMap<>();
        for (UUID uuid : getAllWordCounts().keySet()) {
            String name = getNameFromUUID(uuid);
            if (name != null) nameUUID.put(name, uuid);
        }
        return nameUUID;
    }
}
