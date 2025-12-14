package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;

public class HooksExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            System.out.println("Registering Position component with hooks...");

            world.component(Position.class, hooks -> {
                hooks.onAdd((it, components) -> System.out.println("hook.onAdd: \" + components.length + \" elements"));
                hooks.onSet((it, components) -> System.out.println("hook.onSet: " + components[0]));
                hooks.ctor((components, count) -> System.out.println("hook.ctor: " + count + " elements initialized"));
                hooks.dtor((components, count) -> System.out.println("hook.dtor: " + count + " elements destroyed"));
            });

            Entity ent = world.obtainEntity(world.entity("HookedEntity"));

            System.out.println("\n--- Adding the component ---");
            ent.set(new Position(1.5f, 2.5f));
            System.out.println("Current value: " + ent.get(Position.class));

            System.out.println("\n--- Modifying the component ---");
            ent.set(new Position(3.0f, 4.0f));

            System.out.println("\n--- Removing the component ---");
            ent.remove(Position.class);

            world.progress(0.016f);
        }
    }
}
