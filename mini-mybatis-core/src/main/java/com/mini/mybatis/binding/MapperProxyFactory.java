package com.mini.mybatis.binding;

import com.mini.mybatis.session.SqlSession;

import java.lang.reflect.Proxy;

/**
 * Creates JDK dynamic proxy instances for a given Mapper interface.
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                mapperProxy);
    }
}
