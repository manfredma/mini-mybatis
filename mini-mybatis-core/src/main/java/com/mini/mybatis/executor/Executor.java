package com.mini.mybatis.executor;

import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

/**
 * Executor is the core component that executes SQL statements.
 */
public interface Executor {

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException;

    int update(MappedStatement ms, Object parameter) throws SQLException;

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    void close(boolean forceRollback);

    boolean isClosed();

    void clearLocalCache();
}
