package com.github.elebras1.flecs.util;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class StringUtils {

    public static MemorySegment toMemorySegment(String value) {
        if (value == null) {
            return MemorySegment.NULL;
        }
        return Arena.ofAuto().allocateFrom(value);
    }
}
