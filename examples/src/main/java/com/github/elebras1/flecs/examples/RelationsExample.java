package com.github.elebras1.flecs.examples;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.examples.components.Position;

public class RelationsExample {

    public static void main(String[] args) {
        try (World world = new World()) {
            world.component(Position.class);

            Entity root = world.obtainEntity(world.entity("Root"));
            root.set(new Position(0, 0));

            Entity child1 = world.obtainEntity(world.entity("Child1"));
            child1.set(new Position(10, 10));
            child1.childOf(root);

            Entity child2 = world.obtainEntity(world.entity("Child2"));
            child2.set(new Position(20, 20));
            child2.childOf(root);

            Entity grandchild = world.obtainEntity(world.entity("Grandchild"));
            grandchild.set(new Position(15, 15));
            grandchild.childOf(child1);

            System.out.println("Child1 parent: " + child1.parent().getName());
            System.out.println("Grandchild parent: " + grandchild.parent().getName());

            System.out.println("\nRoot children:");
            root.children(child -> System.out.println("  - " + child.getName()));

            System.out.println("\nChild1 children:");
            child1.children(child -> System.out.println("  - " + child.getName()));

            long likesRelation = world.entity("Likes");
            long eatsRelation = world.entity("Eats");

            Entity alice = world.obtainEntity(world.entity("Alice"));
            Entity bob = world.obtainEntity(world.entity("Bob"));
            Entity apple = world.obtainEntity(world.entity("Apple"));

            alice.addRelation(likesRelation, bob.id());
            alice.addRelation(eatsRelation, apple.id());
            bob.addRelation(likesRelation, alice.id());

            System.out.println("\nAlice likes Bob: " + alice.hasRelation(likesRelation, bob.id()));
            System.out.println("Alice eats Apple: " + alice.hasRelation(eatsRelation, apple.id()));
            System.out.println("Bob likes Alice: " + bob.hasRelation(likesRelation, alice.id()));

            long vehicleId = world.entity("Vehicle");
            long carId = world.entity("Car");

            Entity myCar = world.obtainEntity(world.entity("MyCar"));
            myCar.isA(carId);

            Entity car = world.obtainEntity(carId);
            car.isA(vehicleId);

            System.out.println("\nMyCar is a Car: " + myCar.hasRelation(world.lookup("IsA"), carId));

            alice.removeRelation(likesRelation, bob.id());
            System.out.println("\nAlice likes Bob after remove: " + alice.hasRelation(likesRelation, bob.id()));
        }
    }
}

