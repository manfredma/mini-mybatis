package com.mini.mybatis.plugin;

import java.lang.reflect.Method;

/**
 * Encapsulates the intercepted method call — target, method, and args.
 */
public class Invocation {

    private final Object target;
    private final Method method;
    private final Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object proceed() throws Throwable {
        return method.invoke(target, args);
    }
}
