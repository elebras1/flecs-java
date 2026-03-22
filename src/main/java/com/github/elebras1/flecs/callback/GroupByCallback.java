package com.github.elebras1.flecs.callback;

import com.github.elebras1.flecs.Table;
import com.github.elebras1.flecs.World;

@FunctionalInterface
public interface GroupByCallback {
    long accept(World world, Table table, long id);
}
