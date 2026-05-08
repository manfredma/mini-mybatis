package com.mini.mybatis.cache;

import org.junit.Test;

import static org.junit.Assert.*;

public class CacheKeyTest {

    @Test
    public void testSameKeyEquality() {
        CacheKey key1 = new CacheKey();
        key1.update("selectUser");
        key1.update(0);
        key1.update(Integer.MAX_VALUE);
        key1.update("SELECT * FROM user WHERE id = ?");
        key1.update(1);

        CacheKey key2 = new CacheKey();
        key2.update("selectUser");
        key2.update(0);
        key2.update(Integer.MAX_VALUE);
        key2.update("SELECT * FROM user WHERE id = ?");
        key2.update(1);

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void testDifferentKeyInequality() {
        CacheKey key1 = new CacheKey();
        key1.update("selectUser");
        key1.update(1);

        CacheKey key2 = new CacheKey();
        key2.update("selectUser");
        key2.update(2);

        assertNotEquals(key1, key2);
    }

    @Test
    public void testNullUpdate() {
        CacheKey key = new CacheKey();
        key.update(null);
        assertNotNull(key.toString());
    }
}
