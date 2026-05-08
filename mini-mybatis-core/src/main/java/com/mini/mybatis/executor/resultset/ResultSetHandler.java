package com.mini.mybatis.executor.resultset;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface ResultSetHandler {

    <E> List<E> handleResultSets(PreparedStatement ps) throws SQLException;
}
