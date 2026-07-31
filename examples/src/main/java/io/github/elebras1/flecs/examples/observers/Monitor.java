package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates a monitor observer, which triggers when an entity starts or stops
 * matching the observer query. Entering is reported as OnAdd, leaving as OnRemove.
 */
public class Monitor {

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);

        world.observer()
                .event(Flecs.Monitor)
                .with(Position.class)
                .with(Velocity.class)
                .iter(it -> {
                    long event = it.event();
                    for (int i = 0; i < it.count(); i++) {
                        String name = world.obtainEntity(it.entity(i)).name();
                        if (event == Flecs.OnAdd) {
                            System.out.println(" - Enter: Velocity: " + name);
                        } else if (event == Flecs.OnRemove) {
                            System.out.println(" - Leave: Position: " + name);
                        }
                    }
                });

        // Create entity. Setting Position alone does not match the query.
        long e = world.entity("e");
        world.obtainEntity(e).set(new Position(10, 20));

        // Now the entity matches: monitor reports Enter.
        world.obtainEntity(e).set(new Velocity(1, 2));

        // The entity no longer matches: monitor reports Leave.
        world.obtainEntity(e).remove(Position.class);

        world.destroy();
    }

    // Output:
    //  - Enter: Velocity: e
    //  - Leave: Position: e
}
