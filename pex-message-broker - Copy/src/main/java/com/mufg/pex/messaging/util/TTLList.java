package com.mufg.pex.messaging.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TTLList<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final Map<K, Long> timestamps = new HashMap<>();
    private final long ttl; // Time-To-Live in milliseconds
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TTLList(long ttl) {
        this.ttl = ttl;
        startCleanupTask();
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
        timestamps.put(key, System.currentTimeMillis());
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    private void startCleanupTask() {
        executor.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            synchronized (TTLList.this) {
                Iterator<Map.Entry<K, Long>> iterator = timestamps.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<K, Long> entry = iterator.next();
                    if (now - entry.getValue() >= ttl) {
                        map.remove(entry.getKey());
                        iterator.remove();
                    }
                }
            }
        }, ttl, ttl, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        executor.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        TTLList<String, String> ttlList = new TTLList<>(5000); // TTL of 5 seconds
        ttlList.put("key1", "value1");
        ttlList.put("key2", "value2");

        System.out.println("Initial list: " + ttlList.map);

        // Simulate some delay
        Thread.sleep(6000);

        System.out.println("List after 6 seconds: " + ttlList.map);

        ttlList.stop();
    }

    public int size() {
        return map.size();
    }

    public List<K> getKeys() {

        return map.keySet().stream().toList();
    }
}
