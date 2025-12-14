package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.collection.EcsLongList;
import com.github.elebras1.flecs.examples.components.*;

public class QueryFeaturesExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            System.out.println("=== Query Features Example ===\n");

            // Register components
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);

            // Create tags
            long playerTag = world.entity("PlayerTag");
            long enemyTag = world.entity("EnemyTag");

            // Create entities
            Entity player = world.obtainEntity(world.entity("Player"));
            player.add(playerTag)
                    .set(new Position(10, 20))
                    .set(new Velocity(5, 0))
                    .set(new Health(100));

            Entity enemy1 = world.obtainEntity(world.entity("Enemy1"));
            enemy1.add(enemyTag)
                    .set(new Position(50, 30))
                    .set(new Velocity(-2, 1))
                    .set(new Health(50));

            Entity enemy2 = world.obtainEntity(world.entity("Enemy2"));
            enemy2.add(enemyTag)
                    .set(new Position(80, 40))
                    .set(new Health(75));

            Entity obstacle = world.obtainEntity(world.entity("Obstacle"));
            obstacle.set(new Position(100, 100));

            // --- Feature 1: with() simple ---
            System.out.println("--- 1. Query with Position only ---");
            try (Query q1 = world.query().with(Position.class).build()) {
                System.out.println("Count: " + q1.count());
                q1.each(entityId -> {
                    Entity e = world.obtainEntity(entityId);
                    System.out.println("  - " + e.getName());
                });
            }

            // --- Feature 2: with() multiple (AND implicite) ---
            System.out.println("\n--- 2. Query with Position AND Velocity ---");
            try (Query q2 = world.query()
                    .with(Position.class)
                    .with(Velocity.class)
                    .build()) {
                System.out.println("Count: " + q2.count());
                q2.each(entityId -> {
                    Entity e = world.obtainEntity(entityId);
                    System.out.println("  - " + e.getName());
                });
            }

            // --- Feature 3: with() + tag ---
            System.out.println("\n--- 3. Query enemies with Health ---");
            try (Query q3 = world.query()
                    .with(enemyTag)
                    .with(Health.class)
                    .build()) {
                System.out.println("Count: " + q3.count());
            }

            // --- Feature 4: OR operator ---
            System.out.println("\n--- 4. Query Position OR Velocity ---");
            try (Query q4 = world.query()
                    .with(Position.class).or()
                    .with(Velocity.class)
                    .build()) {
                System.out.println("Count: " + q4.count());
            }

            // --- Feature 5: NOT operator ---
            System.out.println("\n--- 5. Query Position NOT Velocity ---");
            try (Query q5 = world.query()
                    .with(Position.class)
                    .with(Velocity.class).not()
                    .build()) {
                System.out.println("Count: " + q5.count());
                q5.each(entityId -> {
                    Entity e = world.obtainEntity(entityId);
                    System.out.println("  - " + e.getName());
                });
            }

            // --- Feature 6: Optional ---
            System.out.println("\n--- 6. Query Position + Velocity (optional) ---");
            try (Query q6 = world.query()
                    .with(Position.class)
                    .with(Velocity.class).optional()
                    .build()) {
                System.out.println("Count: " + q6.count());
            }

            // --- Feature 7: iter() + field() ---
            System.out.println("\n--- 7. Using iter() and field() ---");
            try (Query q7 = world.query()
                    .with(Position.class)
                    .with(Velocity.class)
                    .build()) {
                q7.iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);

                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Velocity vel = velocities.get(i);
                        Entity e = world.obtainEntity(it.entity(i));
                        System.out.printf("  %s: pos=(%.1f,%.1f) vel=(%.1f,%.1f)%n", e.getName(), pos.x(), pos.y(), vel.dx(), vel.dy());
                    }
                });
            }

            // --- Feature 8: run() callback ---
            System.out.println("\n--- 8. Using run() callback ---");
            try (Query q8 = world.query()
                    .with(Position.class)
                    .with(Health.class)
                    .build()) {
                q8.run(it -> {
                    System.out.println("  Field count: " + it.fieldCount());
                    System.out.println("  Total entities: " + q8.count());

                    while (it.next()) {
                        for (int i = 0; i < it.count(); i++) {
                            Entity e = world.obtainEntity(it.entity(i));
                            Health h = e.get(Health.class);
                            System.out.println("  - " + e.getName() + " HP: " + h.value());
                        }
                    }
                });
            }

            // --- Feature 9: in/out modifiers ---
            System.out.println("\n--- 9. Query with in/out modifiers ---");
            try (Query q9 = world.query()
                    .with(Position.class).in()
                    .with(Velocity.class).out()
                    .build()) {
                System.out.println("Count: " + q9.count());
            }

            // --- Feature 10: entities() list ---
            System.out.println("\n--- 10. Get all entities as list ---");
            try (Query q10 = world.query().with(enemyTag).build()) {
                EcsLongList entities = q10.entities();
                System.out.println("Enemies: " + entities.size());
                for (int i = 0; i < entities.size(); i++) {
                    Entity e = world.obtainEntity(entities.get(i));
                    System.out.println("  - " + e.getName());
                }
            }

            System.out.println("\n=== Example completed! ===");
        }
    }
}
