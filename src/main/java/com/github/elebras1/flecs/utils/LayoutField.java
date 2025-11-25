package com.github.elebras1.flecs.utils;

import com.github.elebras1.flecs.generated.flecs_h$shared;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public final class LayoutField {

    private LayoutField() {}

    public static MemoryLayout floatLayout() {
        return flecs_h$shared.C_FLOAT;
    }

    public static MemoryLayout doubleLayout() {
        return flecs_h$shared.C_DOUBLE;
    }

    public static MemoryLayout shortLayout() {
        return flecs_h$shared.C_SHORT;
    }

    public static MemoryLayout intLayout() {
        return flecs_h$shared.C_INT;
    }

    public static MemoryLayout longLayout() {
        return flecs_h$shared.C_LONG;
    }

    public static MemoryLayout booleanLayout() {
        return flecs_h$shared.C_BOOL;
    }

    public static MemoryLayout stringLayout() {
        return flecs_h$shared.C_POINTER;
    }

    public static void set(MemorySegment segment, long offset, float value) {
        segment.set(flecs_h$shared.C_FLOAT, offset, value);
    }

    public static void set(MemorySegment segment, long offset, double value) {
        segment.set(flecs_h$shared.C_DOUBLE, offset, value);
    }

    public static void set(MemorySegment segment, long offset, short value) {
        segment.set(flecs_h$shared.C_SHORT, offset, value);
    }

    public static void set(MemorySegment segment, long offset, int value) {
        segment.set(flecs_h$shared.C_INT, offset, value);
    }

    public static void set(MemorySegment segment, long offset, long value) {
        segment.set(flecs_h$shared.C_LONG, offset, value);
    }

    public static void set(MemorySegment segment, long offset, boolean value) {
        segment.set(flecs_h$shared.C_BOOL, offset, value);
    }

    public static void set(MemorySegment segment, long offset, String value) {
        segment.set(flecs_h$shared.C_POINTER, offset, StringUtils.toMemorySegment(value));
    }

    public static float getFloat(MemorySegment segment, long offset) {
        return segment.get(flecs_h$shared.C_FLOAT, offset);
    }

    public static double getDouble(MemorySegment segment, long offset) {
        return segment.get(flecs_h$shared.C_DOUBLE, offset);
    }

    public static short getShort(MemorySegment segment, long offset) {
        return segment.get(flecs_h$shared.C_SHORT, offset);
    }

    public static int getInt(MemorySegment segment, long offset) {
        return segment.get(flecs_h$shared.C_INT, offset);
    }

    public static long getLong(MemorySegment segment, long offset) {
        return segment.get(flecs_h$shared.C_LONG, offset);
    }

    public static boolean getBoolean(MemorySegment segment, long offset) {
        return segment.get(flecs_h$shared.C_BOOL, offset);
    }

    public static String getString(MemorySegment segment, long offset) {
        return segment.get(flecs_h$shared.C_POINTER, offset).getString(0);
    }

    public static long offsetOf(MemoryLayout layout, String fieldName) {
        return layout.byteOffset(MemoryLayout.PathElement.groupElement(fieldName));
    }
}

