package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface ComparatorId {
    int compare(long idA, long idB);
}
