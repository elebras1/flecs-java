package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;
import com.github.elebras1.flecs.util.FlecsConstants;

public class SystemExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            long posId = world.component(Position.class);
            long velId = world.component(Velocity.class);

            Entity player = world.obtainEntity(world.entity("Player"));
            player.set(new Position(0, 0)).set(new Velocity(1, 0.5f));

            Entity enemy1 = world.obtainEntity(world.entity("Enemy1"));
            enemy1.set(new Position(10, 5)).set(new Velocity(-0.5f, 0));

            Entity enemy2 = world.obtainEntity(world.entity("Enemy2"));
            enemy2.set(new Position(-5, 10)).set(new Velocity(0, -1));

            FlecsSystem moveSystem = world.system("MoveSystem")
                .with(Position.class)
                .with(Velocity.class)
                .kind(FlecsConstants.EcsOnUpdate)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    
                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Velocity vel = velocities.get(i);
                        
                        float newX = pos.x() + vel.dx() * it.deltaTime();
                        float newY = pos.y() + vel.dy() * it.deltaTime();

                        Entity entity = world.obtainEntity(it.entity(i));
                        entity.set(new Position(newX, newY));
                        System.out.printf("%s: (%.2f, %.2f) -> (%.2f, %.2f)%n",
                            entity.getName(), pos.x(), pos.y(), newX, newY);
                    }
                });

            FlecsSystem debugSystem = world.system("DebugSystem")
                .with(posId)
                .kind(FlecsConstants.EcsPostUpdate)
                .each(entityId -> {
                    Entity entity = world.obtainEntity(entityId);
                    Position pos = entity.get(Position.class);
                    System.out.printf("Debug: %s at (%.2f, %.2f)%n", entity.getName(), pos.x(), pos.y());
                });

            FlecsSystem taskSystem = world.system("TaskSystem")
                .kind(FlecsConstants.EcsPreUpdate)
                .run(it -> System.out.println("Frame starting..."));

            for (int frame = 0; frame < 3; frame++) {
                System.out.println("=== Frame " + (frame + 1) + " ===");
                world.progress(0.016f);
            }

            debugSystem.disable();
            System.out.println("\nDebugSystem disabled");
            world.progress(0.016f);
        }
    }
}

