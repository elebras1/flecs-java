package io.github.elebras1.flecs.examples.entities;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Id;
import io.github.elebras1.flecs.Table;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Apples;
import io.github.elebras1.flecs.examples.components.Eats;
import io.github.elebras1.flecs.examples.components.Human;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;

/**
 * Demonstrates how to iterate the component ids of an entity and inspect
 * regular ids versus pairs.
 */
public class IterateComponents {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);
        world.component(Human.class);
        world.component(Eats.class);
        world.component(Apples.class);

        // Build Bob with Position, Velocity, Human and a (Eats, Apples) pair.
        long eatsId = world.component(Eats.class);
        long applesId = world.component(Apples.class);
        Entity bob = world.obtainEntity(world.entity())
                .set(new Position(10, 20))
                .set(new Velocity(1, 1))
                .add(Human.class)
                .addRelation(eatsId, applesId);

        System.out.println("Entity's components:");
        iterateComponents(world, bob);

        // We can use the same function to iterate the components of a component.
        System.out.println("Position's components:");
        iterateComponents(world, world.obtainEntity(world.component(Position.class)));

        world.destroy();
    }

    private static void iterateComponents(World world, Entity entity) {
        // The easiest way to print the components is to use the table string.
        Table table = entity.table();
        if (table != null) {
            System.out.println(table.str());
        }
        System.out.println();

        // To get individual component ids, use the entity's table type.
        long[] type = table != null ? table.type() : new long[0];
        for (int i = 0; i < type.length; i++) {
            System.out.println(i + ": " + idString(world, type[i]));
        }
        System.out.println();

        // Inspect and print the ids in our own way. This is more complicated
        // because we need to handle what can be encoded in an id.
        for (int i = 0; i < type.length; i++) {
            System.out.print(i + ": ");
            Id id = world.obtainId(type[i]);
            if (id.isPair()) {
                long rel = id.first();
                long tgt = id.second();
                System.out.println("rel: " + world.obtainEntity(rel).name() + ", tgt: " + world.obtainEntity(tgt).name());
            } else {
                System.out.println("entity: " + world.obtainEntity(type[i]).name());
            }
        }
        System.out.println("\n");
    }

    private static String idString(World world, long id) {
        Id idObj = world.obtainId(id);
        if (idObj.isPair()) {
            long rel = idObj.first();
            long tgt = idObj.second();
            return "(" + world.obtainEntity(rel).name() + "," + world.obtainEntity(tgt).name() + ")";
        }
        return world.obtainEntity(id).name();
    }

    // Output (format may vary slightly depending on the binding):
    // Entity's components:
    // Position, Velocity, Human, (Eats,Apples)
    //
    // 0: Position
    // 1: Velocity
    // 2: Human
    // 3: (Eats,Apples)
    //
    // 0: entity: Position
    // 1: entity: Velocity
    // 2: entity: Human
    // 3: rel: Eats, tgt: Apples
    //
    //
    // Position's components:
    // Component, (Identifier,Name), (Identifier,Symbol)
    //
    // 0: Component
    // 1: (Identifier,Name)
    // 2: (Identifier,Symbol)
    //
    // 0: entity: Component
    // 1: rel: Identifier, tgt: Name
    // 2: rel: Identifier, tgt: Symbol
}
