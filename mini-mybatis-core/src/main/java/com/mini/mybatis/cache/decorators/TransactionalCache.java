package com.mini.mybatis.cache.decorators;

import com.mini.mybatis.cache.Cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The 2nd level cache transactional buffer.
 *
 * Reads are committed immediately. Writes are held in a temporary buffer
 * and only written to the real cache on {@link #commit()}.
 * On {@link #rollback()} all pending writes are discarded.
 */
public class TransactionalCache implements Cache {

    private final Cache delegate;
    /** entries that were fetched from the real cache; null means "miss that was stored" */
    private final Map<Object, Object> entriesToAddOnCommit;
    /** keys that have been looked up and were a miss — store NULL_PLACEHOLDER on commit */
    private final Set<Object> entriesMissedInCache;
    /** if true, clear the delegate cache on commit */
    private boolean clearOnCommit;

    public TransactionalCache(Cache delegate) {
        this.delegate = delegate;
        this.clearOnCommit = false;
        this.entriesToAddOnCommit = new HashMap<>();
        this.entriesMissedInCache = new HashSet<>();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public Object getObject(Object key) {
        Object object = delegate.getObject(key);
        if (object == null) {
            entriesMissedInCache.add(key);
        }
        if (clearOnCommit) {
            return null;
        }
        return object;
    }

    @Override
    public void putObject(Object key, Object object) {
        entriesToAddOnCommit.put(key, object);
    }

    @Override
    public Object removeObject(Object key) {
        return null;
    }

    @Override
    public void clear() {
        clearOnCommit = true;
        entriesToAddOnCommit.clear();
    }

    public void commit() {
        if (clearOnCommit) {
            delegate.clear();
        }
        flushPendingEntries();
        reset();
    }

    public void rollback() {
        unlockMissedEntries();
        reset();
    }

    private void reset() {
        clearOnCommit = false;
        entriesToAddOnCommit.clear();
        entriesMissedInCache.clear();
    }

    private void flushPendingEntries() {
        for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
            delegate.putObject(entry.getKey(), entry.getValue());
        }
        for (Object entry : entriesMissedInCache) {
            if (!entriesToAddOnCommit.containsKey(entry)) {
                delegate.putObject(entry, null);
            }
        }
    }

    private void unlockMissedEntries() {
        for (Object entry : entriesMissedInCache) {
            delegate.putObject(entry, null);
        }
    }
}
