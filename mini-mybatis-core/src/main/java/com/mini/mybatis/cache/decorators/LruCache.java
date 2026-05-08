package com.mini.mybatis.cache.decorators;

import com.mini.mybatis.cache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU (Least Recently Used) cache decorator. Evicts the least recently used entry
 * when the cache exceeds its configured size.
 */
public class LruCache implements Cache {

    private final Cache delegate;
    private Map<Object, Object> keyMap;
    private Object eldestKey;

    public LruCache(Cache delegate) {
        this(delegate, 1024);
    }

    public LruCache(Cache delegate, int size) {
        this.delegate = delegate;
        setSize(size);
    }

    public void setSize(final int size) {
        keyMap = new LinkedHashMap<Object, Object>(size, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                boolean tooBig = size() > size;
                if (tooBig) {
                    eldestKey = eldest.getKey();
                }
                return tooBig;
            }
        };
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        delegate.putObject(key, value);
        cycleKeyList(key);
    }

    @Override
    public Object getObject(Object key) {
        keyMap.get(key); // touch to update LRU order
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyMap.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    private void cycleKeyList(Object key) {
        keyMap.put(key, key);
        if (eldestKey != null) {
            delegate.removeObject(eldestKey);
            eldestKey = null;
        }
    }
}
