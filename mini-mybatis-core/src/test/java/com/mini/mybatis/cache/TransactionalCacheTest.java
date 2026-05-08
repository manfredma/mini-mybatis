package com.mini.mybatis.cache;

import com.mini.mybatis.cache.decorators.TransactionalCache;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransactionalCacheTest {

    @Test
    public void testPendingWritesNotVisibleBeforeCommit() {
        Cache delegate = new PerpetualCache("test");
        TransactionalCache txCache = new TransactionalCache(delegate);

        txCache.putObject("key", "value");
        // not committed yet — delegate should not have it
        assertNull(delegate.getObject("key"));
    }

    @Test
    public void testWritesVisibleAfterCommit() {
        Cache delegate = new PerpetualCache("test");
        TransactionalCache txCache = new TransactionalCache(delegate);

        txCache.putObject("key", "value");
        txCache.commit();

        assertEquals("value", delegate.getObject("key"));
    }

    @Test
    public void testRollbackDiscardsWrites() {
        Cache delegate = new PerpetualCache("test");
        TransactionalCache txCache = new TransactionalCache(delegate);

        txCache.putObject("key", "value");
        txCache.rollback();

        assertNull(delegate.getObject("key"));
    }

    @Test
    public void testClearOnCommit() {
        Cache delegate = new PerpetualCache("test");
        delegate.putObject("existing", "value");

        TransactionalCache txCache = new TransactionalCache(delegate);
        txCache.clear();
        txCache.commit();

        assertNull(delegate.getObject("existing"));
        assertEquals(0, delegate.getSize());
    }

    @Test
    public void testGetReturnsDelegateValue() {
        Cache delegate = new PerpetualCache("test");
        delegate.putObject("key", "existing");

        TransactionalCache txCache = new TransactionalCache(delegate);
        assertEquals("existing", txCache.getObject("key"));
    }
}
