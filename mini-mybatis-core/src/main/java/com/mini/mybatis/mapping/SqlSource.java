package com.mini.mybatis.mapping;

/**
 * Represents the content of a mapped SQL statement read from an annotation or XML file.
 * Creates the SQL that will be passed to the database from a given input object.
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);
}
