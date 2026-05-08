package com.mini.mybatis.session;

import com.mini.mybatis.binding.MapperRegistry;
import com.mini.mybatis.executor.CachingExecutor;
import com.mini.mybatis.executor.Executor;
import com.mini.mybatis.executor.SimpleExecutor;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.plugin.Interceptor;
import com.mini.mybatis.plugin.InterceptorChain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The top-level configuration object. Holds all parsed mapper statements,
 * settings, and the plugin interceptor chain.
 */
public class Configuration {

    protected Environment environment;
    protected boolean cacheEnabled = true;
    protected boolean useGeneratedKeys = false;

    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();
    protected final InterceptorChain interceptorChain = new InterceptorChain();
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    public boolean hasMappedStatement(String id) {
        return mappedStatements.containsKey(id);
    }

    public Collection<MappedStatement> getMappedStatements() {
        return mappedStatements.values();
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    /**
     * Creates a new Executor, wrapping it with CachingExecutor (if second-level cache is enabled)
     * and then with any registered interceptors.
     */
    public Executor newExecutor() {
        return newExecutor(false);
    }

    public Executor newExecutor(boolean autoCommit) {
        Executor executor = new SimpleExecutor(this);
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }
}
