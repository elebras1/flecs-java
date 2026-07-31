package io.github.elebras1.flecs.examples.queries;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Gravity;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates singleton components. A singleton component lives on the world
 * entity and can be matched by queries as if every matching entity had it.
 */
public class Singleton {

    public static void main(String[] args) {
        World world = new World();

        // Register components and mark Gravity as a singleton.
        long gravityId = world.component(Gravity.class);
        world.component(Velocity.class);
        world.obtainEntity(gravityId).add(Flecs.Singleton);

        // Set the singleton value on the world entity.
        world.obtainEntity(Flecs.World).set(new Gravity(9.81f));

        // Create entities with Velocity.
        world.obtainEntity(world.entity("e1")).set(new Velocity(0, 0));
        world.obtainEntity(world.entity("e2")).set(new Velocity(0, 1));
        world.obtainEntity(world.entity("e3")).set(new Velocity(0, 2));

        // Query that matches Velocity and the Gravity singleton. The singleton
        // term is explicitly routed to the world entity.
        Query query = world.query()
                .with(Velocity.class)
                .with(Gravity.class)
                .src(Flecs.World)
                .build();

        // Use iter so we can read the singleton once and mutate each Velocity.
        query.iter(it -> {
            Field<Velocity> velocities = it.field(Velocity.class, 0);
            Field<Gravity> gravities = it.field(Gravity.class, 1);
            float gravity = gravities.get(0).value();

            for (int i = 0; i < it.count(); i++) {
                VelocityView vel = velocities.getMutView(i);
                vel.dy(vel.dy() + gravity);
                System.out.println("velocity is {" + vel.dx() + ", " + vel.dy() + "}");
            }
        });

        query.destroy();
        world.destroy();
    }

    // Output:
    // velocity is {0.0, 9.81}
    // velocity is {0.0, 10.81}
    // velocity is {0.0, 11.81}
}
