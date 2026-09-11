package io.github.elebras1.flecs;

@FunctionalInterface
public interface IterWithIndexCallback {
    void accept(Iter iter, int index);
}
