package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;

public class QueryBasics {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        Entity player = world.obtainEntity(world.entity("Player"))
                .set(new Position(0, 0))
                .set(new Velocity(1, 0.5f));

        Entity enemy1 = world.obtainEntity(world.entity("Enemy1"))
                .set(new Position(10, 5))
                .set(new Velocity(-0.5f, 0));

        Entity enemy2 = world.obtainEntity(world.entity("Enemy2"))
                .set(new Position(-5, 10))
                .set(new Velocity(0, -1));

        // This entity will not match as it does not have Velocity.
        world.obtainEntity(world.entity("Obstacle"))
                .set(new Position(20, 20));

        Query query = world.query().with(Position.class).with(Velocity.class).build();
        System.out.println("Entities with Position and Velocity: " + query.count());

        query.each(Position.class, (entityId, pos) -> {
            Entity entity = world.obtainEntity(entityId);
            System.out.println("  - " + entity.name() + " at position (" + pos.x() + ", " + pos.y() + ")");
        });

        System.out.println();

        query.iter(it -> {
            Field<Position> positions = it.field(Position.class, 0);
            Field<Velocity> velocities = it.field(Velocity.class, 1);

            for (int i = 0; i < it.count(); i++) {
                Position pos = positions.get(i);
                Velocity vel = velocities.get(i);
                Entity entity = world.obtainEntity(it.entity(i));

                System.out.println(entity.name() + ": pos={" + pos.x() + ", " + pos.y() + "}, vel={" + vel.dx() + ", " + vel.dy() + "}");
            }
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // Entities with Position and Velocity: 3
    //   - Player at position (0.0, 0.0)
    //   - Enemy1 at position (10.0, 5.0)
    //   - Enemy2 at position (-5.0, 10.0)
    //
    // Player: pos={0.0, 0.0}, vel={1.0, 0.5}
    // Enemy1: pos={10.0, 5.0}, vel={-0.5, 0.0}
    // Enemy2: pos={-5.0, 10.0}, vel={0.0, -1.0}
}
