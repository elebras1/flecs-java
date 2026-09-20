package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.World;

@FunctionalInterface
public interface GroupCreateCallback {
    Object accept(World world, long groupId, Object ctx);
}
