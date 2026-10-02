package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;

/**
 * Demonstrates enqueuing a custom event while the world is in deferred mode.
 * The observer is invoked when the command queue is flushed.
 */
public class EnqueueEvent {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            long myEvent = world.entity("MyEvent");

            world.observer()
                    .event(myEvent)
                    .with(Position.class)
                    .each(Position.class, (Iter it, int index, Position p) -> {
                        String eventName = world.obtainEntity(it.event()).name();
                        System.out.println(" - " + eventName + ": Position: " + world.obtainEntity(it.entity(index)).name() + ": {" + p.x() + ", " + p.y() + "}");
                    });

            Entity e = world.obtainEntity(world.entity("e"))
                    .set(new Position(10, 20));

            // Enqueuing an event while deferred places it in the command queue.
            world.deferBegin();
            System.out.println("Event enqueued!");
            e.enqueue(myEvent, Position.class);
            world.deferEnd();

        }
    }

    // Output:
    // Event enqueued!
    //  - MyEvent: Position: e: {10.0, 20.0}
}
