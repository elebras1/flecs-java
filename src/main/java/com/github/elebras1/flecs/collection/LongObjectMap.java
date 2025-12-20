package com.github.elebras1.flecs.collection;

import java.util.Arrays;

public final class LongObjectMap<V> {
    private static final int CAPACITY = 512;
    private static final int MASK = 511;
    private static final long EMPTY_KEY = -1L;

    private final long[] keys;
    private final V[] values;
    private int size;

    @SuppressWarnings("unchecked")
    public LongObjectMap() {
        this.keys = new long[CAPACITY];
        this.values = (V[]) new Object[CAPACITY];
        this.size = 0;
        Arrays.fill(keys, EMPTY_KEY);
    }

    private static int hash(long key) {
        key ^= key >>> 33;
        key *= 0xff51afd7ed558ccdL;
        key ^= key >>> 33;
        key *= 0xc4ceb9fe1a85ec53L;
        key ^= key >>> 33;
        return (int) key;
    }

    public V get(long key) {
        final long[] keysLocal = this.keys;
        final V[] valuesLocal = this.values;

        int idx = hash(key) & MASK;
        long k = keysLocal[idx];

        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == EMPTY_KEY) {
            return null;
        }

        idx = (idx + 1) & MASK;
        k = keysLocal[idx];
        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == EMPTY_KEY) {
            return null;
        }

        idx = (idx + 1) & MASK;
        k = keysLocal[idx];
        if (k == key) {
            return valuesLocal[idx];
        }
        if (k == EMPTY_KEY) {
            return null;
        }

        return getSlowPath(key, idx, keysLocal, valuesLocal);
    }

    private V getSlowPath(long key, int idx, long[] keysLocal, V[] valuesLocal) {
        while (true) {
            idx = (idx + 1) & MASK;
            long k = keysLocal[idx];

            if (k == key) return valuesLocal[idx];
            if (k == EMPTY_KEY) return null;
        }
    }

    public void put(long key, V value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values not allowed");
        }

        if (size >= CAPACITY - 1) {
            throw new IllegalStateException("Map is full");
        }

        final long[] keysLocal = this.keys;
        int idx = hash(key) & MASK;

        while (keysLocal[idx] != EMPTY_KEY) {
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

    public boolean containsKey(long key) {
        return get(key) != null;
    }

    public int size() {
        return this.size;
    }
}