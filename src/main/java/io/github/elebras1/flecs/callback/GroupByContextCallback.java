package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.Table;
import io.github.elebras1.flecs.World;

@FunctionalInterface
public interface GroupByContextCallback {
    long accept(World world, Table table, long id, Object ctx);
}
