package io.github.elebras1.flecs.util.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class ParamRegistry {
    private static final AtomicLong COUNTER;
    private static final ConcurrentHashMap<Long, Object> TABLE;

    static {
        COUNTER = new AtomicLong(1);
        TABLE = new ConcurrentHashMap<>();
    }

    public static long put(Object param) {
        long id = COUNTER.getAndIncrement();
        TABLE.put(id, param);
        return id;
    }

    public static Object get(long id) {
        return TABLE.get(id);
    }

    public static void remove(long id) {
        TABLE.remove(id);
    }
}
