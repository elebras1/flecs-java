package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;
import com.github.elebras1.flecs.examples.components.Velocity;

import static com.github.elebras1.flecs.util.FlecsConstants.*;

public class ObserverExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);
            world.component(Velocity.class);

            world.observer(Position.class).event(EcsOnAdd).each((entityId) ->
                    System.out.println("Position added to " + entityId)
            );

            Entity e1 = world.obtainEntity(world.entity("Entity1"));
            e1.set(new Position(10.0f, 20.0f));

            world.observer(Position.class).event(EcsOnSet).iter((it) -> {
                for (int i = 0; i < it.count(); i++) {
                    long entityId = it.entityId(i);
                    Position pos = world.obtainEntity(entityId).get(Position.class);
                    System.out.println("Position set to (" + pos.x() + ", " + pos.y() + ")");
                }
            });

            e1.set(new Position(30.0f, 40.0f));

            world.observer().with(Position.class).with(Velocity.class).event(EcsOnAdd).each((entityId) ->
                    System.out.println("Entity " + entityId + " has Position and Velocity")
            );

            Entity e2 = world.obtainEntity(world.entity("Entity2"));
            e2.set(new Position(5.0f, 5.0f));
            e2.set(new Velocity(1.0f, 1.0f));

            world.observer(Velocity.class).event(EcsOnAdd).event(EcsOnRemove).iter((it) -> {
                String event = (it.event() == EcsOnAdd) ? "OnAdd" : "OnRemove";
                System.out.println("Velocity " + event + " (" + it.count() + " entities)");
            });

            Entity e3 = world.obtainEntity(world.entity("Entity3"));
            e3.set(new Velocity(2.0f, 2.0f));
            e3.remove(Velocity.class);

            world.observer().with(Position.class).with(Velocity.class).inOut().event(EcsOnAdd).each((entityId) ->
                    System.out.println("Position added to entity with Velocity: " + entityId)
            );

            Entity e4 = world.obtainEntity(world.entity("Entity4"));
            e4.set(new Velocity(1.0f, 1.0f));
            e4.set(new Position(50.0f, 50.0f));

            Entity e5 = world.obtainEntity(world.entity("Entity5"));
            e5.set(new Position(60.0f, 60.0f));
        }
    }
}

