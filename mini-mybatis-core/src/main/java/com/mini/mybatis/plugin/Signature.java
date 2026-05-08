package com.mini.mybatis.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a method signature to intercept within {@link Intercepts}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Signature {
    Class<?> type();
    String method();
    Class<?>[] args();
}
