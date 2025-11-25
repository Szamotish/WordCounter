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
    private final Map<String, UUID> nameToUUID = new HashMap<>();

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

            Map<String, Double> counts = (Map<String, Double>) json.get("wordCounts");
            if (counts != null) {
                counts.forEach((k, v) -> {
                    UUID uuid = UUID.fromString(k);
                    wordCounts.put(uuid, v.intValue());
                });
            }

            Map<String, String> prefs = (Map<String, String>) json.get("playerPreferences");
            if (prefs != null) {
                prefs.forEach((k, v) -> playerPreferences.put(UUID.fromString(k), v));
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

    public String getPreference(UUID uuid) {
        return playerPreferences.getOrDefault(uuid, "words");
    }

    public void setPreference(UUID uuid, String mode) {
        playerPreferences.put(uuid, mode.toLowerCase());
    }

    private void cacheName(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (p != null) nameToUUID.put(p.getName(), uuid);
    }

    public UUID getUUID(String playerName) {
        UUID uuid = nameToUUID.get(playerName);
        if (uuid != null) return uuid;

        OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
        if (p != null && p.hasPlayedBefore()) {
            uuid = p.getUniqueId();
            nameToUUID.put(playerName, uuid);
            return uuid;
        }

        return UUID.nameUUIDFromBytes(playerName.getBytes());
    }

    public String getNameFromUUID(UUID uuid) {
        for (Map.Entry<String, UUID> entry : nameToUUID.entrySet()) {
            if (entry.getValue().equals(uuid)) return entry.getKey();
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        if (p != null && p.hasPlayedBefore()) {
            String name = p.getName();
            if (name != null) nameToUUID.put(name, uuid);
            return name;
        }

        return uuid.toString();
    }

    public synchronized void clearAllWordCounts() {
        wordCounts.clear();
    }
}
