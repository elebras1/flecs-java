package io.github.elebras1.flecs.callback;

import io.github.elebras1.flecs.World;

@FunctionalInterface
public interface GroupDeleteCallback {
    void accept(World world, long groupId, Object groupCtx, Object ctx);
}
