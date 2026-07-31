package io.github.elebras1.flecs.examples.entities;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Walking;

public class EntityBasics {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Walking.class);

        // Create an entity with name Bob.
        Entity bob = world.obtainEntity(world.entity("Bob"))
                // The set operation finds or creates a component, and sets it.
                .set(new Position(10, 20))
                // The add operation adds a component without setting a value. This is
                // useful for tags, or when adding a component with its default value.
                .add(Walking.class);

        // Get the value for the Position component.
        Position c = bob.get(Position.class);
        System.out.println("{" + c.x() + ", " + c.y() + "}");

        // Overwrite the value of the Position component.
        bob.set(new Position(20, 30));

        // Create another named entity.
        Entity alice = world.obtainEntity(world.entity("Alice"))
                .set(new Position(10, 20));

        // Add a tag after entity is created.
        alice.add(Walking.class);

        // Print all of the components the entity has.
        System.out.println("[" + alice.table().str() + "]");

        // Remove tag.
        alice.remove(Walking.class);

        // Iterate all entities with Position.
        Query query = world.query().with(Position.class).build();
        query.each(Position.class, (entityId, pos) -> {
            Entity entity = world.obtainEntity(entityId);
            System.out.println(entity.name() + ": {" + pos.x() + ", " + pos.y() + "}");
        });
        query.destroy();

        world.destroy();
    }

    // Output:
    // {10.0, 20.0}
    // [Position, Walking, (Identifier,Name)]
    // Alice: {10.0, 20.0}
    // Bob: {20.0, 30.0}
}
