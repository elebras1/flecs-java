package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Flecs;
import com.github.elebras1.flecs.FlecsObserver;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Velocity;

import static com.github.elebras1.flecs.FlecsConstants.*;

public class ObserverExample {

    public static void main(String[] args) {
        try (Flecs world = new Flecs()) {
            System.out.println("=== Observer Example ===\n");

            // Register components
            world.component(Position.class);
            world.component(Velocity.class);

            // Example 1: OnAdd observer
            System.out.println("--- Example 1: OnAdd Observer ---");
            FlecsObserver onAddObserver = world.observer(Position.class)
                    .event(EcsOnAdd)
                    .each((entityId) -> {
                        System.out.println("OnAdd triggered for entity: " + entityId);
                    });

            Entity e1 = world.obtainEntity(world.entity("Entity1"));
            e1.set(new Position(10.0f, 20.0f)); // Should trigger OnAdd
            System.out.println();

            // Example 2: OnSet observer
            System.out.println("--- Example 2: OnSet Observer ---");
            world.observer(Position.class)
                    .event(EcsOnSet)
                    .iter((it) -> {
                        for (int i = 0; i < it.count(); i++) {
                            long entityId = it.entityId(i);
                            Position pos = world.obtainEntity(entityId).get(Position.class);
                            System.out.println("Position set to (" + pos.x() + ", " + pos.y() + ") for entity: " + entityId);
                        }
                    });

            e1.set(new Position(30.0f, 40.0f)); // Should trigger OnSet
            System.out.println();

            // Example 3: OnRemove observer
            System.out.println("--- Example 3: OnRemove Observer ---");
            world.observer(Position.class)
                    .event(EcsOnRemove)
                    .each((entityId) -> {
                        System.out.println("OnRemove triggered for entity: " + entityId);
                    });

            e1.remove(Position.class); // Should trigger OnRemove
            System.out.println();

            // Example 4: Multi-term observer
            System.out.println("--- Example 4: Multi-term Observer ---");
            world.observer()
                    .with(Position.class)
                    .with(Velocity.class)
                    .event(EcsOnAdd)
                    .each((entityId) -> {
                        System.out.println("Entity " + entityId + " now has both Position and Velocity!");
                    });

            Entity e2 = world.obtainEntity(world.entity("Entity2"));
            e2.set(new Position(5.0f, 5.0f)); // Won't trigger (missing Velocity)
            e2.set(new Velocity(1.0f, 1.0f)); // Should trigger (now has both)
            System.out.println();

            // Example 5: Multi-event observer
            System.out.println("--- Example 5: Multi-event Observer ---");
            world.observer(Velocity.class)
                    .event(EcsOnAdd)
                    .event(EcsOnRemove)
                    .iter((it) -> {
                        long eventId = it.event();
                        String eventName = (eventId == EcsOnAdd) ? "OnAdd" : "OnRemove";
                        System.out.println("Event " + eventName + " triggered for " + it.count() + " entities");
                    });

            Entity e3 = world.obtainEntity(world.entity("Entity3"));
            e3.set(new Velocity(2.0f, 2.0f)); // Should trigger OnAdd
            e3.remove(Velocity.class); // Should trigger OnRemove
            System.out.println();

            // Example 6: Monitor observer
            System.out.println("--- Example 6: Monitor Observer ---");
            world.observer()
                    .with(Position.class)
                    .with(Velocity.class)
                    .event(EcsMonitor)
                    .iter((it) -> {
                        long eventId = it.event();
                        String eventName = (eventId == EcsOnAdd) ? "started matching" : "stopped matching";
                        for (int i = 0; i < it.count(); i++) {
                            System.out.println("Entity " + it.entityId(i) + " " + eventName + " the query");
                        }
                    });

            Entity e4 = world.obtainEntity(world.entity("Entity4"));
            e4.set(new Position(0.0f, 0.0f));
            e4.set(new Velocity(0.0f, 0.0f)); // Should trigger "started matching"
            e4.remove(Position.class); // Should trigger "stopped matching"
            System.out.println();

            // Example 7: Yield existing
            System.out.println("--- Example 7: Yield Existing ---");
            Entity e5 = world.obtainEntity(world.entity("Entity5"));
            e5.set(new Position(100.0f, 100.0f));

            world.observer(Position.class)
                    .event(EcsOnAdd)
                    .yieldExisting()
                    .each((entityId) -> {
                        System.out.println("Yielded existing entity: " + entityId);
                    });
            System.out.println();

            // Example 8: Filter terms
            System.out.println("--- Example 8: Filter Terms ---");
            world.observer()
                    .with(Position.class)
                    .with(Velocity.class).filter() // Velocity is a filter term
                    .event(EcsOnAdd)
                    .each((entityId) -> {
                        System.out.println("Position added to entity with Velocity: " + entityId);
                    });

            Entity e6 = world.obtainEntity(world.entity("Entity6"));
            e6.set(new Velocity(1.0f, 1.0f));
            e6.set(new Position(50.0f, 50.0f)); // Should trigger (has Velocity)

            Entity e7 = world.obtainEntity(world.entity("Entity7"));
            e7.set(new Position(60.0f, 60.0f)); // Won't trigger (no Velocity)
            System.out.println();

            // Example 9: NOT operator
            System.out.println("--- Example 9: NOT Operator ---");
            world.observer(Position.class)
                    .without(Velocity.class)
                    .event(EcsOnAdd)
                    .each((entityId) -> {
                        System.out.println("Position added to entity WITHOUT Velocity: " + entityId);
                    });

            Entity e8 = world.obtainEntity(world.entity("Entity8"));
            e8.set(new Position(70.0f, 70.0f)); // Should trigger (no Velocity)

            Entity e9 = world.obtainEntity(world.entity("Entity9"));
            e9.set(new Velocity(2.0f, 2.0f));
            e9.set(new Position(80.0f, 80.0f)); // Won't trigger (has Velocity)
            System.out.println();

            System.out.println("=== Observer Example Complete ===");
        }
    }
}

