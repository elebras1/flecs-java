package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.*;

import java.util.ArrayList;
import java.util.List;

public class StandaloneViewExample {
    public static void main(String[] args) {
        try (World world = new World()) {
            // Register components
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Inventory.class);

            List<Entity> entities = new ArrayList<>();

            // Create entities with initial data (Records)
            for (int i = 0; i < 20; i++) {
                long entityId = world.entity("Entity_" + i);
                Entity entity = world.obtainEntity(entityId);
                entity.set(new Position(i * 10.0f, i * 5.0f));
                entity.set(new Velocity(1.0f, 0.5f));
                entities.add(entity);
            }

            // getMutView() requires an active scope to function properly.
            world.scope(() -> {
                for(Entity entity : entities) {
                    PositionView positionView = entity.getMutView(Position.class);
                    VelocityView velocityView = entity.getMutView(Velocity.class);
                    System.out.println("Entity: " + entity.id() + " Position(" + positionView.x() + ", " + positionView.y() + ") Velocity(" + velocityView.dx() + ", " + velocityView.dy() + ")");
                }
            });
        }
    }
}
