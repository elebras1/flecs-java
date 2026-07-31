package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Radius;

import java.util.Random;

/**
 * Shows how to pass one or more existing queries to a system. In Java the
 * query can simply be captured by the system callback, so no explicit context
 * API is required. This example uses a second query to implement simple
 * collision detection.
 */
public class SystemCtx {

    private static float sqr(float value) {
        return value * value;
    }

    private static float randf(Random random, int max) {
        return random.nextInt(max);
    }

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Radius.class);

        Query qCollide = world.query()
                .with(Position.class)
                .with(Radius.class)
                .build();

        // The captured query is the Java equivalent of the C++ system context.
        FlecsSystem collideSystem = world.system("Collide")
                .with(Position.class)
                .with(Radius.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Radius> radii = it.field(Radius.class, 1);

                    for (int i = 0; i < it.count(); i++) {
                        long e1 = it.entity(i);
                        Position p1 = positions.get(i);
                        Radius r1 = radii.get(i);

                        qCollide.iter(otherIt -> {
                            Field<Position> otherPositions = otherIt.field(Position.class, 0);
                            Field<Radius> otherRadii = otherIt.field(Radius.class, 1);

                            for (int j = 0; j < otherIt.count(); j++) {
                                long e2 = otherIt.entity(j);

                                // Don't collide with self.
                                if (e1 == e2) {
                                    continue;
                                }

                                // Prevent collisions from being detected twice
                                // with the entities reversed.
                                if (e1 > e2) {
                                    continue;
                                }

                                Position p2 = otherPositions.get(j);
                                Radius r2 = otherRadii.get(j);

                                // Check for collision.
                                float dx = p2.x() - p1.x();
                                float dy = p2.y() - p1.y();
                                float dSqr = dx * dx + dy * dy;
                                float rSqr = sqr(r1.value() + r2.value());

                                if (rSqr > dSqr) {
                                    System.out.println(e1 + " and " + e2
                                            + " collided!");
                                }
                            }
                        });
                    }
                });

        // Create a few test entities.
        Random random = new Random(42);
        for (int i = 0; i < 10; i++) {
            world.obtainEntity(world.entity())
                    .set(new Position(randf(random, 100), randf(random, 100)))
                    .set(new Radius(randf(random, 10) + 1));
        }

        // Run the system.
        collideSystem.run();

        world.destroy();
    }

    // Output (entity ids are non-deterministic):
    // 526 and 529 collided!
    // 527 and 534 collided!
    // 529 and 531 collided!
    // 533 and 535 collided!
}
