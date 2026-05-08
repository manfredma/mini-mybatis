package com.mini.mybatis.plugin;

import com.mini.mybatis.executor.Executor;
import com.mini.mybatis.mapping.BoundSql;
import com.mini.mybatis.mapping.MappedStatement;
import com.mini.mybatis.session.Configuration;
import com.mini.mybatis.session.RowBounds;
import org.junit.Test;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class PluginTest {

    /** Counts how many times the interceptor was invoked */
    static int interceptCount = 0;

    @Intercepts({
        @Signature(type = Executor.class, method = "query", args = {
            MappedStatement.class, Object.class, RowBounds.class, BoundSql.class
        })
    })
    static class CountingInterceptor implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            interceptCount++;
            return invocation.proceed();
        }
    }

    @Test
    public void testInterceptorWrapsExecutor() {
        CountingInterceptor interceptor = new CountingInterceptor();
        // wrap a mock executor
        StubExecutor stub = new StubExecutor();
        Object proxy = Plugin.wrap(stub, interceptor);

        assertTrue("Proxy should implement Executor", proxy instanceof Executor);
    }

    @Test
    public void testNonMatchingTargetNotWrapped() {
        CountingInterceptor interceptor = new CountingInterceptor();
        String target = "not an executor";
        Object result = Plugin.wrap(target, interceptor);
        assertSame("Non-matching target should be returned as-is", target, result);
    }

    @Test
    public void testInterceptorChainPluginAll() {
        InterceptorChain chain = new InterceptorChain();
        chain.addInterceptor(new CountingInterceptor());

        StubExecutor stub = new StubExecutor();
        Object wrapped = chain.pluginAll(stub);
        assertTrue(wrapped instanceof Executor);
    }

    @Test
    public void testMissingAnnotationThrows() {
        Interceptor bad = invocation -> invocation.proceed();
        try {
            Plugin.wrap(new StubExecutor(), bad);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("@Intercepts"));
        }
    }

    @Test
    public void testInvocationProceed() throws Throwable {
        StubExecutor stub = new StubExecutor();
        Method method = Executor.class.getMethod("clearLocalCache");
        Invocation inv = new Invocation(stub, method, new Object[0]);
        inv.proceed();
        assertTrue(stub.clearLocalCacheCalled);
    }

    @Test
    public void testConfigurationAddInterceptor() {
        Configuration config = new Configuration();
        config.addInterceptor(new CountingInterceptor());
        assertEquals(1, config.getInterceptorChain().getInterceptors().size());
    }

    /** Minimal Executor stub for testing */
    static class StubExecutor implements Executor {
        boolean clearLocalCacheCalled = false;

        @Override
        public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) {
            return new ArrayList<>();
        }

        @Override
        public int update(MappedStatement ms, Object parameter) { return 0; }

        @Override
        public void commit(boolean required) {}

        @Override
        public void rollback(boolean required) {}

        @Override
        public void close(boolean forceRollback) {}

        @Override
        public boolean isClosed() { return false; }

        @Override
        public void clearLocalCache() { clearLocalCacheCalled = true; }
    }
}
