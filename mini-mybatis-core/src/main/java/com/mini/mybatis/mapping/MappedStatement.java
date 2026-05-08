package com.mini.mybatis.mapping;

import com.mini.mybatis.cache.Cache;
import com.mini.mybatis.session.Configuration;

/**
 * Stores the configuration and SQL for a single mapper method.
 */
public class MappedStatement {

    private String id;
    private SqlCommandType sqlCommandType;
    private SqlSource sqlSource;
    private Class<?> resultType;
    private String resultMap;
    private Cache cache;
    private boolean useCache;
    private boolean flushCacheRequired;
    private Configuration configuration;

    private MappedStatement() {}

    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    public String getId() { return id; }
    public SqlCommandType getSqlCommandType() { return sqlCommandType; }
    public SqlSource getSqlSource() { return sqlSource; }
    public Class<?> getResultType() { return resultType; }
    public String getResultMap() { return resultMap; }
    public Cache getCache() { return cache; }
    public boolean isUseCache() { return useCache; }
    public boolean isFlushCacheRequired() { return flushCacheRequired; }
    public Configuration getConfiguration() { return configuration; }

    public static Builder builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
        return new Builder(configuration, id, sqlSource, sqlCommandType);
    }

    public static class Builder {
        private final MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.useCache = sqlCommandType == SqlCommandType.SELECT;
            mappedStatement.flushCacheRequired = sqlCommandType != SqlCommandType.SELECT;
        }

        public Builder resultType(Class<?> resultType) {
            mappedStatement.resultType = resultType;
            return this;
        }

        public Builder resultMap(String resultMap) {
            mappedStatement.resultMap = resultMap;
            return this;
        }

        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }

        public Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }

        public Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }

        public MappedStatement build() {
            return mappedStatement;
        }
    }
}
