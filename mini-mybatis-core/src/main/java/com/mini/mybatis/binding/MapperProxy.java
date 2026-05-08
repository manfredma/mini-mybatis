package com.mini.mybatis.binding;

import com.mini.mybatis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDK InvocationHandler that routes mapper method calls to the correct SqlSession operation.
 */
public class MapperProxy<T> implements InvocationHandler {

    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<>();

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        MapperMethod mapperMethod = cachedMapperMethod(method);
        return mapperMethod.execute(sqlSession, args);
    }

    private MapperMethod cachedMapperMethod(Method method) {
        return methodCache.computeIfAbsent(method,
                k -> new MapperMethod(mapperInterface, method, sqlSession.getConfiguration()));
    }
}
