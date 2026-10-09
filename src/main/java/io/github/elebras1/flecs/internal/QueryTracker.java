package io.github.elebras1.flecs.internal;

import io.github.elebras1.flecs.Query;

import java.util.function.Consumer;

public final class QueryTracker {
    private static final long EMPTY = 0L;
    private static final long TOMBSTONE = -1L;

    private long[] keys;
    private Query[] values;
    private int mask;
    private int size;
    private int used;

    public QueryTracker() {
        this.keys = new long[16];
        this.values = new Query[16];
        this.mask = 15;
    }

    private static int hash(long key) {
        key ^= key >>> 33;
        key *= 0xff51afd7ed558ccdL;
        key ^= key >>> 33;
        key *= 0xc4ceb9fe1a85ec53L;
        key ^= key >>> 33;
        return (int) key;
    }

    public void put(long key, Query value) {
        if ((size + 1) * 2 > keys.length || used * 2 > keys.length) {
            rehash(keys.length << 1);
        }

        int idx = hash(key) & mask;
        int tombstone = -1;
        while (true) {
            long k = keys[idx];
            if (k == key) {
                values[idx] = value;
                return;
            }
            if (k == EMPTY) {
                if (tombstone != -1) {
                    keys[tombstone] = key;
                    values[tombstone] = value;
                } else {
                    keys[idx] = key;
                    values[idx] = value;
                    used++;
                }
                size++;
                return;
            }
            if (k == TOMBSTONE && tombstone == -1) {
                tombstone = idx;
            }
            idx = (idx + 1) & mask;
        }
    }

    public Query remove(long key) {
        int idx = hash(key) & mask;
        while (true) {
            long k = keys[idx];
            if (k == key) {
                Query old = values[idx];
                values[idx] = null;
                keys[idx] = TOMBSTONE;
                size--;
                return old;
            }
            if (k == EMPTY) {
                return null;
            }
            idx = (idx + 1) & mask;
        }
    }

    public void forEachValue(Consumer<Query> action) {
        for (int i = 0; i < keys.length; i++) {
            long k = keys[i];
            if (k != EMPTY && k != TOMBSTONE) {
                action.accept(values[i]);
            }
        }
    }

    public int size() {
        return this.size;
    }

    private void rehash(int capacity) {
        long[] oldKeys = this.keys;
        Query[] oldValues = this.values;
        this.keys = new long[capacity];
        this.values = new Query[capacity];
        this.mask = capacity - 1;
        this.size = 0;
        this.used = 0;
        for (int i = 0; i < oldKeys.length; i++) {
            long k = oldKeys[i];
            if (k != EMPTY && k != TOMBSTONE) {
                put(k, oldValues[i]);
            }
        }
    }
}
