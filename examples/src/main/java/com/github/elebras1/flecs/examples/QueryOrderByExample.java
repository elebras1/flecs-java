package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.examples.components.*;

public class QueryOrderByExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);

            world.obtainEntity(world.entity("Tree")).set(new Position(12, 1));
            world.obtainEntity(world.entity("Player")).set(new Position(3, 2));
            world.obtainEntity(world.entity("Enemy")).set(new Position(8, -1));
            world.obtainEntity(world.entity("Rock")).set(new Position(3, -4));

            try (Query query = world.query().with(Position.class).orderBy(Position.class, (PositionView a, PositionView b) -> Float.compare(a.x(), b.x())).build()) {
                query.each(entityId -> {
                    Entity entity = world.obtainEntity(entityId);
                    Position position = entity.get(Position.class);
                    System.out.println(" - " + entity.getName() + " (" + position.x() + ", " + position.y() + ")");
                });
            }
        }
    }
}
