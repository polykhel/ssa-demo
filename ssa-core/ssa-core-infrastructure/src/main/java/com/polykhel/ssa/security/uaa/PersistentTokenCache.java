package com.polykhel.ssa.security.uaa;

import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PersistentTokenCache<T> {
    private final long expireMillis;
    private final Map<String, PersistentTokenCache<T>.Value> map;
    private long latestWriteTime;

    public PersistentTokenCache(long expireMillis) {
        if (expireMillis <= 0L) {
            throw new IllegalArgumentException();
        } else {
            this.expireMillis = expireMillis;
            this.map = new LinkedHashMap<>(64, 0.75F);
            this.latestWriteTime = System.currentTimeMillis();
        }
    }

    public T get(String key) {
        purge();
        PersistentTokenCache<T>.Value val = this.map.get(key);
        long time = System.currentTimeMillis();
        return val != null && time < val.expire ? val.token : null;
    }

    public void put(String key, T token) {
        purge();
        map.remove(key);
        long time = System.currentTimeMillis();
        map.put(key, new Value(token, time + expireMillis));
        latestWriteTime = time;
    }

    public void purge() {
        long time = System.currentTimeMillis();
        if (time - latestWriteTime > expireMillis) {
            map.clear();
        } else {
            Iterator<Value> values = map.values().iterator();
            while (values.hasNext() && time >= values.next().expire) {
                values.remove();
            }
        }
    }

    @AllArgsConstructor
    private class Value {
        private final T token;
        private final long expire;
    }
}
