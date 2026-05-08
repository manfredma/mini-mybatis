package com.mini.mybatis.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A value object used as a cache key.
 * Keys are built by updating with a series of objects that together identify a unique query.
 */
public class CacheKey implements Cloneable, Serializable {

    private static final long serialVersionUID = 1146682552656046210L;

    public static final CacheKey NULL_CACHE_KEY = new CacheKey() {
        @Override
        public void update(Object object) {
            throw new UnsupportedOperationException("Not allowed to update a null cache key instance.");
        }
    };

    private static final int DEFAULT_MULTIPLIER = 37;
    private static final int DEFAULT_HASHCODE = 17;

    private final int multiplier;
    private int hashcode;
    private long checksum;
    private int count;
    private final List<Object> updateList;

    public CacheKey() {
        this.hashcode = DEFAULT_HASHCODE;
        this.multiplier = DEFAULT_MULTIPLIER;
        this.count = 0;
        this.updateList = new ArrayList<>();
    }

    public CacheKey(Object[] objects) {
        this();
        updateAll(objects);
    }

    public void update(Object object) {
        int baseHashCode = object == null ? 1 : object.hashCode();
        count++;
        checksum += baseHashCode;
        baseHashCode *= count;
        hashcode = multiplier * hashcode + baseHashCode;
        updateList.add(object);
    }

    public void updateAll(Object[] objects) {
        for (Object o : objects) {
            update(o);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CacheKey)) {
            return false;
        }
        CacheKey cacheKey = (CacheKey) object;
        if (hashcode != cacheKey.hashcode) {
            return false;
        }
        if (checksum != cacheKey.checksum) {
            return false;
        }
        if (count != cacheKey.count) {
            return false;
        }
        for (int i = 0; i < updateList.size(); i++) {
            Object thisObject = updateList.get(i);
            Object thatObject = cacheKey.updateList.get(i);
            if (thisObject == null) {
                if (thatObject != null) {
                    return false;
                }
            } else if (!thisObject.equals(thatObject)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder().append(hashcode).append(':').append(checksum).append(':');
        for (Object obj : updateList) {
            returnValue.append(obj).append(':');
        }
        return returnValue.toString();
    }
}
