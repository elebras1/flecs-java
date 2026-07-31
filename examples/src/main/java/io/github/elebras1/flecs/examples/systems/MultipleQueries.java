package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Bee;
import io.github.elebras1.flecs.examples.components.Flower;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.util.Flecs;

import java.util.Random;

/**
 * Shows how to pass one or more existing queries to a system, which allows
 * for systems that iterate queries multiple times and/or iterate multiple
 * queries.
 */
public class MultipleQueries {

    private static final int MAX_RANGE = 10;

    private static float randf(Random r, int n) {
        return r.nextInt(n);
    }

    private static float pow2(float n) {
        return n * n;
    }

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Bee.class);
        world.component(Flower.class);

        // Create two queries. Since they'll stick around until the end of the
        // ECS world, give them a name which makes them easier to find in the
        // explorer. Named queries default to cached.
        Query bees = world.query().with(Position.class).with(Bee.class).cached().build();
        Query flowers = world.query().with(Position.class).with(Flower.class).cached().build();

        // Find the closest in range flower for each bee.
        // The Java binding currently requires systems to have at least one
        // matching term to be scheduled, so we provide a dummy Position term.
        world.system("FlowersAndTheBees")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .run(it -> {
                    bees.iter(beeIt -> {
                        for (int b = 0; b < beeIt.count(); b++) {
                            final int beeIndex = b;
                            long beeId = beeIt.entity(beeIndex);
                            Position pBee = beeIt.field(Position.class, 0).get(beeIndex);

                            SearchResult result = findClosestFlower(pBee, flowers);

                            if (result.flower != 0) {
                                beeIt.field(Bee.class, 1).set(beeIndex, new Bee(result.flower));
                                Entity beeEntity = world.obtainEntity(beeId);
                                Entity flowerEntity = world.obtainEntity(result.flower);
                                System.out.println("Bee " + beeEntity.name() + " picked flower " + flowerEntity.name());
                            }
                        }
                    });
                });

        // Find bees that picked the same flower.
        world.system("BumpingBees")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .run(it -> {
                    bees.iter(beeIt -> {
                        for (int i = 0; i < beeIt.count(); i++) {
                            final int idx = i;
                            long bee1 = beeIt.entity(idx);
                            Bee b1 = beeIt.field(Bee.class, 1).get(idx);
                            if (b1.flower() == 0) {
                                continue;
                            }

                            bees.iter(otherBeeIt -> {
                                for (int j = 0; j < otherBeeIt.count(); j++) {
                                    long bee2 = otherBeeIt.entity(j);
                                    Bee b2 = otherBeeIt.field(Bee.class, 1).get(j);
                                    if (bee1 > bee2 && b2.flower() != 0 && b1.flower() == b2.flower()) {
                                        Entity e1 = world.obtainEntity(bee1);
                                        Entity e2 = world.obtainEntity(bee2);
                                        System.out.println("Bee " + e1.name() + " and bee " + e2.name() + " bumped into each other");
                                    }
                                }
                            });
                        }
                    });
                });

        // Create flowers and bees.
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            world.obtainEntity(world.entity("Bee_" + i))
                    .set(new Position(randf(rand, 20), randf(rand, 20)))
                    .set(new Bee(0));
        }
        for (int i = 0; i < 10; i++) {
            world.obtainEntity(world.entity("Flower_" + i))
                    .set(new Position(randf(rand, 20), randf(rand, 20)))
                    .set(new Flower());
        }

        // Dummy entity so the first system has a matching term and runs.
        world.obtainEntity(world.entity("Dummy")).set(new Position(0, 0));

        world.progress(0.016f);

        bees.destroy();
        flowers.destroy();
        world.destroy();
    }

    private static SearchResult findClosestFlower(Position beePos, Query flowers) {
        float dsqrMin = 2 * pow2(MAX_RANGE);
        float[] minDist = {dsqrMin};
        long[] bestFlower = {0};
        flowers.iter(flowerIt -> {
            for (int f = 0; f < flowerIt.count(); f++) {
                Position pFlower = flowerIt.field(Position.class, 0).get(f);
                float dx = beePos.x() - pFlower.x();
                float dy = beePos.y() - pFlower.y();
                float dsqr = dx * dx + dy * dy;
                if (dsqr < minDist[0]) {
                    minDist[0] = dsqr;
                    bestFlower[0] = flowerIt.entity(f);
                }
            }
        });
        return new SearchResult(minDist[0], bestFlower[0]);
    }

    private record SearchResult(float distance, long flower) {
    }

    // Output (entity names are non-deterministic):
    // Bee Bee_... picked flower Flower_...
    // ...
    // Bee Bee_... and bee Bee_... bumped into each other
}
