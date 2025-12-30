package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class DirectFieldAccessExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            System.out.println("=== Direct Field Access Example ===\n");

            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Health.class);
            
            long tagEnemy = world.entity("Enemy");;

            // Create entities
            for (int i = 0; i < 3; i++) {
                long uniqueTag = world.entity("UniqueTag_" + i);
                world.obtainEntity(world.entity("Entity_" + i))
                    .set(new Position(i * 10.0f, i * 5.0f))
                    .set(new Velocity(1.0f + i, 0.5f + i))
                    .set(new Health(100 - i * 10)).add(tagEnemy).add(uniqueTag);
            }

            try (Query query = world.query().with(Position.class).with(Velocity.class).with(Health.class).build()) {

                System.out.println("\nDirect access (no allocation):");
                query.iter(iter -> {
                    for (int i = 0; i < iter.count(); i++) {
                        float x = iter.fieldFloat(Position.class, 0, "x", i);
                        float y = iter.fieldFloat(Position.class, 0, "y", i);
                        int health = iter.fieldInt(Health.class, 2, "value", i);
                        System.out.printf("  pos=(%.1f, %.1f) health=%d%n", x, y, health);

                        iter.setFieldFloat(Position.class, 0, "x", i, x + 1.0f);
                        iter.setFieldFloat(Position.class, 0, "y", i, y + 1.0f);
                        iter.setFieldInt(Health.class, 2, "value", i, health - 1);

                        x = iter.fieldFloat(Position.class, 0, "x", i);
                        y = iter.fieldFloat(Position.class, 0, "y", i);
                        health = iter.fieldInt(Health.class, 2, "value", i);
                        System.out.println("    After update: pos=(" + x + ", " + y + ") health=" + health);
                    }
                });
            }


            try (Query query = world.query().with(tagEnemy).with(Health.class).build()) {
                System.out.println("\nDirect access with tag:");
                query.iter(iter -> {
                    for (int i = 0; i < iter.count(); i++) {
                        int health = iter.fieldInt(Health.class, 1, "value", i);
                        System.out.println("health=" + health);
                    }
                });
            }
        }
    }
}

