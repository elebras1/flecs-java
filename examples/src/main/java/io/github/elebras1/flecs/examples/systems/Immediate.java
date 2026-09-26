package io.github.elebras1.flecs.examples.systems;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Plate;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.examples.components.Waiter;

/**
 * Demonstrates an immediate system. By default, the world is in readonly mode
 * while systems run, which defers structural changes until the end of the
 * frame. An immediate system takes the world out of readonly mode so that a
 * system can make changes that are visible immediately.
 */
public class Immediate {

    public static void main(String[] args) {
        World world = new World();
        world.component(Waiter.class);
        world.component(Plate.class);

        // Create a query that finds all waiters without a plate.
        Query qWaiter = world.query()
                .with(Waiter.class)
                .without(Plate.class, Flecs.Wildcard)
                .build();

        // System that assigns plates to waiters. By making this system immediate,
        // plate assignments are applied directly (not deferred), which ensures
        // that we won't assign plates to the same waiter more than once.
        world.system("AssignPlate")
                .with(Plate.class)
                .without(Waiter.class, Flecs.Wildcard)
                .immediate()
                .each(entityId -> {
                    EntityView plate = world.obtainEntityView(entityId);

                    // Find an available waiter.
                    long waiterRaw = qWaiter.first();
                    if (waiterRaw == 0) {
                        // No available waiters.
                        return;
                    }
                    EntityView waiter = world.obtainEntityView(waiterRaw);

                    // Suspend deferring so the waiter gets the plate
                    // immediately. Even in an immediate system, deferring is
                    // still enabled by default, as adding/removing
                    // components to the entities being iterated would
                    // interfere with the system iterator.
                    world.deferSuspend();
                    waiter.add(Plate.class, plate);
                    world.deferResume();

                    // Now that deferring is resumed, also add the waiter to
                    // the plate. We can't do this while deferring is
                    // suspended because the plate is the entity we're
                    // iterating.
                    plate.add(Waiter.class, waiter);

                    System.out.println("Assigned " + waiter.name() + " to " + plate.name() + "!");
                });

        // Create a few waiters and plates.
        Entity waiter1 = world.obtainEntity(world.entity("waiter_1")).add(Waiter.class);
        world.obtainEntity(world.entity("waiter_2")).add(Waiter.class);
        world.obtainEntity(world.entity("waiter_3")).add(Waiter.class);

        world.obtainEntity(world.entity("plate_1")).add(Plate.class);
        Entity plate2 = world.obtainEntity(world.entity("plate_2")).add(Plate.class);
        world.obtainEntity(world.entity("plate_3")).add(Plate.class);

        // waiter_1 already has a plate (plate_2).
        waiter1.add(Plate.class, plate2);
        plate2.add(Waiter.class, waiter1);

        // Run systems.
        world.progress();

        world.destroy();
    }

    // Output:
    // Assigned waiter_3 to plate_1!
    // Assigned waiter_2 to plate_3!
}
