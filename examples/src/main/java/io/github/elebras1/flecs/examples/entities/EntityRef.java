package io.github.elebras1.flecs.examples.entities;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Ref;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Amount;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;

public class EntityRef {

    static void main(String[] args) {
        World world = new World();
        world.component(Position.class);
        world.component(Velocity.class);
        world.component(Amount.class);

        // Refs cache the pointer to a component of an entity so it can be read
        // quickly and repeatedly without a full lookup. They stay valid while
        // the entity keeps its set of components.

        // Create an entity with Position and Velocity.
        Entity player = world.obtainEntity(world.entity("Player"))
                .set(new Position(10, 20))
                .set(new Velocity(3, 4));

        // Take a ref to the Position component on the player entity.
        Ref<Position> posRef = player.getRef(Position.class);
        System.out.println(
            "Player position: {" + posRef.get().x() + ", " + posRef.get().y() + "}");
        System.out.println("Player has Position: " + posRef.has());

        // Overwriting an existing component is immediately visible through the ref.
        player.set(new Position(5, 6));
        System.out.println(
            "Player position (after set): {" + posRef.get().x() + ", " + posRef.get().y() + "}");

        // A ref allocates its own arena; call destroy() when done.
        posRef.destroy();

        // Refs can also be bound to a component stored for a specific target
        // (a pair). Here every depot stores an Amount of each resource, under
        // the pair (Amount, resource).

        Entity gold = world.obtainEntity(world.entity("Gold"));
        Entity depot = world.obtainEntity(world.entity("Depot"));
        depot.set(new Amount(5), gold.id());
        depot.set(new Amount(3), world.obtainEntity(world.entity("Iron")).id());

        // Take a ref to the Amount gold has in the depot. This matches the
        // get_ref<Stores>(resource) pattern from the C++ factory example.
        Ref<Amount> goldRef = depot.getRef(Amount.class, gold);
        System.out.println("Depot gold: " + goldRef.get().value());

        depot.set(new Amount(12), gold.id());
        System.out.println("Depot gold (after set): " + goldRef.get().value());

        goldRef.destroy();

        world.destroy();
    }

    // Output:
    // Player position: {10.0, 20.0}
    // Player has Position: true
    // Player position (after set): {5.0, 6.0}
    // Depot gold: 5
    // Depot gold (after set): 12
}