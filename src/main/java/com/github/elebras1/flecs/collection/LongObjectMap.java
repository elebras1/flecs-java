package com.github.elebras1.flecs.collection;

import java.util.Arrays;

public final class LongObjectMap<V> {
    private static final long EMPTY_KEY = -1L;
    private final long[] keys;
    private final V[] values;
    private final int mask;
    private int size;

    @SuppressWarnings("unchecked")
    public LongObjectMap(int expectedSize) {
        int capacity = nextPowerOfTwo(Math.max(expectedSize * 2, 16));
        this.mask = capacity - 1;
        this.keys = new long[capacity];
        this.values = (V[]) new Object[capacity];
        Arrays.fill(this.keys, EMPTY_KEY);
    }

    private static int nextPowerOfTwo(int n) {
        return 1 << (32 - Integer.numberOfLeadingZeros(n - 1));
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

        int idx = hash(key) & this.mask;
        long k = keysLocal[idx];

        if (k == key) return valuesLocal[idx];
        if (k == EMPTY_KEY) return null;

        idx = (idx + 1) & this.mask;
        k = keysLocal[idx];
        if (k == key) return valuesLocal[idx];
        if (k == EMPTY_KEY) return null;

        idx = (idx + 1) & this.mask;
        k = keysLocal[idx];
        if (k == key) return valuesLocal[idx];
        if (k == EMPTY_KEY) return null;

        return this.getSlowPath(key, idx, keysLocal, valuesLocal);
    }

    private V getSlowPath(long key, int idx, long[] keysLocal, V[] valuesLocal) {
        while (true) {
            idx = (idx + 1) & this.mask;
            long k = keysLocal[idx];
            if (k == key) return valuesLocal[idx];
            if (k == EMPTY_KEY) return null;
        }
    }

    public void put(long key, V value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values not allowed");
        }
        if (this.size == this.mask + 1) {
            throw new IllegalStateException("Map is full");
        }

        final long[] keysLocal = this.keys;
        int idx = hash(key) & this.mask;

        while (keysLocal[idx] != EMPTY_KEY) {
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

    public boolean containsKey(long key) {
        return get(key) != null;
    }

    public int size() {
        return this.size;
    }
}