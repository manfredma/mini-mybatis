package com.mini.mybatis.session;

import java.io.Closeable;
import java.util.List;

/**
 * The primary Java interface for working with MyBatis.
 * Through this interface you can execute commands, get mappers and manage transactions.
 */
public interface SqlSession extends Closeable {

    <T> T selectOne(String statement);

    <T> T selectOne(String statement, Object parameter);

    <E> List<E> selectList(String statement);

    <E> List<E> selectList(String statement, Object parameter);

    int insert(String statement);

    int insert(String statement, Object parameter);

    int update(String statement);

    int update(String statement, Object parameter);

    int delete(String statement);

    int delete(String statement, Object parameter);

    <T> T getMapper(Class<T> type);

    Configuration getConfiguration();

    void commit();

    void commit(boolean force);

    void rollback();

    void rollback(boolean force);

    void close();
}
