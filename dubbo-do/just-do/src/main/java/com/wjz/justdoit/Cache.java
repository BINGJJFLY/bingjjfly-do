package com.wjz.justdoit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cache<C> {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Map<String, C>> cache = new HashMap<>();

    public Map<String, C> get(String k) {
        return read(() -> cache.getOrDefault(k, Collections.EMPTY_MAP));
    }

    public Map<String, C> put(String k) {
        return write(() -> cache.computeIfAbsent(k, key -> new HashMap<>()));
    }

    public void put(String k, String key, C value) {
        write(() -> cache.computeIfAbsent(k, kk -> new HashMap<>()).putIfAbsent(key, value));
    }

    private <V> V read(Callable<V> callable) {
        V value;
        Lock readLock = this.lock.readLock();
        readLock.lock();
        try {
            value = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
        }
        return value;
    }

    private <V> V write(Callable<V> callable) {
        V value;
        Lock writeLock = this.lock.writeLock();
        writeLock.lock();
        try {
            value = callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
        return value;
    }

    private void write(Runnable runnable) {
        write(() -> {
            runnable.run();
            return null;
        });
    }
}
