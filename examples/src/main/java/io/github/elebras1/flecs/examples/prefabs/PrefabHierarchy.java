package io.github.elebras1.flecs.examples.prefabs;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;

/**
 * Demonstrates that instantiating a prefab also instantiates its children.
 */
public class PrefabHierarchy {

    public static void main(String[] args) {
        try (World world = new World()) {

            // Create a prefab hierarchy.
            Entity spaceShip = world.obtainEntity(world.prefab("SpaceShip"));

            world.obtainEntity(world.prefab("Engine"))
                    .childOf(spaceShip);

            world.obtainEntity(world.prefab("Cockpit"))
                    .childOf(spaceShip);

            // Instantiate the prefab. This also creates Engine and Cockpit children
            // for the instance.
            Entity inst = world.obtainEntity(world.entity("my_spaceship"))
                    .isA(spaceShip);

            long instEngine = inst.lookup("Engine");
            long instCockpit = inst.lookup("Cockpit");

            System.out.println("instance engine:  " + world.obtainEntity(instEngine).path());
            System.out.println("instance cockpit: " + world.obtainEntity(instCockpit).path());

        }
    }

    // Output:
    // instance engine:  ::my_spaceship::Engine
    // instance cockpit: ::my_spaceship::Cockpit
}
