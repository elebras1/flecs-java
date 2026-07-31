package io.github.elebras1.flecs.examples.entities;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Moon;
import io.github.elebras1.flecs.examples.components.Planet;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Star;
import io.github.elebras1.flecs.util.Flecs;

public class Hierarchy {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Star.class);
        world.component(Planet.class);
        world.component(Moon.class);

        // Create a simple hierarchy.
        // Hierarchies use ECS relationships and the builtin ChildOf relationship to
        // create entities as children of other entities.
        Entity sun = world.obtainEntity(world.entity("Sun"))
                .add(Star.class)
                .set(new Position(1, 1));

        long previousScope = world.setScope(sun.id());
        world.obtainEntity(world.entity("Mercury"))
                .add(Planet.class)
                .set(new Position(1, 1));

        world.obtainEntity(world.entity("Venus"))
                .add(Planet.class)
                .set(new Position(2, 2));

        Entity earth = world.obtainEntity(world.entity("Earth"))
                .add(Planet.class)
                .set(new Position(3, 3));

        // Create Moon as a child of Earth.
        world.setScope(earth.id());
        Entity moon = world.obtainEntity(world.entity("Moon"))
                .add(Moon.class)
                .set(new Position(0.1f, 0.1f));
        world.setScope(previousScope);

        // Is the Moon a child of Earth?
        System.out.println("Child of Earth? " + (moon.hasRelation(Flecs.ChildOf, earth.id()) ? 1 : 0));
        System.out.println();

        // The moon entity was already obtained while creating it.
        System.out.println("Moon found: " + moon.name());
        System.out.println();

        // Do a depth-first walk of the tree.
        iterateTree(world, sun, new Position(0, 0));

        world.destroy();
    }

    private static void iterateTree(World world, Entity entity, Position parentPosition) {
        // Print hierarchical name of entity & the entity type.
        String type = entity.table().str();
        System.out.println(entity.name() + " [" + type + "]");

        // Get entity position.
        Position p = entity.get(Position.class);
        Position actual = new Position(p.x() + parentPosition.x(), p.y() + parentPosition.y());
        System.out.println("{" + actual.x() + ", " + actual.y() + "}\n");

        // Iterate children recursively.
        entity.children(childId -> {
            Entity child = world.obtainEntity(childId);
            iterateTree(world, child, actual);
        });
    }

    // Output:
    // Child of Earth? 1
    //
    // Moon found: Moon
    //
    // Sun [Position, Star, (Identifier,Name)]
    // {1.0, 1.0}
    //
    // Mercury [Position, Planet, (Identifier,Name), (ChildOf,Sun)]
    // {2.0, 2.0}
    //
    // Venus [Position, Planet, (Identifier,Name), (ChildOf,Sun)]
    // {3.0, 3.0}
    //
    // Earth [Position, Planet, (Identifier,Name), (ChildOf,Sun)]
    // {4.0, 4.0}
    //
    // Moon [Position, Moon, (Identifier,Name), (ChildOf,Sun.Earth)]
    // {4.1, 4.1}
}
