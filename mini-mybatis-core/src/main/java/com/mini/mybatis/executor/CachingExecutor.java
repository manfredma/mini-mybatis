package com.mini.mybatis.executor;

import com.mini.mybatis.cache.Cache;
import com.mini.mybatis.cache.CacheKey;
import com.mini.mybatis.cache.decorators.TransactionalCacheManager;
import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Caching executor that wraps another executor and adds second-level (namespace/mapper) cache support.
 *
 * Query flow:
 *   1. Check second-level cache (if the MappedStatement has a Cache and useCache=true)
 *   2. Delegate to wrapped executor (which checks first-level cache, then the database)
 *   3. Store result in second-level cache pending buffer
 *
 * The pending buffer is flushed to the real cache only on {@link #commit}.
 */
public class CachingExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(CachingExecutor.class);

    private final Executor delegate;
    private final TransactionalCacheManager tcm = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        Cache cache = ms.getCache();
        if (cache != null && ms.isUseCache()) {
            if (ms.isFlushCacheRequired()) {
                tcm.clear(cache);
            }
            CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
            List<E> list = (List<E>) tcm.getObject(cache, key);
            if (list == null) {
                list = delegate.query(ms, parameter, rowBounds, boundSql);
                tcm.putObject(cache, key, list);
            } else {
                log.debug("Cache hit for statement '{}' (second-level cache)", ms.getId());
            }
            return list;
        }
        return delegate.query(ms, parameter, rowBounds, boundSql);
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        flushCacheIfRequired(ms);
        return delegate.update(ms, parameter);
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        tcm.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                tcm.rollback();
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            if (forceRollback) {
                tcm.rollback();
            } else {
                tcm.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    private CacheKey createCacheKey(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) {
        if (delegate instanceof BaseExecutor) {
            return ((BaseExecutor) delegate).createCacheKey(ms, parameter, rowBounds, boundSql);
        }
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        if (parameter != null) {
            cacheKey.update(parameter);
        }
        return cacheKey;
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            tcm.clear(cache);
        }
    }
}
