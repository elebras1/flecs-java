package io.github.elebras1.flecs.examples.observers;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;

public class CustomEvent {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        // Define a custom event. Any plain entity can be used as an event.
        long clickedEvent = world.entity("Clicked");

        // Create an observer that listens for the custom event on entities
        // that have a Position component.
        world.observer()
                .event(clickedEvent)
                .with(Position.class)
                .each(entityId -> System.out.println("Entity " + entityId + " clicked"));

        // Create an entity with Position and emit the custom event.
        // The event is matched against the entity, so the entity must have
        // the Position component for the observer to trigger.
        Entity button = world.obtainEntity(world.entity("Button"));
        button.set(new Position(100.0f, 50.0f));
        button.emit(clickedEvent, Position.class);

        world.destroy();
    }

    // Output:
    // Entity <id> clicked
}
