package com.mini.mybatis.session.defaults;

import com.mini.mybatis.executor.Executor;
import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.RowBounds;
import com.mini.mybatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private static final Logger log = LoggerFactory.getLogger(DefaultSqlSession.class);

    private final Configuration configuration;
    private final Executor executor;
    private final boolean autoCommit;

    public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.autoCommit = autoCommit;
    }

    @Override
    public <T> T selectOne(String statement) {
        return selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        List<T> list = selectList(statement, parameter);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new RuntimeException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        }
        return null;
    }

    @Override
    public <E> List<E> selectList(String statement) {
        return selectList(statement, null);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        MappedStatement ms = configuration.getMappedStatement(statement);
        BoundSql boundSql = ms.getBoundSql(parameter);
        try {
            return executor.query(ms, parameter, RowBounds.DEFAULT, boundSql);
        } catch (SQLException e) {
            throw new RuntimeException("Error querying database. Cause: " + e, e);
        }
    }

    @Override
    public int insert(String statement) {
        return insert(statement, null);
    }

    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public int update(String statement) {
        return update(statement, null);
    }

    @Override
    public int update(String statement, Object parameter) {
        MappedStatement ms = configuration.getMappedStatement(statement);
        try {
            return executor.update(ms, parameter);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating database. Cause: " + e, e);
        }
    }

    @Override
    public int delete(String statement) {
        return delete(statement, null);
    }

    @Override
    public int delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void commit() {
        commit(false);
    }

    @Override
    public void commit(boolean force) {
        try {
            executor.commit(isCommitOrRollbackRequired(force));
        } catch (SQLException e) {
            throw new RuntimeException("Error committing transaction. Cause: " + e, e);
        }
    }

    @Override
    public void rollback() {
        rollback(false);
    }

    @Override
    public void rollback(boolean force) {
        try {
            executor.rollback(isCommitOrRollbackRequired(force));
        } catch (SQLException e) {
            log.warn("Unexpected exception on rolling back.", e);
        }
    }

    @Override
    public void close() {
        executor.close(isCommitOrRollbackRequired(false));
    }

    private boolean isCommitOrRollbackRequired(boolean force) {
        return !autoCommit || force;
    }
}
