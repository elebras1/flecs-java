package io.github.elebras1.flecs.examples;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Apples;
import io.github.elebras1.flecs.examples.components.Eats;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.util.Flecs;

public class HelloWorld {

    static void main(String[] args) {
        World world = new World();

        // Register components.
        world.component(Position.class);
        world.component(Velocity.class);
        world.component(Eats.class);
        world.component(Apples.class);

        // Register a system that updates Position from Velocity.
        world.system("MoveSystem")
                .with(Position.class)
                .with(Velocity.class)
                .kind(Flecs.OnUpdate)
                .iter(it -> {
                    Field<Position> positions = it.field(Position.class, 0);
                    Field<Velocity> velocities = it.field(Velocity.class, 1);
                    for (int i = 0; i < it.count(); i++) {
                        Position pos = positions.get(i);
                        Velocity vel = velocities.get(i);
                        Entity entity = world.obtainEntity(it.entity(i));
                        entity.set(new Position(pos.x() + vel.dx(), pos.y() + vel.dy()));
                    }
                });

        // Create an entity named Bob, add Position, Velocity and the (Eats, Apples) pair.
        long eatsId = world.component(Eats.class);
        long applesId = world.component(Apples.class);
        Entity bob = world.obtainEntity(world.entity("Bob"))
                .set(new Position(0, 0))
                .set(new Velocity(1, 2))
                .addRelation(eatsId, applesId);

        // Show us what you got.
        System.out.println(bob.name() + "'s got [" + bob.table().str() + "]");

        // Run systems twice. Usually this function is called once per frame.
        world.progress(0.0f);
        world.progress(0.0f);

        // See if Bob has moved (he has).
        Position p = bob.get(Position.class);
        System.out.println(bob.name() + "'s position is {" + p.x() + ", " + p.y() + "}");

        world.destroy();
    }

    // Output:
    // Bob's got [Position, Velocity, (Identifier,Name), (Eats,Apples)]
    // Bob's position is {2.0, 4.0}
}
