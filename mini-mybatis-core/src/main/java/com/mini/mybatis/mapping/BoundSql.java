package com.mini.mybatis.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * An actual SQL string with parameter mappings after dynamic SQL processing.
 */
public class BoundSql {

    private final String sql;
    private final List<ParameterMapping> parameterMappings;
    private final Object parameterObject;

    public BoundSql(String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings != null ? parameterMappings : new ArrayList<>();
        this.parameterObject = parameterObject;
    }

    public String getSql() {
        return sql;
    }

    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return parameterObject;
    }
}
