package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;

/**
 * Demonstrates enqueuing a custom event while the world is in deferred mode.
 * The observer is invoked when the command queue is flushed.
 */
public class EnqueueEvent {

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        long myEvent = world.entity("MyEvent");

        world.observer()
                .event(myEvent)
                .with(Position.class)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    String eventName = world.obtainEntity(it.event()).name();
                    for (int i = 0; i < it.count(); i++) {
                        Position p = positions.get(i);
                        System.out.println(" - " + eventName + ": Position: " + world.obtainEntity(it.entity(i)).name() + ": {" + p.x() + ", " + p.y() + "}");
                    }
                });

        Entity e = world.obtainEntity(world.entity("e"))
                .set(new Position(10, 20));

        // Emitting an event while deferred places it in the command queue.
        world.deferBegin();
        System.out.println("Event enqueued!");
        e.emit(myEvent, Position.class);
        world.deferEnd();

        world.destroy();
    }

    // Output:
    // Event enqueued!
    //  - MyEvent: Position: e: {10.0, 20.0}
}
