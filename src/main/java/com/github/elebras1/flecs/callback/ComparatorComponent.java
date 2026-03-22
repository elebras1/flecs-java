package com.github.elebras1.flecs.callback;

@FunctionalInterface
public interface ComparatorComponent {
    <CA, CB> int compare(CA componentA, CB componentB);
}
