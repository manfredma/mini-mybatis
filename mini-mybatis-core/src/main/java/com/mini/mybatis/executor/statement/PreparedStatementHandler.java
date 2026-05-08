package com.mini.mybatis.executor.statement;

import com.mini.mybatis.executor.resultset.DefaultResultSetHandler;
import com.mini.mybatis.executor.resultset.ResultSetHandler;
import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.mapping.ParameterMapping;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.RowBounds;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class PreparedStatementHandler implements StatementHandler {

    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final Object parameterObject;
    private final RowBounds rowBounds;
    private final BoundSql boundSql;
    private final ResultSetHandler resultSetHandler;

    public PreparedStatementHandler(Configuration configuration, MappedStatement mappedStatement,
                                    Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        this.configuration = configuration;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.rowBounds = rowBounds;
        this.boundSql = boundSql;
        this.resultSetHandler = new DefaultResultSetHandler(mappedStatement, boundSql);
    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        return connection.prepareStatement(boundSql.getSql());
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return;
        }
        Object paramObject = boundSql.getParameterObject();
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            String property = parameterMapping.getProperty();
            Object value = resolveParameterValue(paramObject, property);
            ps.setObject(i + 1, value);
        }
    }

    @Override
    public <E> List<E> query(Statement statement, RowBounds rowBounds) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return resultSetHandler.handleResultSets(ps);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        return ps.getUpdateCount();
    }

    private Object resolveParameterValue(Object paramObject, String property) {
        if (paramObject == null) {
            return null;
        }
        if (paramObject instanceof Map) {
            return ((Map<?, ?>) paramObject).get(property);
        }
        // Try field access via reflection
        try {
            Class<?> clazz = paramObject.getClass();
            Field field = findField(clazz, property);
            if (field != null) {
                field.setAccessible(true);
                return field.get(paramObject);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get value of field '" + property + "' from " + paramObject.getClass(), e);
        }
        // Single primitive parameter case — property name doesn't matter
        return paramObject;
    }

    private Field findField(Class<?> clazz, String fieldName) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // try superclass
            }
        }
        return null;
    }
}
