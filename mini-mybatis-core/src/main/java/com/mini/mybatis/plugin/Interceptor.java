package com.mini.mybatis.plugin;

import java.util.Properties;

/**
 * Interceptor interface for the plugin mechanism.
 * Implementations can intercept Executor, StatementHandler, ParameterHandler, and ResultSetHandler methods.
 */
public interface Interceptor {

    Object intercept(Invocation invocation) throws Throwable;

    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    default void setProperties(Properties properties) {
        // optional configuration
    }
}
