package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;

public class CustomEventExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            System.out.println("=== Custom Event Example ===\n");

            // Register components
            world.component(Position.class);

            // Create custom event entities
            long clickedEvent = world.entity("Clicked");
            long damagedEvent = world.entity("Damaged");
            long healedEvent = world.entity("Healed");

            // Example 1: Custom event observer
            System.out.println("--- Example 1: Custom Event Observer ---");
            world.observer()
                    .event(clickedEvent)
                    .with(Position.class)
                    .each(entityId -> 
                        System.out.println("Entity " + entityId + " was clicked!")
                    );

            Entity button = world.obtainEntity(world.entity("Button"));
            button.set(new Position(100.0f, 50.0f));
            button.emit(clickedEvent, Position.class);
            System.out.println();

            // Example 2: Multiple custom events
            System.out.println("--- Example 2: Multiple Custom Events ---");
            world.observer()
                    .event(damagedEvent)
                    .event(healedEvent)
                    .with(Position.class)
                    .iter(it -> {
                        String eventName = (it.event() == damagedEvent) ? "Damaged" : "Healed";
                        for (int i = 0; i < it.count(); i++) {
                            System.out.println("Entity " + it.entityId(i) + " received " + eventName + " event");
                        }
                    });

            Entity player = world.obtainEntity(world.entity("Player"));
            player.set(new Position(200.0f, 100.0f));
            
            player.emit(damagedEvent, Position.class);
            player.emit(healedEvent, Position.class);
            System.out.println();

            // Example 3: Entity-specific observer
            System.out.println("--- Example 3: Entity-Specific Observer ---");
            Entity widget = world.obtainEntity(world.entity("Widget"));
            widget.set(new Position(300.0f, 150.0f));

            widget.observe(clickedEvent, () -> 
                System.out.println("Widget was clicked!")
            );

            // This will trigger the observer
            widget.emit(clickedEvent);
            
            // Creating another entity and emitting event won't trigger the widget's observer
            Entity otherWidget = world.obtainEntity(world.entity("OtherWidget"));
            otherWidget.set(new Position(400.0f, 200.0f));
            otherWidget.emit(clickedEvent); // Won't print "Widget was clicked!"
            System.out.println();

            // Example 4: Event without component
            System.out.println("--- Example 4: Event Without Component ---");
            long updateEvent = world.entity("Update");
            
            world.observer()
                    .event(updateEvent)
                    .with(Position.class)
                    .each(entityId -> 
                        System.out.println("Update event for entity: " + entityId)
                    );

            Entity obj = world.obtainEntity(world.entity("GameObject"));
            obj.set(new Position(500.0f, 250.0f));
            obj.emit(updateEvent, Position.class);
            System.out.println();

            System.out.println("=== Custom Event Example Complete ===");
        }
    }
}

