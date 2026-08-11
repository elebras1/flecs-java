package io.github.elebras1.flecs.examples.relationships;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Eats;
import io.github.elebras1.flecs.util.Flecs;

public class RelationBasics {

    static void main(String[] args) {
        World world = new World();
        // Register the Eats relationship.
        long eatsId = world.component(Eats.class);

        // Create plain entities for the Grows relationship and its targets.
        // In the C++ example these are just named entities, not component types.
        long grows = world.entity("Grows");
        long apples = world.entity("Apples");
        long pears = world.entity("Pears");

        // Create an entity with multiple relationships. Relationships are like regular
        // components, but combine two identifiers into a (relationship, object) pair.
        Entity bob = world.obtainEntity(world.entity("Bob"));

        // Pairs can be constructed from a component id and entity id.
        bob.add(eatsId, apples);
        bob.add(eatsId, pears);

        // Pairs can also be constructed from two entity ids.
        bob.add(grows, pears);

        // has can be used with relationships as well.
        System.out.println("Bob eats apples? " + (bob.has(eatsId, apples) ? 1 : 0));

        // Wildcards can be used to match relationships.
        System.out.println("Bob grows food? " + (bob.has(grows, Flecs.Wildcard) ? 1 : 0));

        // Print the type of the entity. The exact string depends on how the
        // Java binding formats component names (here fully-qualified names).
        System.out.println("Bob's type: [" + bob.table().str() + "]");

        // Get first target of relationship.
        long first = bob.target(eatsId);
        System.out.println("Bob eats " + world.obtainEntity(first).name());

        // Get second target of relationship.
        long second = bob.target(eatsId, 1);
        System.out.println("Bob also eats " + world.obtainEntity(second).name());

        world.destroy();
    }

    // Output:
    // Bob eats apples? 1
    // Bob grows food? 1
    // Bob's type: [(Identifier,Name), (Eats,Apples), (Eats,Pears), (Grows,Pears)]
    // Bob eats Apples
    // Bob also eats Pears
}
