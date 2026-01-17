package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;

public class HooksExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class, hooks -> {
                hooks.onAdd((components) ->
                    System.out.println("onAdd: " + components.length + " elements"));
                hooks.onSet((components) ->
                    System.out.println("onSet: " + components[0]));
                hooks.ctor((components, count) ->
                    System.out.println("ctor: " + count + " elements"));
                hooks.dtor((components, count) ->
                    System.out.println("dtor: " + count + " elements"));
            });

            Entity ent = world.obtainEntity(world.entity("HookedEntity"));

            ent.set(new Position(1.5f, 2.5f));
            System.out.println("Current: " + ent.get(Position.class));

            ent.set(new Position(3.0f, 4.0f));

            ent.remove(Position.class);

            world.progress(0.016f);
        }
    }
}
