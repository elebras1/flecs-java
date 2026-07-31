package io.github.elebras1.flecs.examples.entities;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;

public class Hooks {

    static void main(String[] args) {
        World world = new World();

        // Register component with hooks.
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

        world.destroy();
    }

    // Output:
    // ctor: 1 elements
    // onAdd: 1 elements
    // onSet: Position[x=1.5, y=2.5]
    // Current: Position[x=1.5, y=2.5]
    // ctor: 1 elements
    // onSet: Position[x=3.0, y=4.0]
    // dtor: 1 elements
}
