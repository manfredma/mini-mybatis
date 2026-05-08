package com.mini.mybatis.cache.decorators;

import com.mini.mybatis.cache.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages a set of {@link TransactionalCache} instances — one per delegate Cache (i.e., namespace).
 * Ensures that second-level cache writes are committed or rolled back atomically with the transaction.
 */
public class TransactionalCacheManager {

    private final Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();

    public void clear(Cache cache) {
        getTransactionalCache(cache).clear();
    }

    public Object getObject(Cache cache, Object key) {
        return getTransactionalCache(cache).getObject(key);
    }

    public void putObject(Cache cache, Object key, Object value) {
        getTransactionalCache(cache).putObject(key, value);
    }

    public void commit() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.commit();
        }
    }

    public void rollback() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.rollback();
        }
    }

    private TransactionalCache getTransactionalCache(Cache cache) {
        return transactionalCaches.computeIfAbsent(cache, TransactionalCache::new);
    }
}
