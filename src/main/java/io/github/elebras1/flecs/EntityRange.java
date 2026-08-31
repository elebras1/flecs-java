package io.github.elebras1.flecs;

public record EntityRange(int min, int max, int current, long[] recycled) {
}
