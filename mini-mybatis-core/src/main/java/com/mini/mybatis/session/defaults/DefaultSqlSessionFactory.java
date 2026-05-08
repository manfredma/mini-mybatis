package com.mini.mybatis.session.defaults;

import com.mini.mybatis.executor.Executor;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.SqlSession;
import com.mini.mybatis.session.SqlSessionFactory;
import com.mini.mybatis.session.TransactionIsolationLevel;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource(false);
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        return openSessionFromDataSource(autoCommit);
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        return openSessionFromDataSource(false);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private SqlSession openSessionFromDataSource(boolean autoCommit) {
        Executor executor = configuration.newExecutor(autoCommit);
        return new DefaultSqlSession(configuration, executor, autoCommit);
    }
}
