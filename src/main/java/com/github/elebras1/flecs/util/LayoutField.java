package com.github.elebras1.flecs.util;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class LayoutField {

    private LayoutField() {}

    public static MemoryLayout floatLayout() {
        return ValueLayout.JAVA_FLOAT;
    }

    public static MemoryLayout doubleLayout() {
        return ValueLayout.JAVA_DOUBLE;
    }

    public static MemoryLayout byteLayout() {
        return ValueLayout.JAVA_BYTE;
    }

    public static MemoryLayout shortLayout() {
        return ValueLayout.JAVA_SHORT;
    }

    public static MemoryLayout intLayout() {
        return ValueLayout.JAVA_INT;
    }

    public static MemoryLayout longLayout() {
        return ValueLayout.JAVA_LONG;
    }

    public static MemoryLayout booleanLayout() {
        return ValueLayout.JAVA_BOOLEAN;
    }

    public static MemoryLayout intArrayLayout(int length) {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_INT);
    }

    public static MemoryLayout longArrayLayout(int length) {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_LONG);
    }

    public static MemoryLayout floatArrayLayout(int length) {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_FLOAT);
    }

    public static MemoryLayout doubleArrayLayout(int length) {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_DOUBLE);
    }

    public static MemoryLayout byteArrayLayout(int length) {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_BYTE);
    }

    public static MemoryLayout shortArrayLayout(int length) {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_SHORT);
    }

    public static MemoryLayout booleanArrayLayout(int length) {
        return MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_BOOLEAN);
    }

    public static MemoryLayout stringLayout(int capacity) {
        return MemoryLayout.sequenceLayout(capacity, ValueLayout.JAVA_BYTE);
    }

    public static void set(MemorySegment segment, long offset, float value) {
        segment.set(ValueLayout.JAVA_FLOAT, offset, value);
    }

    public static void set(MemorySegment segment, long offset, double value) {
        segment.set(ValueLayout.JAVA_DOUBLE, offset, value);
    }

    public static void set(MemorySegment segment, long offset, byte value) {
        segment.set(ValueLayout.JAVA_BYTE, offset, value);
    }

    public static void set(MemorySegment segment, long offset, short value) {
        segment.set(ValueLayout.JAVA_SHORT, offset, value);
    }

    public static void set(MemorySegment segment, long offset, int value) {
        segment.set(ValueLayout.JAVA_INT, offset, value);
    }

    public static void set(MemorySegment segment, long offset, long value) {
        segment.set(ValueLayout.JAVA_LONG, offset, value);
    }

    public static void set(MemorySegment segment, long offset, int[] value, int capacity) {
        MemorySegment.copy(value, 0, segment, ValueLayout.JAVA_INT, offset, capacity);
    }

    public static void set(MemorySegment segment, long offset, long[] value, int capacity) {
        MemorySegment.copy(value, 0, segment, ValueLayout.JAVA_LONG, offset, capacity);
    }

    public static void set(MemorySegment segment, long offset, float[] value, int capacity) {
        MemorySegment.copy(value, 0, segment, ValueLayout.JAVA_FLOAT, offset, capacity);
    }

    public static void set(MemorySegment segment, long offset, double[] value, int capacity) {
        MemorySegment.copy(value, 0, segment, ValueLayout.JAVA_DOUBLE, offset, capacity);
    }

    public static void set(MemorySegment segment, long offset, byte[] value, int capacity) {
        MemorySegment.copy(value, 0, segment, ValueLayout.JAVA_BYTE, offset, capacity);
    }

    public static void set(MemorySegment segment, long offset, short[] value, int capacity) {
        MemorySegment.copy(value, 0, segment, ValueLayout.JAVA_SHORT, offset, capacity);
    }

    public static void set(MemorySegment segment, long offset, boolean[] value, int capacity) {
        int len = Math.min(value.length, capacity);
        for (int i = 0; i < len; i++) {
            segment.set(ValueLayout.JAVA_BYTE, offset + i, (byte) (value[i] ? 1 : 0));
        }
    }

    public static void set(MemorySegment segment, long offset, String value, int capacity) {
        MemorySegment slice = segment.asSlice(offset, capacity);
        if (value == null || value.isEmpty()) {
            slice.set(ValueLayout.JAVA_BYTE, 0, (byte) 0);
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(bytes.length, capacity - 1);
        MemorySegment.copy(bytes, 0, slice, ValueLayout.JAVA_BYTE, 0, len);
        slice.set(ValueLayout.JAVA_BYTE, len, (byte) 0);
    }

    public static float getFloat(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_FLOAT, offset);
    }

    public static double getDouble(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_DOUBLE, offset);
    }

    public static byte getByte(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_BYTE, offset);
    }

    public static short getShort(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_SHORT, offset);
    }

    public static int getInt(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_INT, offset);
    }

    public static long getLong(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_LONG, offset);
    }

    public static boolean getBoolean(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_BOOLEAN, offset);
    }

    public static int[] getIntArray(MemorySegment segment, long offset, int length) {
        return segment.asSlice(offset, length * ValueLayout.JAVA_INT.byteSize()).toArray(ValueLayout.JAVA_INT);
    }

    public static long[] getLongArray(MemorySegment segment, long offset, int length) {
        return segment.asSlice(offset, length * ValueLayout.JAVA_LONG.byteSize()).toArray(ValueLayout.JAVA_LONG);
    }

    public static float[] getFloatArray(MemorySegment segment, long offset, int length) {
        return segment.asSlice(offset, length * ValueLayout.JAVA_FLOAT.byteSize()).toArray(ValueLayout.JAVA_FLOAT);
    }

    public static double[] getDoubleArray(MemorySegment segment, long offset, int length) {
        return segment.asSlice(offset, length * ValueLayout.JAVA_DOUBLE.byteSize()).toArray(ValueLayout.JAVA_DOUBLE);
    }

    public static byte[] getByteArray(MemorySegment segment, long offset, int length) {
        return segment.asSlice(offset, length * ValueLayout.JAVA_BYTE.byteSize()).toArray(ValueLayout.JAVA_BYTE);
    }

    public static short[] getShortArray(MemorySegment segment, long offset, int length) {
        return segment.asSlice(offset, length * ValueLayout.JAVA_SHORT.byteSize()).toArray(ValueLayout.JAVA_SHORT);
    }

    public static boolean[] getBooleanArray(MemorySegment segment, long offset, int length) {
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            byte value = segment.get(ValueLayout.JAVA_BYTE, offset + i);
            result[i] = value != 0;
        }
        return result;
    }

    public static String getFixedString(MemorySegment segment, long offset, int capacity) {
        return segment.asSlice(offset, capacity).getString(0);
    }

    public static long offsetOf(MemoryLayout layout, String fieldName) {
        return layout.byteOffset(MemoryLayout.PathElement.groupElement(fieldName));
    }

    public static MemoryLayout createStructLayout(String name, MemoryLayout... elements) {
        if (elements == null || elements.length == 0) {
            return MemoryLayout.structLayout(
                    MemoryLayout.paddingLayout(1)
            ).withName(name);
        }

        List<MemoryLayout> layouts = new ArrayList<>();
        long offset = 0;
        long maxAlign = 1;

        for (MemoryLayout elem : elements) {
            long align = elem.byteAlignment();
            if (align > maxAlign) {
                maxAlign = align;
            }

            if (offset % align != 0) {
                long pad = align - (offset % align);
                layouts.add(MemoryLayout.paddingLayout(pad));
                offset += pad;
            }

            layouts.add(elem);
            offset += elem.byteSize();
        }

        if (offset % maxAlign != 0) {
            long pad = maxAlign - (offset % maxAlign);
            layouts.add(MemoryLayout.paddingLayout(pad));
        }

        return MemoryLayout.structLayout(layouts.toArray(new MemoryLayout[0])).withName(name);
    }
}

