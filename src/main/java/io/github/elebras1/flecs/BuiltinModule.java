package io.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public final class BuiltinModule {

    private final String name;
    private final MemorySegment importFunction;

    BuiltinModule(String name, MemorySegment importFunction) {
        this.name = name;
        this.importFunction = importFunction;
    }

    public String name() {
        return this.name;
    }

    MemorySegment importFunction() {
        return this.importFunction;
    }
}
