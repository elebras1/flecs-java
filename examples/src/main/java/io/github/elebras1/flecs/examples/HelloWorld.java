package io.github.elebras1.flecs.examples;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.examples.components.Apples;
import io.github.elebras1.flecs.examples.components.Eats;
import io.github.elebras1.flecs.examples.components.Position;
import io.github.elebras1.flecs.examples.components.Velocity;
import io.github.elebras1.flecs.examples.components.VelocityView;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.examples.components.PositionMutView;

public class HelloWorld {

    static void main(String[] args) {
        try (World world = new World()) {

            // Register components.
            world.component(Position.class);
            world.component(Velocity.class);
            world.component(Eats.class);
            world.component(Apples.class);

            // Register a system that updates Position from Velocity.
            world.system("MoveSystem", Position.class, Velocity.class)
                    .kind(Flecs.OnUpdate)
                    .eachView(Position.class, Velocity.class, (PositionMutView pos, VelocityView vel) -> {
                        pos.x(pos.x() + vel.dx());
                        pos.y(pos.y() + vel.dy());
                    });

            // Create an entity named Bob, add Position, Velocity and the (Eats, Apples) pair.
            Entity bob = world.obtainEntity(world.entity("Bob"))
                    .set(new Position(0, 0))
                    .set(new Velocity(1, 2))
                    .add(Eats.class, Apples.class);

            // Show us what you got.
            System.out.println(bob.name() + "'s got [" + bob.table().toString() + "]");

            // Run systems twice. Usually this function is called once per frame.
            world.progress(0.0f);
            world.progress(0.0f);

            // See if Bob has moved (he has).
            Position p = bob.get(Position.class);
            System.out.println(bob.name() + "'s position is {" + p.x() + ", " + p.y() + "}");

        }
    }

    // Output:
    // Bob's got [Position, Velocity, (Identifier,Name), (Eats,Apples)]
    // Bob's position is {2.0, 4.0}
}
