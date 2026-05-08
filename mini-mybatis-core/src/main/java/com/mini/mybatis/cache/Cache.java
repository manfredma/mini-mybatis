package com.mini.mybatis.cache;

/**
 * Cache interface for both first-level (session) and second-level (global) caches.
 */
public interface Cache {

    String getId();

    void putObject(Object key, Object value);

    Object getObject(Object key);

    Object removeObject(Object key);

    void clear();

    int getSize();
}
