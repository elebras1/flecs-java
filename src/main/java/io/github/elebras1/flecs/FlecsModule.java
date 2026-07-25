package io.github.elebras1.flecs;

public interface FlecsModule {
    void initModule(World world);

    default String name() {
        return getClass().getSimpleName();
    }
}
