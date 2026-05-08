package com.mini.mybatis.executor;

import com.mini.mybatis.cache.Cache;
import com.mini.mybatis.cache.CacheKey;
import com.mini.mybatis.cache.PerpetualCache;
import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Base executor implementation providing transaction management and local (first-level) cache.
 * Subclasses implement the actual query/update logic.
 */
public abstract class BaseExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(BaseExecutor.class);

    protected Configuration configuration;
    protected boolean closed;

    /** First-level (SqlSession-scoped) cache */
    protected Cache localCache;

    protected BaseExecutor(Configuration configuration) {
        this.configuration = configuration;
        this.closed = false;
        this.localCache = new PerpetualCache("LocalCache");
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        if (closed) {
            throw new IllegalStateException("Executor was closed.");
        }
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, boundSql, key);
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql, CacheKey key) throws SQLException {
        List<E> list = (List<E>) localCache.getObject(key);
        if (list != null) {
            log.debug("Cache hit for statement '{}' (local cache)", ms.getId());
        } else {
            list = doQuery(ms, parameter, rowBounds, boundSql);
            localCache.putObject(key, list);
        }
        return list;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        if (closed) {
            throw new IllegalStateException("Executor was closed.");
        }
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new IllegalStateException("Executor was closed.");
        }
        clearLocalCache();
        if (required) {
            doCommit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if (closed) {
            throw new IllegalStateException("Executor was closed.");
        }
        if (required) {
            clearLocalCache();
            doRollback();
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {
                rollback(forceRollback);
            } finally {
                closed = true;
            }
        } catch (SQLException e) {
            log.warn("Unexpected exception on closing executor.", e);
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
        }
    }

    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        if (parameterObject != null) {
            cacheKey.update(parameterObject);
        }
        return cacheKey;
    }

    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException;

    protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;

    protected abstract void doCommit() throws SQLException;

    protected abstract void doRollback() throws SQLException;
}
