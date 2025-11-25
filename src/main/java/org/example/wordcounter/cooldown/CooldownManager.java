package org.example.wordcounter.cooldown;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    private final Map<UUID, Long> last = new ConcurrentHashMap<>();

    /**
     * Check whether player can earn points. If allowed, record timestamp and return 0.
     * If not allowed, return remaining seconds (rounded up).
     */
    public long checkAndSet(UUID uuid, long cooldownSeconds) {
        long now = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000L;
        Long prev = last.get(uuid);
        if (prev == null || now - prev >= cooldownMillis) {
            last.put(uuid, now);
            return 0L;
        } else {
            long left = (cooldownMillis - (now - prev) + 999) / 1000;
            return left;
        }
    }

    public void reset(UUID uuid) { last.remove(uuid); }
}