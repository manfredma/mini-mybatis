package com.mini.mybatis.executor.statement;

import com.mini.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public interface StatementHandler {

    Statement prepare(Connection connection) throws SQLException;

    void parameterize(Statement statement) throws SQLException;

    <E> List<E> query(Statement statement, RowBounds rowBounds) throws SQLException;

    int update(Statement statement) throws SQLException;
}
