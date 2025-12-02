package com.github.elebras1.flecs.util;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class LayoutField {

    private LayoutField() {}

    public static MemoryLayout floatLayout() {
        return ValueLayout.JAVA_FLOAT;
    }

    public static MemoryLayout doubleLayout() {
        return ValueLayout.JAVA_DOUBLE;
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

    public static MemoryLayout stringLayout() {
        return ValueLayout.ADDRESS;
    }

    public static void set(MemorySegment segment, long offset, float value) {
        segment.set(ValueLayout.JAVA_FLOAT, offset, value);
    }

    public static void set(MemorySegment segment, long offset, double value) {
        segment.set(ValueLayout.JAVA_DOUBLE, offset, value);
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

    public static void set(MemorySegment segment, long offset, boolean value) {
        segment.set(ValueLayout.JAVA_BOOLEAN, offset, value);
    }

    public static void set(MemorySegment segment, long offset, String value) {
        MemorySegment stringSegment = value == null ? MemorySegment.NULL : Arena.ofAuto().allocateFrom(value);
        segment.set(ValueLayout.ADDRESS, offset, stringSegment);
    }

    public static float getFloat(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_FLOAT, offset);
    }

    public static double getDouble(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.JAVA_DOUBLE, offset);
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

    public static String getString(MemorySegment segment, long offset) {
        return segment.get(ValueLayout.ADDRESS, offset).toString();
    }

    public static long offsetOf(MemoryLayout layout, String fieldName) {
        return layout.byteOffset(MemoryLayout.PathElement.groupElement(fieldName));
    }
}

