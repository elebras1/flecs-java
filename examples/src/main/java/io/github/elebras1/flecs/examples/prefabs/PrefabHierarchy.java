package io.github.elebras1.flecs.examples.prefabs;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates that instantiating a prefab also instantiates its children.
 */
public class PrefabHierarchy {

    public static void main(String[] args) {
        World world = new World();

        // Create a prefab hierarchy.
        Entity spaceShip = world.obtainEntity(world.entity("SpaceShip"))
                .add(Flecs.Prefab);

        world.obtainEntity(world.entity("Engine"))
                .add(Flecs.Prefab)
                .childOf(spaceShip);

        world.obtainEntity(world.entity("Cockpit"))
                .add(Flecs.Prefab)
                .childOf(spaceShip);

        // Instantiate the prefab. This also creates Engine and Cockpit children
        // for the instance.
        Entity inst = world.obtainEntity(world.entity("my_spaceship"))
                .isA(spaceShip.id());

        long instEngine = inst.lookup("Engine");
        long instCockpit = inst.lookup("Cockpit");

        System.out.println("instance engine:  " + world.obtainEntity(instEngine).name());
        System.out.println("instance cockpit: " + world.obtainEntity(instCockpit).name());

        world.destroy();
    }

    // Output:
    // instance engine:  Engine
    // instance cockpit: Cockpit
}
