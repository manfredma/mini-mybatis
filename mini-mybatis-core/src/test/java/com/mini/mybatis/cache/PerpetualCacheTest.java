package com.mini.mybatis.cache;

import org.junit.Test;

import static org.junit.Assert.*;

public class PerpetualCacheTest {

    @Test
    public void testPutAndGet() {
        Cache cache = new PerpetualCache("test");
        cache.putObject("key1", "value1");
        assertEquals("value1", cache.getObject("key1"));
    }

    @Test
    public void testGetMissReturnsNull() {
        Cache cache = new PerpetualCache("test");
        assertNull(cache.getObject("nonexistent"));
    }

    @Test
    public void testRemove() {
        Cache cache = new PerpetualCache("test");
        cache.putObject("key1", "value1");
        cache.removeObject("key1");
        assertNull(cache.getObject("key1"));
    }

    @Test
    public void testClear() {
        Cache cache = new PerpetualCache("test");
        cache.putObject("a", 1);
        cache.putObject("b", 2);
        assertEquals(2, cache.getSize());
        cache.clear();
        assertEquals(0, cache.getSize());
    }

    @Test
    public void testGetId() {
        Cache cache = new PerpetualCache("myCache");
        assertEquals("myCache", cache.getId());
    }
}
