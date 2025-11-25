package org.example.wordcounter.preferences;

import org.example.wordcounter.WordCounter;
import org.example.wordcounter.data.DataManager;

import java.util.UUID;

public class PlayerPreferences {

    private final DataManager dataManager;

    public PlayerPreferences(WordCounter plugin) {
        this.dataManager = plugin.getDataManager();
    }

    public String get(UUID uuid) {
        return dataManager.getPreference(uuid);
    }

    public void set(UUID uuid, String mode) {
        dataManager.setPreference(uuid, mode.toLowerCase());
    }
}
