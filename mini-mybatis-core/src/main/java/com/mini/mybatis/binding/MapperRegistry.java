package com.mini.mybatis.binding;

import com.mini.mybatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry that maps Mapper interfaces to their {@link MapperProxyFactory}.
 */
public class MapperRegistry {

    private final com.mini.mybatis.session.Configuration config;
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public MapperRegistry(com.mini.mybatis.session.Configuration config) {
        this.config = config;
    }

    public <T> void addMapper(Class<T> type) {
        if (!type.isInterface()) {
            return;
        }
        if (hasMapper(type)) {
            throw new IllegalArgumentException("Type " + type + " is already known to the MapperRegistry.");
        }
        knownMappers.put(type, new MapperProxyFactory<>(type));
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new IllegalArgumentException("Type " + type + " is not known to the MapperRegistry.");
        }
        return mapperProxyFactory.newInstance(sqlSession);
    }
}
