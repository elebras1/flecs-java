package io.github.elebras1.flecs.examples.relationships;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Platoon;
import io.github.elebras1.flecs.util.Flecs;

/**
 * Demonstrates an exclusive relationship: an entity can be related to at most
 * one target for that relationship at a time. Adding a new target replaces the
 * previous one.
 */
public class ExclusiveRelations {

    public static void main(String[] args) {
        World world = new World();

        // Register Platoon as an exclusive relationship. This ensures that an entity
        // can only belong to a single platoon.
        long platoonId = world.component(Platoon.class);
        world.obtainEntity(platoonId).add(Flecs.Exclusive);

        // Create two platoons.
        long platoon1 = world.entity();
        long platoon2 = world.entity();

        // Create a unit.
        Entity unit = world.obtainEntity(world.entity());

        // Add unit to platoon 1.
        unit.addRelation(platoonId, platoon1);

        // Log platoon of unit.
        System.out.println("Unit in platoon 1: " + (unit.hasRelation(platoonId, platoon1)));
        System.out.println("Unit in platoon 2: " + (unit.hasRelation(platoonId, platoon2)));
        System.out.println();

        // Add unit to platoon 2. Because Platoon is exclusive, this removes the
        // first pair and adds the second one in a single operation.
        unit.addRelation(platoonId, platoon2);

        System.out.println("Unit in platoon 1: " + (unit.hasRelation(platoonId, platoon1)));
        System.out.println("Unit in platoon 2: " + (unit.hasRelation(platoonId, platoon2)));

        world.destroy();
    }

    // Output:
    // Unit in platoon 1: true
    // Unit in platoon 2: false
    //
    // Unit in platoon 1: false
    // Unit in platoon 2: true
}
