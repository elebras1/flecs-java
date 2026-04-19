package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;

public class RelationsExample {

    public static void main(String[] args) {
        World world = new World();
        world.component(Position.class);

        childOfHierarchy(world);
        parentHierarchy(world);
        customRelationships(world);
        isaInheritance(world);

        world.destroy();
    }

    private static void childOfHierarchy(World world) {
        System.out.println("=== ChildOf Hierarchy ===");

        Entity scene = world.obtainEntity(world.entity("Scene"));
        scene.set(new Position(0, 0));

        Entity child1 = world.obtainEntity(world.entity("Child1"));
        child1.set(new Position(10, 10));
        child1.childOf(scene);

        Entity child2 = world.obtainEntity(world.entity("Child2"));
        child2.set(new Position(20, 20));
        child2.childOf(scene);

        Entity grandchild = world.obtainEntity(world.entity("Grandchild"));
        grandchild.set(new Position(15, 15));
        grandchild.childOf(child1);

        System.out.println("Child1 parent: " + world.obtainEntity(child1.parent()).getName());
        System.out.println("Grandchild parent: " + world.obtainEntity(grandchild.parent()).getName());

        System.out.println("Scene children:");
        scene.children(c -> System.out.println("  - " + world.obtainEntity(c).getName()));

        System.out.println("Child1 children:");
        child1.children(c -> System.out.println("  - " + world.obtainEntity(c).getName()));
    }

    private static void parentHierarchy(World world) {
        System.out.println("\n=== Parent Hierarchy ===");

        Entity spaceship = world.obtainEntity(world.entity("Spaceship"));
        spaceship.set(new Position(100, 200));

        Entity cockpit = world.obtainEntity(world.entity(spaceship.id(), "Cockpit"));
        cockpit.set(new Position(105, 210));

        Entity engine = world.obtainEntity(world.entity(spaceship.id(), "Engine"));
        engine.set(new Position(95, 190));

        Entity fuelTank = world.obtainEntity(world.entity(engine.id(), "FuelTank"));
        fuelTank.set(new Position(93, 188));

        System.out.println("Cockpit parent: " + world.obtainEntity(cockpit.parent()).getName());
        System.out.println("FuelTank parent: " + world.obtainEntity(fuelTank.parent()).getName());

        System.out.println("Spaceship children:");
        spaceship.children(c -> System.out.println("  - " + world.obtainEntity(c).getName()));

        System.out.println("Engine children:");
        engine.children(c -> System.out.println("  - " + world.obtainEntity(c).getName()));
    }

    private static void customRelationships(World world) {
        System.out.println("\n=== Custom Relationships ===");

        long likes = world.entity("Likes");
        long eats = world.entity("Eats");

        Entity alice = world.obtainEntity(world.entity("Alice"));
        Entity bob = world.obtainEntity(world.entity("Bob"));
        Entity apple = world.obtainEntity(world.entity("Apple"));

        alice.addRelation(likes, bob.id());
        alice.addRelation(eats, apple.id());
        bob.addRelation(likes, alice.id());

        System.out.println("Alice likes Bob: " + alice.hasRelation(likes, bob.id()));
        System.out.println("Alice eats Apple: " + alice.hasRelation(eats, apple.id()));
        System.out.println("Bob likes Alice: " + bob.hasRelation(likes, alice.id()));

        alice.removeRelation(likes, bob.id());
        System.out.println("Alice likes Bob after remove: " + alice.hasRelation(likes, bob.id()));
    }

    private static void isaInheritance(World world) {
        System.out.println("\n=== IsA Inheritance ===");

        long vehicleId = world.entity("Vehicle");
        long carId = world.entity("Car");

        Entity car = world.obtainEntity(carId);
        car.isA(vehicleId);

        Entity myCar = world.obtainEntity(world.entity("MyCar"));
        myCar.isA(carId);

        long isA = world.lookup("IsA");
        System.out.println("MyCar isA Car: " + myCar.hasRelation(isA, carId));
        System.out.println("Car isA Vehicle: " + car.hasRelation(isA, vehicleId));
    }
}