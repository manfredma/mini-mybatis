package com.mini.mybatis.executor;

import com.mini.mybatis.executor.statement.PreparedStatementHandler;
import com.mini.mybatis.executor.statement.StatementHandler;
import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.RowBounds;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Simple executor that opens a new PreparedStatement for every query.
 */
public class SimpleExecutor extends BaseExecutor {

    private Connection connection;

    public SimpleExecutor(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) throws SQLException {
        Connection conn = getConnection();
        StatementHandler handler = new PreparedStatementHandler(configuration, ms, parameter, rowBounds, boundSql);
        Statement stmt = handler.prepare(conn);
        handler.parameterize(stmt);
        return handler.query(stmt, rowBounds);
    }

    @Override
    protected int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Connection conn = getConnection();
        BoundSql boundSql = ms.getBoundSql(parameter);
        StatementHandler handler = new PreparedStatementHandler(configuration, ms, parameter, RowBounds.DEFAULT, boundSql);
        Statement stmt = handler.prepare(conn);
        handler.parameterize(stmt);
        return handler.update(stmt);
    }

    @Override
    protected void doCommit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    @Override
    protected void doRollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.rollback();
        }
    }

    @Override
    public void clearLocalCache() {
        // SimpleExecutor has no local cache
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            super.close(forceRollback);
        } finally {
            closeConnection();
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            DataSource dataSource = configuration.getEnvironment().getDataSource();
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        }
        return connection;
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            } finally {
                connection = null;
            }
        }
    }
}
