package io.github.elebras1.flecs;

public record WorldStats(long entityCount,
                         long notAliveCount,
                         long tableCount,
                         long emptyTableCount,
                         long tagCount,
                         long componentCount,
                         long pairCount,
                         long typeCount,
                         long queryCount,
                         long observerCount,
                         long systemCount,
                         double systemsRan,
                         double observersRan,
                         double eventEmitCount,
                         float deltaTime,
                         float fps) {
}
