package com.mini.mybatis.builder.xml;

import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.ParameterMapping;
import com.mini.mybatis.mapping.SqlSource;

import java.util.List;

/**
 * A SqlSource whose SQL string is already fully resolved (no dynamic SQL).
 */
public class StaticSqlSource implements SqlSource {

    private final String sql;
    private final List<ParameterMapping> parameterMappings;

    public StaticSqlSource(String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(sql, parameterMappings, parameterObject);
    }
}
