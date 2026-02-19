package com.github.elebras1.flecs.collection;

import java.util.Arrays;

public final class ClassLongMap {
    private static final long EMPTY_VALUE = -1L;

    private final Class<?>[] keys;
    private final long[] values;
    private final int mask;
    private int size;

    public ClassLongMap(int expectedSize) {
        int capacity = nextPowerOfTwo(Math.max(expectedSize * 2, 16));
        this.mask = capacity - 1;
        this.keys = new Class<?>[capacity];
        this.values = new long[capacity];
        Arrays.fill(this.values, EMPTY_VALUE);
    }

    private static int nextPowerOfTwo(int n) {
        return 1 << (32 - Integer.numberOfLeadingZeros(n - 1));
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

        int idx = mix(System.identityHashCode(key)) & this.mask;
        Class<?> k = keysLocal[idx];


        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == null) {
            return EMPTY_VALUE;
        }

        idx = (idx + 1) & this.mask;
        k = keysLocal[idx];
        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == null) {
            return EMPTY_VALUE;
        }

        idx = (idx + 1) & this.mask;
        k = keysLocal[idx];
        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == null) {
            return EMPTY_VALUE;
        }

        return this.getSlowPath(key, idx, keysLocal, valuesLocal);
    }

    private long getSlowPath(Class<?> key, int idx, Class<?>[] keysLocal, long[] valuesLocal) {
        while (true) {
            idx = (idx + 1) & this.mask;
            Class<?> k = keysLocal[idx];
            if (k == key) {
                return valuesLocal[idx];
            }
            if (k == null) {
                return EMPTY_VALUE;
            }
        }
    }

    public void put(Class<?> key, long value) {
        if (this.size == this.mask + 1) {
            throw new IllegalStateException("Map is full");
        }

        final Class<?>[] keysLocal = this.keys;
        int idx = mix(System.identityHashCode(key)) & this.mask;

        while (keysLocal[idx] != null) {
            if (keysLocal[idx] == key) {
                this.values[idx] = value;
                return;
            }
            idx = (idx + 1) & this.mask;
        }

        keysLocal[idx] = key;
        this.values[idx] = value;
        this.size++;
    }

    public boolean containsKey(Class<?> key) {
        return get(key) != EMPTY_VALUE;
    }

    public int size() {
        return this.size;
    }
}