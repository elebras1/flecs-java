package com.github.elebras1.flecs.collection;

import java.util.*;


/**
 * A dynamic array for primitive long values.
 * Not thread-safe.
 */
public final class EcsLongList implements Iterable<Long>, RandomAccess, Cloneable {
    private static final int DEFAULT_CAPACITY = 16;
    public long[] data;
    private int size;

    public EcsLongList() {
        this.data = new long[DEFAULT_CAPACITY];
    }

    public EcsLongList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Capacity < 0");
        }
        this.data = new long[Math.max(initialCapacity, DEFAULT_CAPACITY)];
        this.size = 0;
    }

    public EcsLongList(long[] src) {
        Objects.requireNonNull(src);
        this.data = Arrays.copyOf(src, src.length);
        this.size = src.length;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() { return size == 0; }

    public void add(long value) {
        this.ensureCapacity(this.size + 1);
        this.data[this.size++] = value;
    }

    public void add(int index, long value) {
        this.rangeCheckForAdd(index);
        this.ensureCapacity(this.size + 1);
        System.arraycopy(this.data, index, this.data, index + 1, this.size - index);
        this.data[index] = value;
        this.size++;
    }

    public void addAll(long[] values) {
        Objects.requireNonNull(values);
        int numNew = values.length;
        this.ensureCapacity(this.size + numNew);
        System.arraycopy(values, 0, this.data, this.size, numNew);
        this.size += numNew;
    }

    public long get(int index) {
        this.rangeCheck(index);
        return this.data[index];
    }

    public long set(int index, long value) {
        this.rangeCheck(index);
        long old = this.data[index];
        this.data[index] = value;
        return old;
    }

    public long removeAt(int index) {
        this.rangeCheck(index);
        long old = this.data[index];
        int numMoved = this.size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(this.data, index + 1, this.data, index, numMoved);
        }
        this.size--;
        return old;
    }

    public boolean removeLong(long value) {
        for (int i = 0; i < this.size; i++) {
            if (this.data[i] == value) {
                this.removeAt(i);
                return true;
            }
        }
        return false;
    }

    public int indexOf(long value) {
        for (int i = 0; i < this.size; i++) {
            if (this.data[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(long value) {
        for (int i = this.size - 1; i >= 0; i--) {
            if (this.data[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(long value) {
        return indexOf(value) >= 0;
    }

    public void clear() {
        this.size = 0;
    }

    public long[] toLongArray() {
        return Arrays.copyOf(this.data, this.size);
    }

    public void sort() {
        Arrays.sort(this.data, 0, this.size);
    }

    public LongIterator longIterator() {
        return new LongIterator() {
            int cursor = 0;
            int lastRet = -1;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public long nextLong() {
                if (cursor >= size) {
                    throw new java.util.NoSuchElementException();
                }
                lastRet = cursor++;
                return data[lastRet];
            }

            @Override
            public void remove() {
                if (lastRet < 0) {
                    throw new IllegalStateException();
                }
                EcsLongList.this.removeAt(lastRet);
                cursor = lastRet;
                lastRet = -1;
            }
        };
    }

    @Override
    public Iterator<Long> iterator() {
        return new Iterator<>() {
            private final LongIterator it = longIterator();
            @Override public boolean hasNext() {
                return it.hasNext();
            }

            @Override public Long next() {
                return it.nextLong();
            }

            @Override public void remove() {
                it.remove();
            }
        };
    }

    public interface LongIterator {
        boolean hasNext();
        long nextLong();
        void remove();
    }


    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= this.data.length) {
            return;
        }
        int newCapacity = this.data.length + (this.data.length >> 1);
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        this.data = Arrays.copyOf(this.data, newCapacity);
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
        }
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > this.size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
        }
    }

    @Override
    public String toString() {
        if (this.size == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder(2*size);
        sb.append('[');
        for (int i = 0; i < this.size; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(this.data[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public EcsLongList clone() {
        try {
            EcsLongList c = (EcsLongList) super.clone();
            c.data = Arrays.copyOf(this.data, this.data.length);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
