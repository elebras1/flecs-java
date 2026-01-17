package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;

public class CustomEventExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);

            long clickedEvent = world.entity("Clicked");
            long damagedEvent = world.entity("Damaged");
            long healedEvent = world.entity("Healed");

            world.observer().event(clickedEvent).with(Position.class).each(entityId -> System.out.println("Entity " + entityId + " clicked"));

            Entity button = world.obtainEntity(world.entity("Button"));
            button.set(new Position(100.0f, 50.0f));
            button.emit(clickedEvent, Position.class);

            world.observer().event(damagedEvent).event(healedEvent).with(Position.class).iter(it -> {
                String event = (it.event() == damagedEvent) ? "Damaged" : "Healed";
                for (int i = 0; i < it.count(); i++) {
                    System.out.println("Entity " + it.entityId(i) + " " + event);
                }
            });

            Entity player = world.obtainEntity(world.entity("Player"));
            player.set(new Position(200.0f, 100.0f));
            player.emit(damagedEvent, Position.class);
            player.emit(healedEvent, Position.class);

            Entity widget = world.obtainEntity(world.entity("Widget"));
            widget.set(new Position(300.0f, 150.0f));

            widget.observe(clickedEvent, () -> 
                System.out.println("Widget clicked")
            );

            widget.emit(clickedEvent);

            Entity other = world.obtainEntity(world.entity("Other"));
            other.set(new Position(400.0f, 200.0f));
            other.emit(clickedEvent);
        }
    }
}

