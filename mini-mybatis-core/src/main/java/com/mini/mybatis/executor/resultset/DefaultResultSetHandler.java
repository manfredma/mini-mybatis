package com.mini.mybatis.executor.resultset;

import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DefaultResultSetHandler implements ResultSetHandler {

    private final MappedStatement mappedStatement;
    private final BoundSql boundSql;

    public DefaultResultSetHandler(MappedStatement mappedStatement, BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
        this.boundSql = boundSql;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> List<E> handleResultSets(PreparedStatement ps) throws SQLException {
        List<E> results = new ArrayList<>();
        ResultSet rs = ps.getResultSet();
        if (rs == null) {
            return results;
        }

        Class<?> resultType = mappedStatement.getResultType();
        if (resultType == null) {
            return results;
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Object result = createResultObject(resultType, rs, metaData, columnCount);
            results.add((E) result);
        }
        rs.close();
        return results;
    }

    private Object createResultObject(Class<?> resultType, ResultSet rs, ResultSetMetaData metaData, int columnCount) throws SQLException {
        // Handle primitive / wrapper / String types
        if (isPrimitive(resultType)) {
            return rs.getObject(1);
        }

        try {
            Object instance = resultType.getDeclaredConstructor().newInstance();
            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = metaData.getColumnLabel(i);
                String fieldName = underscoreToCamel(columnLabel);
                Object value = rs.getObject(i);
                Field field = findField(resultType, fieldName);
                if (field != null && value != null) {
                    field.setAccessible(true);
                    field.set(instance, convertValue(value, field.getType()));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error creating result object for type: " + resultType, e);
        }
    }

    private boolean isPrimitive(Class<?> type) {
        return type.isPrimitive()
                || type == String.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Boolean.class
                || type == Byte.class
                || type == Short.class;
    }

    private String underscoreToCamel(String name) {
        String lower = name.toLowerCase();
        if (!lower.contains("_")) {
            return lower;
        }
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : lower.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    private Field findField(Class<?> clazz, String fieldName) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(fieldName)) {
                    return f;
                }
            }
        }
        return null;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isAssignableFrom(value.getClass())) return value;
        if (targetType == Long.class || targetType == long.class) return ((Number) value).longValue();
        if (targetType == Integer.class || targetType == int.class) return ((Number) value).intValue();
        if (targetType == Double.class || targetType == double.class) return ((Number) value).doubleValue();
        if (targetType == Float.class || targetType == float.class) return ((Number) value).floatValue();
        if (targetType == String.class) return value.toString();
        return value;
    }
}
