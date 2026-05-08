package com.mini.mybatis.session;

/**
 * Creates an {@link SqlSession} out of a connection or a DataSource.
 */
public interface SqlSessionFactory {

    SqlSession openSession();

    SqlSession openSession(boolean autoCommit);

    SqlSession openSession(TransactionIsolationLevel level);

    Configuration getConfiguration();
}
