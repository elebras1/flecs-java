package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Shows how to annotate a system that deletes entities so that the scheduler
 * can insert the correct sync points. The C++ version uses a wildcard write
 * annotation; this Java port marks the matched Position component as written,
 * which has the same effect of forcing a sync point.
 */
public class SyncPointDelete {

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        // Basic move system.
        world.system("Move")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .with(Velocity.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        Position p = positions.get(i);
                        Velocity v = velocities.get(i);
                        positions.set(i, new Position(p.x() + v.dx(), p.y() + v.dy()));
                    }
                });

        // Delete entities when p.x >= 3. The deletion is a structural change
        // that removes Position, so mark Position as written. This forces the
        // scheduler to insert a sync point before the following system.
        world.system("DeleteEntity")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .out()
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        Position p = positions.get(i);
                        if (p.x() >= 3) {
                            Entity entity = world.obtainEntity(it.entity(i));
                            System.out.println("Delete entity " + entity.name());
                            entity.destruct();
                        }
                    }
                });

        // Print resulting Position.
        world.system("PrintPosition")
                .kind(Flecs.OnUpdate)
                .with(Position.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    for (int i = 0; i < it.count(); i++) {
                        Entity entity = world.obtainEntity(it.entity(i));
                        Position p = positions.get(i);
                        System.out.println(entity.name() + ": {" + p.x() + ", " + p.y() + "}");
                    }
                });

        world.obtainEntity(world.entity("e1"))
                .set(new Position(0, 0))
                .set(new Velocity(1, 2));

        world.obtainEntity(world.entity("e2"))
                .set(new Position(1, 2))
                .set(new Velocity(1, 2));

        // Run until all entities are deleted.
        while (world.count(Position.class) > 0) {
            world.progress();
        }

        world.destroy();
    }

    // Output:
    // e1: {1.0, 2.0}
    // e2: {2.0, 4.0}
    // Delete entity e2
    // e1: {2.0, 4.0}
    // e2: {3.0, 6.0}
    // Delete entity e1
    // e1: {3.0, 6.0}
}
