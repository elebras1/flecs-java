package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Flecs;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Velocity;

public class JsonSerializationExample {

    public static void main(String[] args) {
        // Creating and populating the source world
        String json;
        try (Flecs world = new Flecs()) {
            // Registering components
            world.component(Position.class);
            world.component(Velocity.class);

            // Creating entities with components
            long playerId = world.entity("Player");
            world.obtainEntity(playerId).set(new Position(10.0f, 20.0f)).set(new Velocity(1.5f, -0.5f));

            long enemyId = world.entity("Enemy");
            world.obtainEntity(enemyId).set(new Position(50.0f, 30.0f)).set(new Velocity(-2.0f, 1.0f));

            json = world.toJson();
            System.out.println("=== Serialized JSON (without built-ins) ===");
            System.out.println(json);
            System.out.println();

            // With built-in components and modules
            String jsonComplete = world.toJson(true, true);
            System.out.println("=== Serialized JSON (complete) ===");
            System.out.println(jsonComplete);
            System.out.println();
        }

        // Deserialization into a new world
        try (Flecs newWorld = new Flecs()) {
            // Registering components
            newWorld.component(Position.class);
            newWorld.component(Velocity.class);

            newWorld.fromJson(json);

            // Checking loaded entities
            long playerLoaded = newWorld.lookup("Player");
            long enemyLoaded = newWorld.lookup("Enemy");

            System.out.println("=== Entities loaded from JSON ===");
            System.out.println("Player ID: " + playerLoaded);
            System.out.println("Enemy ID: " + enemyLoaded);
        }
    }
}
