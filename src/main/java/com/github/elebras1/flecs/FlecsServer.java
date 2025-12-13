package com.github.elebras1.flecs;

import java.lang.foreign.MemorySegment;

public class FlecsServer {
    private final MemorySegment serverSegment;

    FlecsServer(MemorySegment serverSegment) {
        this.serverSegment = serverSegment;
    }

    MemorySegment getServerSegment() {
        return this.serverSegment;
    }
}
