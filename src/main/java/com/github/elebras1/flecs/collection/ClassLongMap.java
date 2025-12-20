package com.github.elebras1.flecs.collection;

import java.util.Arrays;

public final class ClassLongMap {
    private static final int CAPACITY = 512;
    private static final int MASK = 511;
    private static final long EMPTY_KEY = -1L;

    private final Class<?>[] keys;
    private final long[] values;
    private int size;

    public ClassLongMap() {
        this.keys = new Class<?>[CAPACITY];
        this.values = new long[CAPACITY];
        this.size = 0;
        Arrays.fill(values, EMPTY_KEY);
    }

    private static int mix(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    public long get(Class<?> key) {
        final Class<?>[] keysLocal = this.keys;
        final long[] valuesLocal = this.values;

        int idx = mix(System.identityHashCode(key)) & MASK;
        Class<?> k = keysLocal[idx];


        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == null) {
            return EMPTY_KEY;
        }

        idx = (idx + 1) & MASK;
        k = keysLocal[idx];
        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == null) {
            return EMPTY_KEY;
        }

        idx = (idx + 1) & MASK;
        k = keysLocal[idx];
        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == null) {
            return EMPTY_KEY;
        }

        return this.getSlowPath(key, idx, keysLocal, valuesLocal);
    }

    private long getSlowPath(Class<?> key, int idx, Class<?>[] keysLocal, long[] valuesLocal) {
        while (true) {
            idx = (idx + 1) & MASK;
            Class<?> k = keysLocal[idx];
            if (k == key) {
                return valuesLocal[idx];
            }
            if (k == null) {
                return EMPTY_KEY;
            }
        }
    }

    public void put(Class<?> key, long value) {
        if (this.size == CAPACITY) {
            throw new IllegalStateException("Map is full");
        }

        final Class<?>[] keysLocal = this.keys;
        int idx = mix(System.identityHashCode(key)) & MASK;

        while (keysLocal[idx] != null) {
            if (keysLocal[idx] == key) {
                this.values[idx] = value;
                return;
            }
            idx = (idx + 1) & MASK;
        }

        keysLocal[idx] = key;
        this.values[idx] = value;
        this.size++;
    }

    public boolean containsKey(Class<?> key) {
        return get(key) != EMPTY_KEY;
    }

    public int size() {
        return this.size;
    }
}