package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.Table;
import io.github.elebras1.flecs.World;

@FunctionalInterface
public interface GroupByCallback {
    long accept(World world, Table table, long id);
}
